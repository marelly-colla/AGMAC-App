package com.example.agmac.data.repository

import android.content.Context
import com.example.agmac.data.model.Alerta
import com.example.agmac.data.repository.remote.AlertApiProvider
import com.example.agmac.data.repository.remote.CreateAlertaRequest
import com.example.agmac.data.repository.remote.UpdateAlertaRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object AlertRepository {
    private fun getAlertsFile(context: Context): File = File(context.filesDir, "alertas.json")

    suspend fun loadAlertsFromServer(context: Context, idPaciente: Int): List<Alerta> = withContext(Dispatchers.IO) {
        val api = AlertApiProvider.alertApi
        val query = "{" + "\"id_paciente\":$idPaciente}" // Filtro JSON
        val orderBy = "{" + "\"hora_programada\":\"ASC\"}" // Orden
        val response = api.getAlertas(query, orderBy)
        val alertas = response.body()?.items ?: emptyList()
        saveAlertsToLocal(context, alertas)
        alertas
    }

    suspend fun markAlertAsTaken(context: Context, idAlerta: Int, horaConfirmacion: String) = withContext(Dispatchers.IO) {
        val api = AlertApiProvider.alertApi
        val body = UpdateAlertaRequest(estado = "TOMADO", hora_confirmacion = horaConfirmacion)
        val response = api.updateAlerta(idAlerta, body) // Defino 'response' correctamente
        if (response.isSuccessful) {
            val alertas = loadAlertsFromLocal(context).toMutableList()
            val index = alertas.indexOfFirst { it.id_alerta == idAlerta }
            if (index != -1) {
                alertas[index] = alertas[index].copy(
                    estado = "TOMADO",
                    hora_confirmacion = horaConfirmacion
                )
                saveAlertsToLocal(context, alertas)
            }
        }
    }

    suspend fun deleteAlert(context: Context, idAlerta: Int) = withContext(Dispatchers.IO) { // Elimino parámetro no utilizado 'idPaciente'
        val api = AlertApiProvider.alertApi
        val response = api.deleteAlerta(idAlerta)
        if (response.isSuccessful) {
            val alertas = loadAlertsFromLocal(context).filter { it.id_alerta != idAlerta }
            saveAlertsToLocal(context, alertas)
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
        } catch (e: Exception) {
            android.util.Log.e("AlertRepository", "Fecha de inicio inválida: $fechaInicioStr", e)
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
                }
            } catch (e: Exception) {
                android.util.Log.e("AlertRepository", "Excepción creando alerta: $fechaHora", e)
                allOk = false
            }
        }

        try {
            loadAlertsFromServer(context, idPaciente)
        } catch (e: Exception) {
            android.util.Log.e("AlertRepository", "Error al cargar alertas desde servidor después de crear", e)
        }

        return@withContext allOk
    }

    fun saveAlertsToLocal(context: Context, alertas: List<Alerta>) {
        val file = getAlertsFile(context)
        file.writeText(Gson().toJson(alertas))
    }

    fun loadAlertsFromLocal(context: Context): List<Alerta> {
        val file = getAlertsFile(context)
        return if (file.exists()) {
            Gson().fromJson(file.readText(), object : TypeToken<List<Alerta>>() {}.type)
        } else {
            emptyList()
        }
    }
}
