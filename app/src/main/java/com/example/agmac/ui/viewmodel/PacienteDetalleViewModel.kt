package com.example.agmac.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import com.example.agmac.data.model.PacienteDetalle
import com.example.agmac.data.model.MedicamentoHoy
import com.example.agmac.data.model.EventoHistorial
import com.example.agmac.data.model.MedicamentoEstado
import com.example.agmac.data.model.EventoEstado

class PacienteDetalleViewModel : ViewModel() {

    private val pacientesSimulados = listOf(
        PacienteDetalle(
            id = 1,
            nombre = "Ana García",
            fotoUrl = "https://previews.123rf.com/images/mrswilkins/mrswilkins1705/mrswilkins170500015/80934398-profile-picture-illustration-woman-vector.jpg",
            rol = "Paciente"
        ),
        PacienteDetalle(
            id = 2,
            nombre = "Carlos Rodríguez",
            fotoUrl = "https://static.vecteezy.com/ti/vetor-gratis/p1/9952572-foto-de-perfil-masculino-vetor.jpg",
            rol = "Paciente"
        ),
        PacienteDetalle(
            id = 3,
            nombre = "Beatriz López",
            fotoUrl = "https://previews.123rf.com/images/mrswilkins/mrswilkins1705/mrswilkins170500015/80934398-profile-picture-illustration-woman-vector.jpg",
            rol = "Paciente"
        )
    )
    private val _paciente = MutableStateFlow<PacienteDetalle?>(null)
    val paciente = _paciente

    private val _medicamentosHoy = MutableStateFlow(
        listOf(
            MedicamentoHoy("Ibuprofeno", "500 mg", "08:00 AM", MedicamentoEstado.PENDIENTE),
            MedicamentoHoy("Paracetamol", "1000 mg", "14:00 PM", MedicamentoEstado.TOMADO),
            MedicamentoHoy("Amlodipino", "5 mg", "22:00 PM", MedicamentoEstado.PENDIENTE)
        )
    )
    val medicamentosHoy = _medicamentosHoy

    private val _historial = MutableStateFlow(
        listOf(
            EventoHistorial("Hoy", "Paracetamol", "13:58 PM", EventoEstado.TOMADO),
            EventoHistorial("Ayer", "Ibuprofeno", "08:05 AM", EventoEstado.TOMADO),
            EventoHistorial("Ayer", "Paracetamol", "14:01 PM", EventoEstado.TOMADO),
            EventoHistorial("Ayer", "Amlodipino", "", EventoEstado.OMITIDO)
        )
    )
    val historial = _historial
    fun loadPacienteById(id: Int) {
        // Aquí podrías hacer una búsqueda en base de datos o API
        // Por ahora solo simulamos con un paciente fijo
        _paciente.value = pacientesSimulados.find { it.id == id }
    }

}