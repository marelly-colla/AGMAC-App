package com.example.agmac.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agmac.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = UserRepository(application)
    data class LoginUiState(
        val isLoading: Boolean = false,
        val success: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repo.login(email, password)
            if (result) {
                _uiState.value = LoginUiState(success = true)
            } else {
                _uiState.value = LoginUiState(errorMessage = "Credenciales incorrectas")
            }
        }
    }
}