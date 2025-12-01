package com.example.agmac

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.util.Log

class App : Application(), ComponentCallbacks2 {
    override fun onCreate() {
        super.onCreate()
        // Inicializaciones de app si se necesitan
        Log.d("App", "Application created")
    }

    @Suppress("DEPRECATION", "RedundantQualifierName")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Manejar eventos de trimming de memoria; liberar caches aquí
        Log.w("App", "onTrimMemory level=$level")
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // The app is running but the device is running low on memory.
                // Free memory that isn't critical.
                clearAppCaches()
            }
            TRIM_MEMORY_UI_HIDDEN -> {
                // App's UI is no longer visible. Release UI-related resources.
                clearUiCaches()
            }
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                // App is in the background or about to be terminated.
                clearAppCaches()
            }
            else -> {
                // Other levels
                clearAppCaches()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w("App", "onLowMemory called")
        clearAppCaches()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private fun clearAppCaches() {
        try {
            // Aquí puedes limpiar caches de librerías como Glide/Picasso si las usas
            // Ejemplo (si usas Glide): Glide.get(this).clearMemory() -> requiere llamar en UI thread
            // y Glide.get(this).clearDiskCache() en background thread.
            Log.d("App", "Clearing app caches (no-op placeholder)")
        } catch (ex: Exception) {
            Log.w("App", "Error clearing caches: ${ex.message}")
        }
    }

    private fun clearUiCaches() {
        // Liberar recursos relacionados con UI
        Log.d("App", "Clearing UI caches (no-op placeholder)")
    }
}
