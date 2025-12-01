package com.example.agmac.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.agmac.ui.screens.*
import com.example.agmac.data.SessionManager
import com.example.agmac.ui.auth.SignUpScreen
import com.example.agmac.ui.auth.RegisterScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    // Diagnóstico: loguear cambios de ruta
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            Log.d("NavDebug", "Ruta actual: ${entry.destination.route}")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash" // La primera pantalla al abrir la app
    ) {
        // Pantalla 1: Bienvenida
        composable("splash") { SplashScreen(navController) }

        // Pantalla 2: Login
        composable("login") { SignUpScreen(navController) }
        composable("register") { RegisterScreen(navController) }

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
        composable("list_patients") { MisPacientesScreen(navController)}
        composable("patient_settings") { PatientSettingsScreen(navController) }
        composable("caregiver_settings") { CaregiverSettingsScreen(navController) }
        composable(
            "patient_detail/{pacienteId}",
            arguments = listOf(navArgument("pacienteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getInt("pacienteId") ?: 0
            PacienteDetalleScreen(navController, pacienteId)
        }
        composable("detalle_medicamento/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")?.toIntOrNull()
            DetalleMedicamentoScreen(id = id)
        }
        composable("create_patient") {
            CreatePatientScreen(navController)
        }
    }
}

@Composable
fun DetalleMedicamentoScreen(id: Int?) {
    TODO("Not yet implemented")
}
