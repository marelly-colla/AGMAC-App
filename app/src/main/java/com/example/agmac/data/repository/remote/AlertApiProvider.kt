package com.example.agmac.data.repository.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder

object AlertApiProvider {
    private const val BASE_URL = "https://gb72d0482c8537e-upchmovilapp2025.adb.us-phoenix-1.oraclecloudapps.com/ords/admin/"

    val alertApi: AlertApi by lazy {
        // Crear un Gson personalizado que use nuestro deserializador tolerante para Alerta
        val gson = GsonBuilder()
            .registerTypeAdapter(com.example.agmac.data.model.Alerta::class.java, AlertaDeserializer())
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AlertApi::class.java)
    }
}
