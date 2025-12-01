package com.example.agmac.data.model

data class PacienteDetalle(
    val id: Int,
    val nombre: String,
    val fotoUrl: String,
    val rol: String
)
data class MedicamentoHoyConId(
    val id: Int,
    val nombre: String,
    val dosis: String,
    val hora: String,
    val estado: MedicamentoEstado
)

data class MedicamentoHoy(
    val nombre: String,
    val dosis: String,
    val hora: String,
    val estado: MedicamentoEstado
)

enum class MedicamentoEstado { PENDIENTE, TOMADO }

data class EventoHistorial(
    val fechaGrupo: String,   // "Hoy", "Ayer"
    val nombreMedicamento: String,
    val hora: String,
    val estado: EventoEstado
)

enum class EventoEstado { TOMADO, OMITIDO }
