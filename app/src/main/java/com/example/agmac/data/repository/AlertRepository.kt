package com.example.agmac.data.repository

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.agmac.data.model.Alerta
import com.example.agmac.data.SessionManager
import com.example.agmac.data.repository.remote.AlertApiProvider
import com.example.agmac.data.repository.remote.CreateAlertaRequest
import com.example.agmac.data.repository.remote.UpdateAlertaRequest
import com.example.agmac.receivers.ReminderReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object AlertRepository {
    private fun getAlertsFile(context: Context): File = File(context.filesDir, "alertas.json")

    private fun requestCodeForAlert(alerta: Alerta): Int {
        val keyString = "${alerta.id_paciente}_${alerta.id_medicamento}_${alerta.hora_programada}"
        return kotlin.math.abs(keyString.hashCode())
    }

    suspend fun loadAlertsFromServer(context: Context, idPaciente: Int): List<Alerta> = withContext(Dispatchers.IO) {
        val api = AlertApiProvider.alertApi
        val query = "{" + "\"id_paciente\":$idPaciente}" // Filtro JSON
        val orderBy = "{" + "\"hora_programada\":\"ASC\"}" // Orden
        val response = api.getAlertas(query, orderBy)
        try {
            android.util.Log.d("AlertRepository", "getAlertas response code=${response.code()}")
            try {
                val bodyJson = Gson().toJson(response.body())
                android.util.Log.d("AlertRepository", "getAlertas bodyJson=$bodyJson")
            } catch (_: Exception) {
                // ignore
            }
        } catch (_: Exception) {
            // ignore logging errors
        }

        val alertas = response.body()?.items ?: emptyList()
        // Log each alerta id to diagnose missing ids (id_alerta == 0)
        alertas.forEach { a ->
            try {
                android.util.Log.d("AlertRepository", "Loaded alerta: id_alerta=${a.id_alerta} id_medicamento=${a.id_medicamento} hora_programada=${a.hora_programada} estado=${a.estado}")
            } catch (_: Exception) {
                // ignore per-item logging errors
            }
        }
        saveAlertsToLocal(context, alertas)
        alertas
    }

    suspend fun markAlertAsTaken(context: Context, idAlerta: Int, horaConfirmacion: String): Boolean = withContext(Dispatchers.IO) {
        val api = AlertApiProvider.alertApi
        // Obtener id paciente desde sesión para evitar enviar NULL al backend
        var idPaciente = SessionManager.getUserId(context)
        if (idPaciente <= 0) {
            // Fallback: intentar leer id_paciente de la alerta local (si existe)
            try {
                val local = loadAlertsFromLocal(context)
                val found = local.firstOrNull { it.id_alerta == idAlerta }
                if (found != null && found.id_paciente > 0) {
                    idPaciente = found.id_paciente
                }
            } catch (_: Exception) {
                // ignore
            }
        }
        if (idPaciente <= 0) {
            android.util.Log.e("AlertRepository", "No hay id_paciente disponible (sesión ni alerta local); no se puede marcar alerta como tomada")
            return@withContext false
        }

        // Para PUT necesitamos enviar el objeto completo. Recuperar id_medicamento, id_paciente y hora_programada del JSON local
        var idMedicamento: Int? = null
        var horaProgramada: String? = null
        try {
            val local = loadAlertsFromLocal(context)
            val found = local.firstOrNull { it.id_alerta == idAlerta }
            if (found != null) {
                if (found.id_medicamento > 0) idMedicamento = found.id_medicamento
                if (found.id_paciente > 0) idPaciente = found.id_paciente
                if (found.hora_programada.isNotBlank()) horaProgramada = found.hora_programada
            }
        } catch (_: Exception) {
            // ignore
        }

        // Si falta algo, intentar recuperar desde el servidor
        if (idMedicamento == null || horaProgramada.isNullOrBlank()) {
            try {
                val serverList = loadAlertsFromServer(context, idPaciente)
                val found = serverList.firstOrNull { it.id_alerta == idAlerta }
                if (found != null) {
                    if (idMedicamento == null && found.id_medicamento > 0) idMedicamento = found.id_medicamento
                    if (horaProgramada.isNullOrBlank() && found.hora_programada.isNotBlank()) horaProgramada = found.hora_programada
                    if (found.id_paciente > 0) idPaciente = found.id_paciente
                }
            } catch (e: Exception) {
                android.util.Log.w("AlertRepository", "No se pudo obtener datos completos desde servidor: ${e.message}")
            }
        }

        if (idMedicamento == null) {
            android.util.Log.e("AlertRepository", "No se encontró id_medicamento para la alerta $idAlerta; evitar enviar NULL al backend")
            return@withContext false
        }

        if (horaProgramada.isNullOrBlank()) {
            android.util.Log.e("AlertRepository", "No se encontró hora_programada para la alerta $idAlerta; evitar enviar PUT incompleto")
            return@withContext false
        }

        val body = UpdateAlertaRequest(
            estado = "TOMADO",
            hora_confirmacion = horaConfirmacion,
            id_paciente = idPaciente,
            id_medicamento = idMedicamento,
            hora_programada = horaProgramada
        )
        try {
            // Log del body que se enviará (útil para depuración sin agregar interceptores externos)
            try {
                val bodyJson = Gson().toJson(body)
                android.util.Log.d("AlertRepository", "updateAlerta request: id=$idAlerta body=$bodyJson")
            } catch (_: Exception) {
                // no bloquear la ejecución si falla el log
            }
            val response = api.updateAlerta(idAlerta, body)
            if (response.isSuccessful) {
                val alertas = loadAlertsFromLocal(context).toMutableList()
                val index = alertas.indexOfFirst { it.id_alerta == idAlerta }
                if (index != -1) {
                    // Cancel alarm usando la alerta actual (antes de modificarla)
                    try {
                        val requestCode = requestCodeForAlert(alertas[index])
                        cancelAlarm(context, requestCode)
                    } catch (ex: Exception) {
                        android.util.Log.w("AlertRepository", "Fallo cancelando alarma al marcar como tomado: ${ex.message}")
                    }

                    alertas[index] = alertas[index].copy(
                        estado = "TOMADO",
                        hora_confirmacion = horaConfirmacion
                    )
                    saveAlertsToLocal(context, alertas)
                }
                return@withContext true
            } else {
                val errBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                android.util.Log.e("AlertRepository", "Failed updateAlerta: code=${response.code()} body=$errBody")
                return@withContext false
            }
        } catch (ex: Exception) {
            android.util.Log.e("AlertRepository", "Exception updating alerta", ex)
            return@withContext false
        }
    }

    suspend fun deleteAlert(context: Context, idAlerta: Int) = withContext(Dispatchers.IO) { // Elimino parámetro no utilizado 'idPaciente'
        val api = AlertApiProvider.alertApi
        val response = api.deleteAlerta(idAlerta)
        if (response.isSuccessful) {
            val alertasActuales = loadAlertsFromLocal(context)
            // buscar la alerta para cancelar su alarma
            val alertaToRemove = alertasActuales.firstOrNull { it.id_alerta == idAlerta }
            if (alertaToRemove != null) {
                try {
                    val requestCode = requestCodeForAlert(alertaToRemove)
                    cancelAlarm(context, requestCode)
                } catch (ex: Exception) {
                    android.util.Log.w("AlertRepository", "Fallo cancelando alarma al eliminar: ${ex.message}")
                }
            }

            val alertas = alertasActuales.filter { it.id_alerta != idAlerta }
            saveAlertsToLocal(context, alertas)
            // Cancelar alarma asociada ya hecho arriba
        }
    }

    // Ensure there is a alerts file in internal storage. If not, try to copy from assets/alertas.json
    private fun ensureAlertsFileInitialized(context: Context) {
        val file = getAlertsFile(context)
        if (!file.exists()) {
            try {
                // Try copying template from assets if present
                context.assets.open("alertas.json").use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (ex: Exception) {
                android.util.Log.w("AlertRepository", "No se pudo copiar alertas desde assets, se creará archivo vacío: ${ex.message}")
                // If asset not present or copy fails, create an empty JSON array file
                try {
                    file.writeText("[]")
                } catch (exc: Exception) {
                    android.util.Log.e("AlertRepository", "Error creando archivo alertas.json vacío", exc)
                }
            }
        }
    }

    suspend fun createAlertSchedule(
        context: Context,
        idPaciente: Int,
        idMedicamento: Int,
        dosis: String,
        fechaInicioStr: String,
        numeroDeDias: Int,
        horasDelDia: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        val api = AlertApiProvider.alertApi
        val fechas = mutableListOf<String>()

        // Parsear fecha base con SimpleDateFormat (yyyy-MM-dd)
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdfDate.timeZone = TimeZone.getTimeZone("UTC")
        val baseDate = try {
            val d = sdfDate.parse(fechaInicioStr) ?: return@withContext false
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
            cal.time = d
            cal
        } catch (ex: Exception) {
            android.util.Log.e("AlertRepository", "Fecha de inicio inválida: $fechaInicioStr", ex)
            return@withContext false
        }

        val sdfUtc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdfUtc.timeZone = TimeZone.getTimeZone("UTC")

        repeat(numeroDeDias) { dayIndex ->
            val dayCal = baseDate.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, dayIndex)
            horasDelDia.forEach { horaStrRaw ->
                val horaStr = horaStrRaw.trim()
                val parts = horaStr.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull()
                val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val second = parts.getOrNull(2)?.toIntOrNull() ?: 0
                if (hour == null) {
                    android.util.Log.e("AlertRepository", "Hora inválida: $horaStr")
                    return@withContext false
                }
                dayCal.set(Calendar.HOUR_OF_DAY, hour)
                dayCal.set(Calendar.MINUTE, minute)
                dayCal.set(Calendar.SECOND, second)
                dayCal.set(Calendar.MILLISECOND, 0)
                val formatted = sdfUtc.format(dayCal.time) // ejemplo: 2025-11-05T13:00:00Z
                fechas.add(formatted)
            }
        }

        var allOk = true
        fechas.forEach { fechaHora ->
            val request = CreateAlertaRequest(
                id_paciente = idPaciente,
                id_medicamento = idMedicamento,
                dosis = dosis,
                hora_programada = fechaHora,
                estado = "PENDIENTE"
            )
            try {
                android.util.Log.d("AlertRepository", "Enviar createAlerta request: $request")
                val response = api.createAlerta(request)
                if (!response.isSuccessful) {
                    val err = response.errorBody()?.string()
                    android.util.Log.e("AlertRepository", "Error al crear alerta: $err")
                    allOk = false
                } else {
                    // Si la creación fue exitosa, programamos la alarma local simple
                    try {
                        val keyString = "${idPaciente}_${idMedicamento}_$fechaHora"
                        val requestCode = kotlin.math.abs(keyString.hashCode())
                        // Obtener nombre comercial del medicamento desde assets/Medicamento.json
                        val nombreComercial = getMedicamentoNombreFromAssets(context, idMedicamento)
                        scheduleAlarm(context, requestCode, fechaHora, "Recordatorio de medicamento: $nombreComercial", dosis)
                    } catch (ex: Exception) {
                        android.util.Log.w("AlertRepository", "Fallo programando alarma local: ${ex.message}")
                    }
                }
            } catch (ex: Exception) {
                android.util.Log.e("AlertRepository", "Excepción creando alerta: $fechaHora", ex)
                allOk = false
            }
        }

        try {
            loadAlertsFromServer(context, idPaciente)
        } catch (ex: Exception) {
            android.util.Log.e("AlertRepository", "Error al cargar alertas desde servidor después de crear", ex)
        }

        return@withContext allOk
    }

    fun saveAlertsToLocal(context: Context, alertas: List<Alerta>) {
        val file = getAlertsFile(context)
        try {
            file.parentFile?.mkdirs()

            // Merge: no perder campos locales útiles si el servidor devuelve valores vacíos/0.
            val merged: List<Alerta> = try {
                val existing: List<Alerta> = if (file.exists()) {
                    try {
                        Gson().fromJson(file.readText(), object : TypeToken<List<Alerta>>() {}.type)
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else emptyList()

                val existingMap = existing.associateBy { it.id_alerta }

                alertas.map { serverA ->
                    val localA = existingMap[serverA.id_alerta]
                    if (localA == null) {
                        serverA
                    } else {
                        val horaProgramada = when {
                            serverA.hora_programada.isNotBlank() -> serverA.hora_programada
                            localA.hora_programada.isNotBlank() -> localA.hora_programada
                            else -> serverA.hora_programada
                        }
                        val idMedicamento = if (serverA.id_medicamento > 0) serverA.id_medicamento else localA.id_medicamento
                        val idPaciente = if (serverA.id_paciente > 0) serverA.id_paciente else localA.id_paciente
                        val dosis = if (serverA.dosis.isNotBlank()) serverA.dosis else localA.dosis
                        val estado = if (serverA.estado.isNotBlank()) serverA.estado else localA.estado
                        val horaConfirmacion = serverA.hora_confirmacion ?: localA.hora_confirmacion

                        serverA.copy(
                            hora_programada = horaProgramada,
                            id_medicamento = idMedicamento,
                            id_paciente = idPaciente,
                            dosis = dosis,
                            estado = estado,
                            hora_confirmacion = horaConfirmacion
                        )
                    }
                }
            } catch (_: Exception) {
                android.util.Log.w("AlertRepository", "Fallo mergeando alertas locales y servidor; se guarda lo recibido del servidor")
                alertas
            }

            file.writeText(Gson().toJson(merged))
            try {
                val length = file.length()
                val preview = file.readText().let { if (it.length > 200) it.substring(0, 200) + "..." else it }
                android.util.Log.d("AlertRepository", "Alertas guardadas en: ${file.absolutePath} (bytes=$length). Preview: $preview")
            } catch (_: Exception) {
                android.util.Log.w("AlertRepository", "Guardado pero fallo al leer archivo para preview")
            }
        } catch (ex: Exception) {
            android.util.Log.e("AlertRepository", "Error guardando alertas locales", ex)
        }
    }

    fun loadAlertsFromLocal(context: Context): List<Alerta> {
        ensureAlertsFileInitialized(context)
        val file = getAlertsFile(context)
        return if (file.exists()) {
            try {
                Gson().fromJson(file.readText(), object : TypeToken<List<Alerta>>() {}.type)
            } catch (ex: Exception) {
                android.util.Log.e("AlertRepository", "Error parseando alertas locales", ex)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // --- Simple alarm helpers ---
    private fun parseUtcIsoToMillis(iso: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(iso)?.time ?: 0L
        } catch (ex: Exception) {
            0L
        }
    }

    private fun scheduleAlarm(context: Context, requestCode: Int, horaProgramadaIso: String, title: String, text: String) {
        val triggerAt = parseUtcIsoToMillis(horaProgramadaIso)
        if (triggerAt <= 0L) return
        val now = System.currentTimeMillis()
        if (triggerAt <= now) {
            // No agendar alarmas en el pasado
            android.util.Log.d("AlertRepository", "Hora programada ya pasó: $horaProgramadaIso")
            return
        }

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // En Android S (31) o superior, verificar si la app puede programar alarmas exactas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (!am.canScheduleExactAlarms()) {
                    android.util.Log.w("AlertRepository", "No puede programar alarmas exactas. Solicitando permiso al usuario.")
                    // Intentar abrir la pantalla de ajustes para permitir alarmas exactas para esta app
                    try {
                        val intentSettings = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intentSettings)
                    } catch (_: Exception) {
                        android.util.Log.w("AlertRepository", "No se pudo abrir ajustes de alarmas exactas")
                        showRequestExactAlarmNotification(context)
                    }
                    return
                }
            } catch (_: Exception) {
                android.util.Log.w("AlertRepository", "Fallo comprobando canScheduleExactAlarms")
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_ID_ALERTA, requestCode)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_TEXT, text)
        }

        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        flags = flags or PendingIntent.FLAG_IMMUTABLE

        val pending = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            android.util.Log.d("AlertRepository", "Alarma programada (requestCode=$requestCode) para $horaProgramadaIso")
        } catch (ex: SecurityException) {
            android.util.Log.w("AlertRepository", "Caller needs SCHEDULE_EXACT_ALARM or user exemption: ${ex.message}")
            // Intentar abrir ajustes si no lo hicimos antes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    val intentSettings = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intentSettings)
                } catch (_: Exception) {
                    android.util.Log.w("AlertRepository", "No se pudo abrir ajustes de alarmas exactas al manejar SecurityException")
                    showRequestExactAlarmNotification(context)
                }
            }
        } catch (_: Exception) {
            android.util.Log.w("AlertRepository", "Error programando alarma")
        }
    }

    // Mostrar notificación que permita al usuario abrir la pantalla ACTION_REQUEST_SCHEDULE_EXACT_ALARM
    private fun showRequestExactAlarmNotification(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                android.util.Log.w("AlertRepository", "No se puede mostrar notificación para pedir alarmas exactas: falta POST_NOTIFICATIONS")
                showOpenAppSettingsNotification(context)
                return
            }

            val intentSettings = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            var flags = PendingIntent.FLAG_UPDATE_CURRENT
            flags = flags or PendingIntent.FLAG_IMMUTABLE
            val pending = PendingIntent.getActivity(context, 0, intentSettings, flags)

            val channelId = ReminderReceiver.CHANNEL_ID
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Permitir alarmas exactas")
                .setContentText("Toca para abrir la pantalla donde puedes permitir alarmas exactas para esta app")
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(9998, notification)
        } catch (_: Exception) {
            android.util.Log.w("AlertRepository", "No se pudo mostrar notificación para solicitar alarmas exactas")
        }
    }

    // Mostrar notificación que abra los detalles de la app (ajustes) como fallback
    private fun showOpenAppSettingsNotification(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                android.util.Log.w("AlertRepository", "No se puede mostrar notificación para abrir ajustes: falta POST_NOTIFICATIONS")
                return
            }

            val intentSettings = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            var flags = PendingIntent.FLAG_UPDATE_CURRENT
            flags = flags or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(context, 0, intentSettings, flags)

            val channelId = ReminderReceiver.CHANNEL_ID
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Abrir ajustes de la app")
                .setContentText("Toca para abrir los ajustes de la app y permitir permisos necesarios")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(9999, notification)
        } catch (_: Exception) {
            android.util.Log.w("AlertRepository", "No se pudo mostrar notificación para abrir ajustes")
        }
    }

    private fun cancelAlarm(context: Context, requestCode: Int) {
        try {
            val intent = Intent(context, ReminderReceiver::class.java)
            var flags = PendingIntent.FLAG_NO_CREATE
            flags = flags or PendingIntent.FLAG_IMMUTABLE
            val pending = PendingIntent.getBroadcast(context, requestCode, intent, flags)
            if (pending != null) {
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.cancel(pending)
                pending.cancel()
                android.util.Log.d("AlertRepository", "Alarma cancelada (requestCode=$requestCode)")
            }
        } catch (_: Exception) {
            android.util.Log.w("AlertRepository", "Error cancelando alarma")
        }
    }

    // Helper para obtener nombre comercial del medicamento por id leyendo assets/Medicamento.json
    private data class MedicamentoAsset(val id: Int, val codigo_medicina: String?, val nombre_comercial: String?, val descripcion: String?)

    private fun getMedicamentoNombreFromAssets(context: Context, idMedicamento: Int): String {
        try {
            // Intentar leer assets/Medicamento.json
            context.assets.open("Medicamento.json").use { input ->
                val text = input.bufferedReader().use { it.readText() }
                val listType = object : TypeToken<List<MedicamentoAsset>>() {}.type
                val meds: List<MedicamentoAsset> = Gson().fromJson(text, listType)
                val found = meds.firstOrNull { it.id == idMedicamento }
                if (found != null && !found.nombre_comercial.isNullOrEmpty()) return found.nombre_comercial
            }
        } catch (_: Exception) {
            android.util.Log.w("AlertRepository", "No se pudo leer Medicamento.json desde assets")
        }
        return "Medicamento #$idMedicamento"
    }

}
