package com.example.agmac.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.SectionTitle
import com.example.agmac.ui.theme.AppTheme

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientSettingsScreen(navController: NavHostController) {
    AppTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Ajustes (Paciente)") }) },
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                SectionTitle("Cuenta")
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nombre: Juan Pérez", style = MaterialTheme.typography.bodyLarge)
                            Text("Usuario: paciente@example.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Preferencias")
                Spacer(modifier = Modifier.height(8.dp))
                val notifEnabled = remember { mutableStateOf(true) }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notificaciones", style = MaterialTheme.typography.bodyLarge)
                        Text("Recordatorios de medicación", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                    Switch(checked = notifEnabled.value, onCheckedChange = { notifEnabled.value = it })
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

