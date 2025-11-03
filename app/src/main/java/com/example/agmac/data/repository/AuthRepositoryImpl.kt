package com.example.agmac.data.repository

import com.example.agmac.data.model.User
import com.example.agmac.data.repository.remote.AuthApi
import com.example.agmac.data.repository.remote.LoginResponse
import retrofit2.Response

class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository {
    override suspend fun registerUser(name: String, email: String, password: String): Boolean {
        val body = mapOf("name" to name, "email" to email, "password" to password)
        val response: Response<Void> = api.registerUser(body)
        return response.isSuccessful
    }

    override suspend fun loginUser(email: String, password: String): User? {
        val query = "{" + "\"email\":\"$email\", \"password\":\"$password\"}" // Formato requerido por el endpoint
        val response: Response<LoginResponse> = api.loginUser(query)
        return if (response.isSuccessful && response.body()?.items?.isNotEmpty() == true) {
            response.body()?.items?.first()
        } else {
            null
        }
    }
}
