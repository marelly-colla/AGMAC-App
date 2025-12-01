package com.example.agmac.data.repository.remote

import com.example.agmac.data.model.Alerta
import retrofit2.Response
import retrofit2.http.*

data class CreateAlertaRequest(
    val id_paciente: Int,
    val id_medicamento: Int,
    val dosis: String,
    val hora_programada: String,
    val estado: String
)

// Para PUT, el backend suele esperar el objeto completo. Hacemos id_paciente e id_medicamento no nulos
// y también incluimos hora_programada que es parte del objeto alerta.
data class UpdateAlertaRequest(
    val estado: String,
    val hora_confirmacion: String? = null,
    val id_paciente: Int,
    val id_medicamento: Int,
    val hora_programada: String
)

interface AlertApi {
    @GET("alertas_programadas/")
    suspend fun getAlertas(@Query("q") query: String, @Query("orderBy") orderBy: String? = null): Response<AlertasResponse>

    @POST("alertas_programadas/")
    suspend fun createAlerta(@Body body: CreateAlertaRequest): Response<Void>

    @PUT("alertas_programadas/{id_alerta}")
    suspend fun updateAlerta(@Path("id_alerta") idAlerta: Int, @Body body: UpdateAlertaRequest): Response<Void>

    @DELETE("alertas_programadas/{id_alerta}")
    suspend fun deleteAlerta(@Path("id_alerta") idAlerta: Int): Response<Void>
}

data class AlertasResponse(val items: List<Alerta>)
