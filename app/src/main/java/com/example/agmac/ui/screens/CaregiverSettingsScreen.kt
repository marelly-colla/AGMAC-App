package com.example.agmac.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun CaregiverSettingsScreen(navController: NavHostController) {
    AppTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Ajustes (Cuidador)") }) },
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                SectionTitle("Perfil del Cuidador")
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Nombre: Marta González", style = MaterialTheme.typography.bodyLarge)
                        Text("Teléfono: +51 9xx xxx xxx", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Gestión")
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Acceso a pacientes", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Aquí podrías añadir o gestionar los pacientes que cuidas.")
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
