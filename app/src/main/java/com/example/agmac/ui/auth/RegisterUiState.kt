package com.example.agmac.ui.auth

data class RegisterUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)
