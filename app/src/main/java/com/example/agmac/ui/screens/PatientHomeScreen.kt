package com.example.agmac.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

//importar Theme y boton de navegación
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.SectionTitle
import androidx.navigation.NavHostController
import com.example.agmac.ui.viewmodel.PatientMedicationViewModel
import com.example.agmac.data.model.Medicamento
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeScreen(navController: NavHostController) {
    val viewModel: PatientMedicationViewModel = viewModel()
    val alertas by viewModel.alertas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // Leer medicamentos desde Medicamento.json
    val medicamentos: List<Medicamento> = remember {
        try {
            val file = context.assets.open("Medicamento.json")
            val json = file.bufferedReader().use { it.readText() }
            Gson().fromJson(json, object : TypeToken<List<Medicamento>>() {}.type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAlertas()
    }

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
            bottomBar = { BottomNavigationBar(navController) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("patient_medication") },
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
                // --- Medicamentos (dinámico desde alertas) ---
                item {
                    SectionTitle("Medicamentos")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        val pendientes = alertas.filter { it.estado == "PENDIENTE" }
                        if (pendientes.isEmpty()) {
                            Text("No hay medicamentos pendientes.", modifier = Modifier.padding(8.dp))
                        } else {
                            pendientes.forEach { alerta ->
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
                                            Icon(Icons.Outlined.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            val medName = remember(alerta.id_medicamento) {
                                                medicamentos.firstOrNull { it.id == alerta.id_medicamento }?.nombre_comercial
                                                    ?: "Medicamento #${alerta.id_medicamento}"
                                            }

                                            val timeLabel = try {
                                                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                                                parser.timeZone = TimeZone.getTimeZone("UTC")
                                                val d = parser.parse(alerta.hora_programada)
                                                if (d != null) {
                                                    val tf = DateFormat.getTimeInstance(DateFormat.LONG, Locale.getDefault())
                                                    tf.format(d)
                                                } else alerta.hora_programada
                                            } catch (_: Exception) {
                                                alerta.hora_programada
                                            }

                                            Text(medName, fontSize = 16.sp)
                                            Text(timeLabel, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                            Text(alerta.dosis, fontSize = 14.sp)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Botón tomar (llama al ViewModel)
                                        Button(onClick = {
                                            // Enviar confirmación en UTC ISO con 'Z'
                                            val sdfUtc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                                                timeZone = TimeZone.getTimeZone("UTC")
                                            }
                                            val nowUtc = sdfUtc.format(Date())
                                            viewModel.markAlertAsTaken(alerta.id_alerta, nowUtc)
                                        }) {
                                            Text("Tomar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- Próximos Recordatorios: mantener sección simple ---
                item {
                    SectionTitle("Próximos Recordatorios")
                    Spacer(modifier = Modifier.height(8.dp))
                    // Mostramos una vista simplificada de la primera alerta pendiente
                    val primera = alertas.filter { it.estado == "PENDIENTE" }.minByOrNull { it.hora_programada }
                    if (primera != null) {
                        val medName = medicamentos.firstOrNull { it.id == primera.id_medicamento }?.nombre_comercial ?: "Medicamento #${primera.id_medicamento}"
                        RecordatorioItem(Recordatorio(medName, "Proximo: ${primera.hora_programada}"))
                    } else {
                        Text("No hay recordatorios próximos.", modifier = Modifier.padding(8.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- Cumplimiento Diario (se deja igual) ---
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

// Data class local para recordatorio rápido
data class Recordatorio(val nombre: String, val tiempo: String)
