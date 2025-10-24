package com.example.agmac.data

/**
 * Simple singleton to hold ephemeral user session data (selected role).
 * This is a lightweight approach suitable for small apps / prototypes.
 * For production consider using a proper ViewModel or persistent storage.
 */
object SessionManager {
    // "paciente" or "cuidador" (default empty until selection)
    var role: String = ""
}

