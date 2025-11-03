package com.example.agmac.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.SectionTitle
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.data.model.Medicamento
import com.example.agmac.ui.viewmodel.PatientMedicationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMedicationScreen(navController: NavHostController) {
    val viewModel: PatientMedicationViewModel = viewModel()
    val alertas by viewModel.alertas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val vmError by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    // --- Panel de creación de alerta ---
    var medicamentoNombre by remember { mutableStateOf("") }
    var medicamentoId by remember { mutableStateOf<Int?>(null) }
    var dosis by remember { mutableStateOf("") }
    var fechaInicioStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var numeroDeDias by remember { mutableStateOf(1) }
    var horasDelDia by remember { mutableStateOf(listOf<String>()) }
    var showAutocomplete by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    // Leer medicamentos desde Medicamento.json
    val medicamentos: List<Medicamento> = remember {
        val file = context.assets.open("Medicamento.json")
        val json = file.bufferedReader().use { it.readText() }
        Gson().fromJson(json, object : TypeToken<List<Medicamento>>() {}.type)
    }

    // Filtrar para autocompletar
    val opcionesAutocomplete = medicamentos.filter {
        it.nombre_comercial.contains(medicamentoNombre, ignoreCase = true) && medicamentoNombre.isNotBlank()
    }

    LaunchedEffect(Unit) {
        viewModel.loadAlertas()
    }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Medicación", style = MaterialTheme.typography.titleLarge) }
                )
            },
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Mostrar error remoto si existe
                if (vmError != null) {
                    Text(
                        text = vmError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                // Panel de creación de alerta
                SectionTitle("Agregar nueva alerta")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = medicamentoNombre,
                    onValueChange = {
                        medicamentoNombre = it
                        showAutocomplete = true
                        medicamentoId = null
                    },
                    label = { Text("Medicamento") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors()
                )
                if (showAutocomplete && opcionesAutocomplete.isNotEmpty()) {
                    opcionesAutocomplete.take(5).forEach { opcion ->
                        Text(
                            text = opcion.nombre_comercial,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                                .clickable {
                                    medicamentoNombre = opcion.nombre_comercial
                                    medicamentoId = opcion.id
                                    showAutocomplete = false
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = dosis,
                    onValueChange = { dosis = it },
                    label = { Text("Dosis") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = fechaInicioStr,
                    onValueChange = { fechaInicioStr = it },
                    label = { Text("Fecha inicio (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Selector de hora
                Text("Hora (HH:mm)", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            // Abrir TimePickerDialog (mostrar AM/PM: is24Hour = false)
                            val now = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    // Guardamos internamente en HH:mm para backend
                                    val horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                                    if (!horasDelDia.contains(horaSeleccionada)) {
                                        horasDelDia = horasDelDia + horaSeleccionada
                                    }
                                },
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                false // usar AM/PM visualmente
                            ).show()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mostrar etiquetas legibles (ej. 1:00 PM) para la UI
                    val displayLabels = remember(horasDelDia) {
                        horasDelDia.map { hhmm ->
                            try {
                                val parser = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val date = parser.parse(hhmm)
                                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                if (date != null) formatter.format(date) else hhmm
                            } catch (e: Exception) {
                                android.util.Log.w("PatientMedication", "Error formateando hora: $hhmm", e)
                                hhmm
                            }
                        }
                    }

                    Text(
                        text = if (displayLabels.isEmpty()) "Selecciona hora" else displayLabels.joinToString(", "),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(Icons.Outlined.AccessTime, contentDescription = "Seleccionar hora")
                }

                // Chips de horas seleccionadas
                if (horasDelDia.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(horasDelDia) { index, hora ->
                            val label = try {
                                val parser = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val d = parser.parse(hora)
                                if (d != null) SimpleDateFormat("hh:mm a", Locale.getDefault()).format(d) else hora
                            } catch (e: Exception) {
                                android.util.Log.w("PatientMedication", "Error formateando hora para chip: $hora", e)
                                hora
                            }
                            AssistChip(
                                onClick = {
                                    // Remover hora al pulsar el chip
                                    horasDelDia = horasDelDia.filterIndexed { i, _ -> i != index }
                                },
                                label = { Text(label) },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Repetir por ", fontSize = 16.sp)
                    TextField(
                        value = numeroDeDias.toString(),
                        onValueChange = {
                            numeroDeDias = it.toIntOrNull() ?: 1
                        },
                        label = { Text("días") },
                        modifier = Modifier.width(80.dp)
                    )
                    Text(" días", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        errorMsg = ""
                        if (medicamentoId == null) {
                            errorMsg = "Selecciona un medicamento válido."
                        } else if (dosis.isBlank()) {
                            errorMsg = "Ingresa la dosis."
                        } else if (horasDelDia.isEmpty()) {
                            errorMsg = "Agrega al menos una hora."
                        } else if (numeroDeDias < 1) {
                            errorMsg = "El número de días debe ser mayor a 0."
                        } else {
                            viewModel.createAlertSchedule(
                                medicamentoId!!,
                                dosis,
                                fechaInicioStr,
                                numeroDeDias,
                                horasDelDia
                            )
                            medicamentoNombre = ""
                            medicamentoId = null
                            dosis = ""
                            horasDelDia = emptyList()
                            numeroDeDias = 1
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear alerta")
                }
                if (errorMsg.isNotBlank()) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Próximas dosis")
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    if (alertas.isEmpty()) {
                        Text("No hay alertas programadas.")
                    } else {
                        alertas.forEach { alerta ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${alerta.dosis} · ${alerta.hora_programada}",
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            text = "Estado: ${alerta.estado}",
                                            fontSize = 14.sp
                                        )
                                    }
                                    if (alerta.estado == "PENDIENTE") {
                                        Button(
                                            onClick = {
                                                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
                                                viewModel.markAlertAsTaken(alerta.id_alerta, now)
                                            },
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text("Tomar")
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteAlert(alerta.id_alerta) }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Historial rápido")
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Últimas tomas: 3/4 hoy", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Adherencia: 75%")
                            Text("Ver detalles", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
