package com.example.agmac.data.repository.remote

import com.example.agmac.data.model.User
import com.example.agmac.data.repository.remote.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("app_usuarios/")
    suspend fun registerUser(@Body user: Map<String, String>): Response<Void>

    @GET("app_usuarios/")
    suspend fun loginUser(@Query("q") query: String): Response<LoginResponse>
}

