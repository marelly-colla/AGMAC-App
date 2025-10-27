package com.example.agmac.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.agmac.data.SessionManager

data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // Construir items dependientes del rol seleccionado
    val items = if (SessionManager.role == "cuidador") {
        listOf(
            BottomNavItem("Inicio", Icons.Outlined.Home, "caregiver_home"),
            BottomNavItem("Medicación", Icons.Outlined.MedicalServices, "caregiver_home"),
            BottomNavItem("Reportes", Icons.Outlined.Analytics, "reports"),
            BottomNavItem("Ajustes", Icons.Outlined.Settings, "caregiver_settings")
        )
    } else {
        // por defecto o "paciente"
        listOf(
            BottomNavItem("Inicio", Icons.Outlined.Home, "patient_home"),
            BottomNavItem("Pacientes", Icons.Outlined.MedicalServices, "patient_medication"),
            BottomNavItem("Reportes", Icons.Outlined.Analytics, "reports"),
            BottomNavItem("Ajustes", Icons.Outlined.Settings, "patient_settings")
        )
    }

    // Obtener ruta actual para marcar el item seleccionado
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 12.sp) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Mantener una sola instancia en la pila y restaurar estado
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                },
                alwaysShowLabel = true
            )
        }
    }
}