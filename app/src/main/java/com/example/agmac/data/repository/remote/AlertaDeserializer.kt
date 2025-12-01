package com.example.agmac.data.repository.remote

import com.example.agmac.data.model.Alerta
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Deserializador tolerante para Alerta: acepta varias formas de nombre de campo
 * que el backend pueda devolver (id_alerta, id, idAlerta, etc.).
 */
class AlertaDeserializer : JsonDeserializer<Alerta> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Alerta {
        val obj = json?.asJsonObject

        fun getInt(vararg names: String): Int {
            if (obj == null) return 0
            for (name in names) {
                if (obj.has(name) && !obj.get(name).isJsonNull) {
                    try {
                        val el = obj.get(name)
                        if (el.isJsonPrimitive) {
                            val prim = el.asJsonPrimitive
                            if (prim.isNumber) return prim.asInt
                            val s = prim.asString
                            return s.toIntOrNull() ?: 0
                        }
                        return 0
                    } catch (_: Exception) {
                    }
                }
            }
            return 0
        }

        fun getString(vararg names: String): String {
            if (obj == null) return ""
            for (name in names) {
                if (obj.has(name) && !obj.get(name).isJsonNull) {
                    try {
                        return obj.get(name).asString
                    } catch (_: Exception) {
                    }
                }
            }
            return ""
        }

        val idAlerta = getInt("id_alerta", "idAlerta", "idAlert", "id")
        val idPaciente = getInt("id_paciente", "idPaciente", "id_paciente")
        val idMedicamento = getInt("id_medicamento", "idMedicamento", "id_medicamento", "id_medicina")
        val dosis = getString("dosis", "dosage", "cantidad")
        val horaProgramada = getString("hora_programada", "horaProgramada", "scheduled_time", "hora_programada_utc")
        val estado = getString("estado", "status")
        val horaConfirmacion = getString("hora_confirmacion", "horaConfirmacion", "confirmed_at")

        return Alerta(
            id_alerta = idAlerta,
            id_paciente = idPaciente,
            id_medicamento = idMedicamento,
            dosis = dosis,
            hora_programada = horaProgramada,
            estado = estado,
            hora_confirmacion = if (horaConfirmacion.isBlank()) null else horaConfirmacion
        )
    }
}
