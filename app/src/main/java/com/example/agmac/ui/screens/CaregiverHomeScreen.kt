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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.agmac.R

//importar Theme y boton de navegación
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverHomeScreen() {
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
            bottomBar = {
                BottomNavigationBar(selected = "Ajustes")
            },
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

                    val pacientes = listOf(
                        Paciente(
                            "Elena Ramírez", "Última dosis: 10:00 AM",
                            R.drawable.user1
                        ),
                        Paciente(
                            "Carlos Mendoza", "Última dosis: 12:00 PM",
                            R.drawable.user2
                        )
                    )

                    pacientes.forEach { paciente ->
                        PacienteItem(paciente)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitle("Alertas Recientes")
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

data class Paciente(val nombre: String, val detalle: String, val imagenRes: Int)

@Composable
fun PacienteItem(paciente: Paciente) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navegar al detalle */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Image(
                painter = painterResource(paciente.imagenRes),
                contentDescription = paciente.nombre,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    paciente.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    paciente.detalle,
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