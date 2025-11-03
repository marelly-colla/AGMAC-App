package com.example.agmac.data.repository

import com.example.agmac.data.model.User

interface AuthRepository {
    suspend fun registerUser(name: String, email: String, password: String): Boolean
    suspend fun loginUser(email: String, password: String): User?
}
