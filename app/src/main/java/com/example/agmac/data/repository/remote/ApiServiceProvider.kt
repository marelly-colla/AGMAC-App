package com.example.agmac.data.repository.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiServiceProvider {
    private const val BASE_URL = "https://gb72d0482c8537e-upchmovilapp2025.adb.us-phoenix-1.oraclecloudapps.com/ords/admin/"

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}

