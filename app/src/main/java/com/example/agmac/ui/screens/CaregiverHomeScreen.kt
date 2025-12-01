package com.example.agmac.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

//importar Theme y boton de navegación
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.SectionTitle
import androidx.navigation.NavHostController
import com.example.agmac.data.model.EstadoPaciente
import com.example.agmac.ui.viewmodel.PacientesViewModel
import com.example.agmac.ui.components.PacienteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverHomeScreen(
    navController: NavHostController,
    viewModel: PacientesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val pacientes by viewModel.pacientes.collectAsState(initial = emptyList())

    // Filtrar los pacientes que no están al día
    val pacientesEnFalta = pacientes.filter { it.estado != EstadoPaciente.AL_DIA }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "AGMAC",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Notificaciones */ }) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                    )
                )
            },
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    SectionTitle("Mis Pacientes")
                }
                items(pacientesEnFalta) { paciente ->
                    PacienteCard(
                        paciente = paciente,
                        navController = navController,
                        onDelete = { id -> viewModel.deletePaciente(id)}
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    SectionTitle("Alarmas recientes")
                }
                items(
                    listOf(
                        Alerta("Elena Ramírez", "Medicamento omitido", "Hace 2 horas"),
                        Alerta("Carlos Mendoza", "Medicamento omitido", "Hace 4 horas")
                    )
                ) { alerta ->
                    AlertaItem(alerta)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

data class Alerta(val paciente: String, val mensaje: String, val tiempo: String)

@Composable
fun AlertaItem(alerta: Alerta) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alerta.paciente,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    alerta.mensaje,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                alerta.tiempo,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}