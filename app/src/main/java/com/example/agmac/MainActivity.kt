package com.example.agmac

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

import androidx.navigation.compose.rememberNavController
import com.example.agmac.navigation.AppNavHost
import com.example.agmac.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Opcional: mostrar mensaje o log
        if (!granted) {
            android.util.Log.w("MainActivity", "POST_NOTIFICATIONS no concedido")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pedir permiso de notificaciones en Android 13+
        requestPostNotificationsIfNeeded()

        // Si el dispositivo no permite alarmas exactas, mostrar dialogo que lleve a ajustes
        askForExactAlarmsIfNeeded()

        setContent {
            AppTheme {
                AppScreen()
            }
        }
    }

    private fun requestPostNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun askForExactAlarmsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(AlarmManager::class.java)
            if (am != null && !am.canScheduleExactAlarms()) {
                // Mostrar diálogo explicativo; al aceptar, abrir pantalla del sistema para permitir exact alarms
                try {
                    AlertDialog.Builder(this)
                        .setTitle("Permitir alarmas exactas")
                        .setMessage("Para que los recordatorios suenen exactamente a la hora programada, permite 'Alarmas exactas' para esta app en Ajustes.")
                        .setPositiveButton("Abrir ajustes", DialogInterface.OnClickListener { _, _ ->
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                startActivity(intent)
                            } catch (ex: Exception) {
                                // Fallback: abrir detalles de la app
                                val intent2 = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.parse("package:$packageName")
                                }
                                startActivity(intent2)
                            }
                        })
                        .setNegativeButton("Ahora no", null)
                        .show()
                } catch (ex: Exception) {
                    // Si no se puede mostrar diálogo por alguna razón, intentar abrir directamente ajustes (menos ideal)
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    } catch (innerEx: Exception) {
                        android.util.Log.w("MainActivity", "No se pudo abrir la pantalla de alarmas exactas: ${innerEx.message}")
                    }
                }
            }
        }
    }
}

@Composable
fun AppScreen() {
    val navController = rememberNavController()
    AppNavHost(navController)
}