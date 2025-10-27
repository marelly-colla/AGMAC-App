package com.example.agmac.data.datasource

import android.content.Context
import android.util.Log
import com.example.agmac.data.model.User
import com.google.gson.GsonBuilder
import java.io.File

class UserLocalDataSource(private val context: Context) {

    private val fileName = "users.json"
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun getUsers(): MutableList<User> {
        val file = File(context.filesDir, fileName)

        // Si no existe el archivo, copia el users.json desde assets (inicialización)
        if (!file.exists()) {
            context.assets.open(fileName).use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }

        val json = file.readText()
        return gson.fromJson(json, Array<User>::class.java)?.toMutableList() ?: mutableListOf()
    }

    fun saveUsers(users: List<User>) {
        val file = File(context.filesDir, fileName)
        file.writeText(gson.toJson(users))
        Log.d("UserLocalDataSource", "Archivo JSON en: ${File(context.filesDir, "users.json").absolutePath}")

    }
}
