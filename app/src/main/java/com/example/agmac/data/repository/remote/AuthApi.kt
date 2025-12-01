package com.example.agmac.data.repository.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import com.example.agmac.data.repository.remote.LoginResponse
import com.example.agmac.data.model.User

interface AuthApi {
    @POST("app_usuarios/")
    suspend fun registerUser(@Body user: Map<String, String>): Response<Void>

    @GET("app_usuarios/")
    suspend fun loginUser(@Query("q") query: String): Response<LoginResponse>

    @GET("app_usuarios/")
    suspend fun findUsers(@Query("q") query: String): Response<LoginResponse>

    // Nuevo: obtener usuario por ID (para detalles de pacientes)
    @GET("app_usuarios/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<User>
}
