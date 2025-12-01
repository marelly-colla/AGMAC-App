package com.example.agmac.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agmac.data.datasource.PacienteDataSource
import com.example.agmac.data.model.Paciente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PacientesViewModel(application: Application) : AndroidViewModel(application) {

    private val dataSource = PacienteDataSource(application.applicationContext)

    private val _pacientes = MutableStateFlow<List<Paciente>>(emptyList())
    val pacientes: StateFlow<List<Paciente>> = _pacientes

    init {
        loadPacientes()
    }

    fun loadPacientes() {
        viewModelScope.launch {
            _pacientes.value = dataSource.getPacientes()
        }
    }

    fun addPaciente(paciente: Paciente) {
        viewModelScope.launch {
            dataSource.addPaciente(paciente)
            loadPacientes()
        }
    }

    fun deletePaciente(id: Int) {
        viewModelScope.launch {
            dataSource.deletePaciente(id)
            loadPacientes()
        }
    }
}