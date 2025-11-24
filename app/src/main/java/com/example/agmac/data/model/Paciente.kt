package com.example.agmac.data.model

import androidx.compose.ui.graphics.Color

data class Paciente(
    val id: Int,
    val nombre: String,
    val estado: EstadoPaciente,
    val fotoUrl: String
)

enum class EstadoPaciente(val color: Color, val texto: String) {
    AL_DIA(Color(0xFF22C55E), "Al día"),
    REQUIERE(Color(0xFFFF9800), "Requiere atención"),
    OMISION(Color(0xFFEF4444), "Dosis omitida")
}
