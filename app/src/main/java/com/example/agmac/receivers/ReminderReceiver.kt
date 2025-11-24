package com.example.agmac.receivers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val idAlerta = intent.getIntExtra(EXTRA_ID_ALERTA, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Recordatorio"
        val text = intent.getStringExtra(EXTRA_TEXT) ?: "Hora de tomar el medicamento"

        createNotificationChannel(context)

        // En Android 13+ se requiere el permiso POST_NOTIFICATIONS en tiempo de ejecución
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                android.util.Log.w("ReminderReceiver", "No se puede mostrar notificación: falta permiso POST_NOTIFICATIONS")
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(idAlerta, notification)
        } catch (ex: SecurityException) {
            android.util.Log.w("ReminderReceiver", "Fallo al postear notificación: ${ex.message}")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios"
            val description = "Canal de recordatorios de medicación"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "med_reminders_channel"
        const val EXTRA_ID_ALERTA = "id_alerta"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
    }
}
