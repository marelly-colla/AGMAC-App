package com.example.agmac.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agmac.data.SessionManager
import com.example.agmac.data.repository.AuthRepositoryImpl
import com.example.agmac.data.repository.remote.ApiServiceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AuthRepositoryImpl(ApiServiceProvider.authApi)
    private val appContext = getApplication<Application>().applicationContext
    data class RegisterUiState(
        val isLoading: Boolean = false,
        val success: Boolean = false,
        val errorMessage: String? = null
    )
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun registrar(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)

            val result = repo.registerUser(name, email, password)
            if (result) {
                // Login automático para obtener el id
                val user = repo.loginUser(email, password)
                if (user != null) {
                    SessionManager.saveUserId(appContext, user.id)
                }
                _uiState.value = RegisterUiState(success = true)
                Log.d("RegisterTest", "Usuario registrado correctamente: $email")
            } else {
                _uiState.value = RegisterUiState(errorMessage = "Error: el usuario ya existe o los datos son inválidos")
            }
        }
    }
}