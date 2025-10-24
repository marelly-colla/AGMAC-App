package com.example.agmac.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.agmac.ui.screens.*
import com.example.agmac.data.SessionManager

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash" // La primera pantalla al abrir la app
    ) {
        // Pantalla 1: Bienvenida
        composable("splash") { SplashScreen(navController) }

        // Pantalla 2: Login
        composable("login") { SignUpScreen(navController) }

        // Pantalla 3: Selección de rol
        composable("role_selection") {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    // Guardar rol en sesión y navegar a la pantalla correspondiente
                    SessionManager.role = role
                    when (role) {
                        "paciente" -> navController.navigate("patient_home")
                        "cuidador" -> navController.navigate("caregiver_home")
                    }
                }
            )
        }

        // Pantalla 4.1: Inicio del Paciente (recibe NavController para la barra inferior)
        composable("patient_home") { PatientHomeScreen(navController) }

        // Pantalla 4.2: Inicio del Cuidador
        composable("caregiver_home") { CaregiverHomeScreen(navController) }

        // Pantalla Reportes
        composable("reports") { ReportsScreen(navController) }

        // Rutas adicionales por rol
        composable("patient_medication") { PatientMedicationScreen(navController) }
        composable("patient_settings") { PatientSettingsScreen(navController) }
        composable("caregiver_settings") { CaregiverSettingsScreen(navController) }
    }
}
