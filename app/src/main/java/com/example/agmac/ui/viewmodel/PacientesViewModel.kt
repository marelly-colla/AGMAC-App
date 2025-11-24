package com.example.agmac.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import com.example.agmac.data.model.Paciente
import com.example.agmac.data.model.EstadoPaciente

class PacientesViewModel : ViewModel() {

    private val _pacientes = MutableStateFlow(
        listOf(
            Paciente(
                1,
                "Ana García",
                EstadoPaciente.AL_DIA,
                "https://previews.123rf.com/images/mrswilkins/mrswilkins1705/mrswilkins170500015/80934398-profile-picture-illustration-woman-vector.jpg"
            ),
            Paciente(
                2,
                "Carlos Rodríguez",
                EstadoPaciente.REQUIERE,
                "https://static.vecteezy.com/ti/vetor-gratis/p1/9952572-foto-de-perfil-masculino-vetor.jpg"
            ),
            Paciente(
                3,
                "Beatriz López",
                EstadoPaciente.OMISION,
                "https://previews.123rf.com/images/mrswilkins/mrswilkins1705/mrswilkins170500015/80934398-profile-picture-illustration-woman-vector.jpg"
            )
        )
    )
    val pacientes = _pacientes
}
