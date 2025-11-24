package com.example.agmac.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.outlined.ArrowBack

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//importar Theme y boton de navegación
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.MedicamentoCard
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.agmac.ui.viewmodel.PacienteDetalleViewModel
import com.example.agmac.data.model.EventoEstado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacienteDetalleScreen(
    navController: NavHostController,
    pacienteId: Int,
    viewModel: PacienteDetalleViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Cargar paciente al iniciar la pantalla
    LaunchedEffect(pacienteId) {
        viewModel.loadPacienteById(pacienteId)
    }
    val pacienteState = viewModel.paciente.collectAsState()
    val paciente = pacienteState.value
    val medicamentos by viewModel.medicamentosHoy.collectAsState()
    val historial by viewModel.historial.collectAsState()

    paciente?.let { p ->
        // Aquí va tu UI de detalle usando `p` y `medicamentos`
        AppTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Resumen del Paciente",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Atrás")
                            }
                        },
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones")
                            }
                        }
                    )
                },
                bottomBar = { BottomNavigationBar(navController) },
            ) { padding ->

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {

                    // ⭐ PROFILE HEADER
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = paciente.fotoUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(paciente.nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Text(paciente.rol, color = Color.Gray)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // ⭐ MEDICAMENTOS DE HOY
                    item {
                        Text("Tus medicamentos para hoy",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    items(medicamentos) { m ->
                        MedicamentoCard(m)
                        Spacer(Modifier.height(12.dp))
                    }

                    item { Spacer(Modifier.height(24.dp)) }

                    // ⭐ HISTORIAL
                    item {
                        Text(
                            "Historial de los últimos días",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    val grupos = historial.groupBy { it.fechaGrupo }
                    grupos.forEach { (grupo, eventos) ->
                        item {
                            Text(
                                grupo,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(eventos) { e ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val color = if (e.estado == EventoEstado.TOMADO)
                                        Color(0xFF4CAF50)
                                    else Color(0xFFE53935)

                                    Icon(
                                        imageVector = if (e.estado == EventoEstado.TOMADO)
                                            Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                                        contentDescription = null,
                                        tint = color
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(e.nombreMedicamento)
                                }

                                Text(
                                    if (e.estado == EventoEstado.TOMADO) "Tomado a las ${e.hora}"
                                    else "Omitido",
                                    color = if (e.estado == EventoEstado.TOMADO)
                                        Color.Gray else Color(0xFFE53935)
                                )
                            }
                        }

                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }


}
