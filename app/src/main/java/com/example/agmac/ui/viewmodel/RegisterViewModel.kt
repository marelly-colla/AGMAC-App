package com.example.agmac.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agmac.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = UserRepository(application)
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

            val result = repo.register(name, email, password)
            if (result) {
                _uiState.value = RegisterUiState(success = true)
                Log.d("RegisterTest", "Usuario registrado correctamente: $email")
            } else {
                _uiState.value = RegisterUiState(errorMessage = "Error: el usuario ya existe o los datos son inválidos")
            }
        }
    }
}