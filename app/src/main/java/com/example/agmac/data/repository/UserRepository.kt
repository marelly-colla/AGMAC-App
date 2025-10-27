package com.example.agmac.data.repository

import android.content.Context
import com.example.agmac.data.datasource.UserLocalDataSource
import com.example.agmac.data.model.User

class UserRepository(context: Context) {

    private val dataSource = UserLocalDataSource(context)

    fun register(name: String, email: String, password: String): Boolean {
        val users = dataSource.getUsers()

        // Evitar duplicados
        if (users.any { it.email == email }) {
            return false
        }

        val nuevo = User(
            id = (users.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            email = email,
            password = password
        )
        users.add(nuevo)
        dataSource.saveUsers(users)
        return true
    }

    fun login(email: String, password: String): Boolean {
        val users = dataSource.getUsers()
        return users.any { it.email == email && it.password == password }
    }
}
