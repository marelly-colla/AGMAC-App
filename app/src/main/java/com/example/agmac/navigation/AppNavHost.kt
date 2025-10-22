package com.example.agmac.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.agmac.ui.screens.*

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
                    when (role) {
                        "paciente" -> navController.navigate("patient_home")
                        "cuidador" -> navController.navigate("caregiver_home")
                    }
                }
            )
        }

        // Pantalla 4.1: Inicio del Paciente
        composable("patient_home") { PatientHomeScreen() }

        // Pantalla 4.2: Inicio del Cuidador
        composable("caregiver_home") { CaregiverHomeScreen() }
    }
}
