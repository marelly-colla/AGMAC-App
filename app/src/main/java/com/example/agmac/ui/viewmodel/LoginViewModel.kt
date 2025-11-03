package com.example.agmac.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agmac.data.SessionManager
import com.example.agmac.data.repository.AuthRepositoryImpl
import com.example.agmac.data.repository.remote.ApiServiceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AuthRepositoryImpl(ApiServiceProvider.authApi)
    private val appContext = getApplication<Application>().applicationContext
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

            val user = repo.loginUser(email, password)
            if (user != null) {
                SessionManager.saveUserId(appContext, user.id)
                _uiState.value = LoginUiState(success = true)
            } else {
                _uiState.value = LoginUiState(errorMessage = "Credenciales incorrectas")
            }
        }
    }
}