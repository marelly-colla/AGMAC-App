package com.example.agmac.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agmac.R
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.SectionTitle
import com.example.agmac.ui.viewmodel.CaregiverViewModel
import androidx.navigation.NavHostController
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import com.example.agmac.ui.viewmodel.CaregiverViewModel.PatientSummary
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverHomeScreen(navController: NavHostController) {
    val caregiverViewModel: CaregiverViewModel = viewModel()
    val uiState = caregiverViewModel.uiState.collectAsState()
    val patients = caregiverViewModel.patients.collectAsState()
    val isHistoryOpen = caregiverViewModel.isHistoryDialogOpen.collectAsState()
    val selectedPatient = caregiverViewModel.selectedPatient.collectAsState()
    val patientHistory = caregiverViewModel.patientHistory.collectAsState()
    var emailToAdd by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { caregiverViewModel.loadPatients() }
    LaunchedEffect(uiState.value.successMessage) {
        if (uiState.value.successMessage != null) {
            emailToAdd = ""
            caregiverViewModel.loadPatients() // refrescar lista tras añadir
        }
    }

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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    SectionTitle("Añadir Paciente")
                    OutlinedTextField(
                        value = emailToAdd,
                        onValueChange = { emailToAdd = it },
                        label = { Text("Email del paciente") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { caregiverViewModel.addPatientByEmail(emailToAdd) },
                        enabled = !uiState.value.isLoading
                    ) { Text(if (uiState.value.isLoading) "Procesando..." else "Añadir") }
                    uiState.value.errorMessage?.let { msg ->
                        Text(msg, color = MaterialTheme.colorScheme.error)
                    }
                    uiState.value.successMessage?.let { msg ->
                        Text(msg, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitle("Mis Pacientes")

                    if (uiState.value.isLoading && patients.value.isEmpty()) {
                        CircularProgressIndicator()
                    } else if (patients.value.isEmpty()) {
                        Text(
                            "No tienes pacientes registrados todavía.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    } else {
                        patients.value.forEach { p ->
                            DynamicPacienteItem(p, onClick = { caregiverViewModel.openPatientHistory(p) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitle("Alertas Recientes")
                }

                // Placeholder de alertas (podrás reemplazarlo luego con datos reales)
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

        if (isHistoryOpen.value && selectedPatient.value != null) {
            AlertDialog(
                onDismissRequest = { caregiverViewModel.closePatientHistory() },
                confirmButton = {
                    TextButton(onClick = { caregiverViewModel.closePatientHistory() }) {
                        Text("Cerrar")
                    }
                },
                title = {
                    val sp = selectedPatient.value!!
                    val titleText = sp.name?.ifBlank { "Paciente #${sp.id}" } ?: "Paciente #${sp.id}"
                    Text("Historial de $titleText")
                },
                text = {
                    if (uiState.value.isLoading && patientHistory.value.isEmpty()) {
                        Column(Modifier.fillMaxWidth()) { CircularProgressIndicator() }
                    } else if (patientHistory.value.isEmpty()) {
                        Text("No hay registros de pastillas tomadas aún.")
                    } else {
                        // Parser UTC para formato ISO básico: 2025-11-05T13:00:00Z
                        fun parseUtc(raw: String): Long? {
                            if (raw.isBlank()) return null
                            return try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                                sdf.timeZone = TimeZone.getTimeZone("UTC")
                                sdf.parse(raw)?.time
                            } catch (_: Exception) { null }
                        }
                        val nowMs = System.currentTimeMillis()
                        Column(Modifier.fillMaxWidth()) {
                            patientHistory.value.forEach { h ->
                                val scheduledRaw = h.hora_programada
                                val confirmRaw = h.hora_confirmacion ?: ""
                                val scheduledMs = parseUtc(scheduledRaw) // puede ser null si formato inesperado
                                val confirmMs = parseUtc(confirmRaw)
                                val displayStatus = when {
                                    h.estado.equals("TOMADO", ignoreCase = true) && confirmMs != null -> "TOMADO"
                                    (confirmMs == null && scheduledMs != null && scheduledMs < nowMs) -> "ATRASADO"
                                    else -> "PENDIENTE"
                                }
                                val timeText = when {
                                    displayStatus == "TOMADO" && confirmMs != null -> confirmRaw
                                    scheduledMs != null -> scheduledRaw
                                    else -> "(hora desconocida)"
                                }
                                Text(
                                    "• $timeText - $displayStatus",
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun DynamicPacienteItem(p: PatientSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val displayName = p.name?.ifBlank { "Paciente #${p.id}" } ?: "Paciente #${p.id}"
                Text(
                    displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    p.email ?: "",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
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