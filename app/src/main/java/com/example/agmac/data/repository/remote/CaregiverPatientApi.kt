package com.example.agmac.data.repository.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

// Modelo de la relación cuidador-paciente
data class CaregiverRelationRequest(
    val id_cuidador: Int,
    val id_paciente: Int,
    val estado_solicitud: String,
    // id opcional: si la tabla lo autogenera se deja null; si ORDS exige un valor podemos generarlo luego
    val id: Int? = null
)

// Respuesta genérica (ORDS usualmente devuelve items)
data class CaregiverRelation(
    val id: Int?,
    val id_cuidador: Int,
    val id_paciente: Int,
    val estado_solicitud: String
)

data class CaregiverRelationResponse(val items: List<CaregiverRelation>)

interface CaregiverPatientApi {
    @POST("cuidador_paciente/")
    suspend fun createRelation(@Body body: CaregiverRelationRequest): Response<CaregiverRelationResponse>

    // Nuevo: buscar relaciones por filtro JSON
    @GET("cuidador_paciente/")
    suspend fun findRelations(@Query("q") query: String): Response<CaregiverRelationResponse>

    // Opcional: obtener relaciones ordenadas (por ejemplo para buscar max id)
    @GET("cuidador_paciente/")
    suspend fun listRelationsOrdered(
        @Query("q") query: String?,
        @Query("orderBy") orderBy: String
    ): Response<CaregiverRelationResponse>
}
