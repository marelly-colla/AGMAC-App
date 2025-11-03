package com.example.agmac.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Simple singleton to hold ephemeral user session data (selected role).
 * This is a lightweight approach suitable for small apps / prototypes.
 * For production consider using a proper ViewModel or persistent storage.
 */
object SessionManager {
    // "paciente" or "cuidador" (default empty until selection)
    var role: String = ""

    private const val PREFS_NAME = "user_session_prefs"
    private const val KEY_USER_ID = "user_id"

    fun saveUserId(context: Context, userId: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): Int {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_USER_ID, -1)
    }
}
