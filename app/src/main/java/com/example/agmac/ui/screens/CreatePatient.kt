package com.example.agmac.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.agmac.data.model.EstadoPaciente
import com.example.agmac.data.model.Paciente
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.ui.viewmodel.PacientesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePatientScreen(
    navController: NavHostController,
    viewModel: PacientesViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf(EstadoPaciente.AL_DIA) }
    var errorMsg by remember { mutableStateOf("") }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Crear Paciente") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    label = { Text("URL de Foto") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Estado del paciente")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoPaciente.entries.forEach { e ->
                        Button(
                            onClick = { estado = e },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (estado == e) e.color else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(e.texto)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        errorMsg = ""
                        if (nombre.isBlank()) {
                            errorMsg = "El nombre es obligatorio"
                        } else {
                            // Crear id automático
                            val nuevoId = (viewModel.pacientes.value.maxOfOrNull { it.id } ?: 0) + 1
                            val nuevoPaciente = Paciente(
                                id = nuevoId,
                                nombre = nombre,
                                estado = estado,
                                fotoUrl = fotoUrl.ifBlank { "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png" }
                            )
                            viewModel.addPaciente(nuevoPaciente)
                            navController.popBackStack() // regresar a lista
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar")
                }

                if (errorMsg.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
