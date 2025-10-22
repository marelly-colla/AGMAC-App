package com.example.agmac.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//importar Theme y boton de navegación
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.SectionTitle

// --- Data classes ---
data class Medicamento(val nombre: String, val hora: String)
data class Recordatorio(val nombre: String, val tiempo: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeScreen() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "AGMAC",
                            fontSize = 20.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Ajustes */ }) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Ajustes"
                            )
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(selected = "Inicio") },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { /* TODO: agregar medicamento */ },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = "Agregar") },
                    text = { Text("Agregar") }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- Medicamentos ---
                item {
                    SectionTitle("Medicamentos")
                    val medicamentos = listOf(
                        Medicamento("Aspirina", "8:00 AM"),
                        Medicamento("Ibuprofeno", "12:00 PM"),
                        Medicamento("Paracetamol", "6:00 PM")
                    )
                    medicamentos.forEach { MedItem(it) }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- Próximos Recordatorios ---
                item {
                    SectionTitle("Próximos Recordatorios")
                    val recordatorios = listOf(
                        Recordatorio("Aspirina", "En 30 minutos")
                    )
                    recordatorios.forEach { RecordatorioItem(it) }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- Cumplimiento Diario ---
                item {
                    SectionTitle("Cumplimiento Diario")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("75% completado", fontSize = 16.sp)
                                Text("🟩", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.75f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp)) // espacio final
                }
            }
        }
    }
}

@Composable
fun MedItem(med: Medicamento) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { /* TODO: Detalle medicamento */ },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(med.nombre, fontSize = 16.sp)
                Text(med.hora, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun RecordatorioItem(rec: Recordatorio) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(rec.nombre, fontSize = 16.sp)
                Text(rec.tiempo, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}