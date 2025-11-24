package com.example.agmac.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//importar Theme y boton de navegación
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.PacienteCard
import androidx.navigation.NavHostController
import com.example.agmac.ui.viewmodel.PacientesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPacientesScreen(
    navController: NavHostController,
    viewModel: PacientesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val pacientes by viewModel.pacientes.collectAsState(initial = emptyList())

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Mis Pacientes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Notificaciones */ }) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones")
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { /* TODO: añadir paciente */ },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Añadir Paciente") }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(pacientes) { paciente ->
                    PacienteCard(paciente = paciente, navController = navController)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
