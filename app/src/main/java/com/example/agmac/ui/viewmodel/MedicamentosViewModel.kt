package com.example.agmac.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.agmac.data.model.MedicamentoEstado
import com.example.agmac.data.model.MedicamentoHoyConId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MedicamentosViewModel : ViewModel() {

    private val _medicamentos = MutableStateFlow(
        listOf(
            MedicamentoHoyConId(
                id = 1,
                nombre = "Paracetamol",
                dosis = "500 mg",
                hora = "08:00 AM",
                estado = MedicamentoEstado.PENDIENTE
            ),
            MedicamentoHoyConId(
                id = 2,
                nombre = "Ibuprofeno",
                dosis = "400 mg",
                hora = "02:00 PM",
                estado = MedicamentoEstado.TOMADO
            ),
            MedicamentoHoyConId(
                id = 3,
                nombre = "Amoxicilina",
                dosis = "1 cápsula",
                hora = "08:00 PM",
                estado = MedicamentoEstado.PENDIENTE
            )
        )
    )

    val medicamentos: StateFlow<List<MedicamentoHoyConId>> = _medicamentos
}
