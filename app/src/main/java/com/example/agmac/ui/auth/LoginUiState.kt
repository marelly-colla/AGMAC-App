package com.example.agmac.ui.auth

data class LoginUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)
