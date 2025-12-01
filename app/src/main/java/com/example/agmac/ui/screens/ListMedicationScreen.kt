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
import androidx.lifecycle.viewmodel.compose.viewModel

//importar Theme y boton de navegación
import com.example.agmac.ui.theme.AppTheme
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.components.MedicamentoCardClickable
import androidx.navigation.NavHostController
import com.example.agmac.ui.viewmodel.MedicamentosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicamentosScreen(
    navController: NavHostController,
    viewModel: MedicamentosViewModel  = viewModel()
) {
    val medicamentos by viewModel.medicamentos.collectAsState()

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Medicamentos",
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
                    onClick = { /* TODO: añadir medicamento */ },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Añadir Medicamento") }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(medicamentos) { med ->
                    MedicamentoCardClickable(
                        medicamento = med,
                        onClick = {
                            navController.navigate("detalle_medicamento/${med.id}")
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
