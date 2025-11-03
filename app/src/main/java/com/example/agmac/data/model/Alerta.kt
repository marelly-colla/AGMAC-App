package com.example.agmac.data.model

data class Alerta(
    val id_alerta: Int,
    val id_paciente: Int,
    val id_medicamento: Int,
    val dosis: String,
    val hora_programada: String,
    val estado: String,
    val hora_confirmacion: String?
)

