package com.example.agmac.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agmac.data.SessionManager
import com.example.agmac.data.repository.remote.ApiServiceProvider
import com.example.agmac.data.repository.remote.AlertApiProvider
import com.example.agmac.data.repository.remote.CaregiverRelation
import com.example.agmac.data.repository.remote.CaregiverRelationRequest
import com.example.agmac.data.model.Alerta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CaregiverViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = getApplication<Application>().applicationContext

    data class UiState(
        val isLoading: Boolean = false,
        val successMessage: String? = null,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    data class PatientSummary(val id: Int, val name: String, val email: String)
    private val _patients = MutableStateFlow<List<PatientSummary>>(emptyList())
    val patients = _patients.asStateFlow()

    private val _selectedPatient = MutableStateFlow<PatientSummary?>(null)
    val selectedPatient = _selectedPatient.asStateFlow()

    private val _patientHistory = MutableStateFlow<List<Alerta>>(emptyList())
    val patientHistory = _patientHistory.asStateFlow()

    private val _isHistoryDialogOpen = MutableStateFlow(false)
    val isHistoryDialogOpen = _isHistoryDialogOpen.asStateFlow()

    data class HistoryItem(val idAlerta: Int, val timeText: String, val status: String, val dosis: String)
    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems = _historyItems.asStateFlow()

    fun addPatientByEmail(email: String) {
        if (email.isBlank()) {
            _uiState.value = UiState(errorMessage = "Ingresa un email")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)
            val caregiverId = SessionManager.getUserId(appContext)
            if (caregiverId <= 0) {
                _uiState.value = UiState(errorMessage = "Sesión inválida. Inicia sesión nuevamente.")
                return@launch
            }
            val result = withContext(Dispatchers.IO) {
                try {
                    val query = "{" + "\"email\":\"$email\"}"
                    val userResp = ApiServiceProvider.authApi.findUsers(query)
                    if (!userResp.isSuccessful) {
                        return@withContext Pair(false, "Error buscando usuario: código ${userResp.code()}")
                    }
                    val items = userResp.body()?.items ?: emptyList()
                    if (items.isEmpty()) {
                        return@withContext Pair(false, "No se encontró un usuario con ese email")
                    }
                    val patientId = items.first().id
                    if (patientId == caregiverId) {
                        return@withContext Pair(false, "No puedes agregarte a ti mismo")
                    }

                    // Verificar si ya existe relación (para no duplicar)
                    val relationQuery = "{" + "\"id_cuidador\":$caregiverId, \"id_paciente\":$patientId}"
                    val existingResp = ApiServiceProvider.caregiverPatientApi.findRelations(relationQuery)
                    if (existingResp.isSuccessful) {
                        val existing = existingResp.body()?.items ?: emptyList()
                        if (existing.isNotEmpty()) {
                            return@withContext Pair(false, "La relación ya existe (ID relación=${existing.first().id ?: "?"})")
                        }
                    }

                    // Generar ID único compuesto (asumiendo patientId < 1_000_000)
                    val newId = generateCompositeRelationId(caregiverId, patientId)

                    val body = CaregiverRelationRequest(
                        id = newId,
                        id_cuidador = caregiverId,
                        id_paciente = patientId,
                        estado_solicitud = "ACEPTADO"
                    )
                    val postResp = ApiServiceProvider.caregiverPatientApi.createRelation(body)
                    if (postResp.isSuccessful) {
                        Pair(true, "Paciente añadido correctamente (relación ID=$newId, paciente ID=$patientId)")
                    } else {
                        Pair(false, "Error creando relación: código ${postResp.code()}")
                    }
                } catch (e: Exception) {
                    Pair(false, "Excepción: ${e.message}")
                }
            }
            _uiState.value = if (result.first) UiState(successMessage = result.second) else UiState(errorMessage = result.second)
        }
    }

    fun loadPatients() {
        val caregiverId = SessionManager.getUserId(appContext)
        if (caregiverId <= 0) {
            _uiState.value = UiState(errorMessage = "Sesión inválida. Inicia sesión nuevamente.")
            return
        }
        viewModelScope.launch {
            // Marcar loading (conservar mensajes previos si existen)
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = withContext(Dispatchers.IO) {
                try {
                    val query = "{" + "\"id_cuidador\":$caregiverId}"
                    val resp = ApiServiceProvider.caregiverPatientApi.findRelations(query)
                    if (!resp.isSuccessful) {
                        return@withContext Result.failure(Exception("Error listando relaciones: código ${resp.code()}"))
                    }
                    val relations = resp.body()?.items ?: emptyList()
                    if (relations.isEmpty()) {
                        return@withContext Result.success(emptyList<PatientSummary>())
                    }
                    // Paralelizar obtención de cada usuario
                    val deferred = relations.map { rel ->
                        async {
                            try {
                                val userResp = ApiServiceProvider.authApi.getUser(rel.id_paciente)
                                if (userResp.isSuccessful && userResp.body() != null) {
                                    val u = userResp.body()!!
                                    PatientSummary(u.id, u.name, u.email)
                                } else null
                            } catch (_: Exception) { null }
                        }
                    }
                    val list = deferred.awaitAll().filterNotNull()
                    Result.success(list)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                _patients.value = result.getOrNull() ?: emptyList()
            } else {
                _uiState.value = UiState(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun openPatientHistory(patient: PatientSummary) {
        _selectedPatient.value = patient
        _isHistoryDialogOpen.value = true
        loadPatientHistory(patient.id)
    }

    fun closePatientHistory() {
        _isHistoryDialogOpen.value = false
        _patientHistory.value = emptyList()
        _selectedPatient.value = null
    }

    private fun loadPatientHistory(patientId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = withContext(Dispatchers.IO) {
                try {
                    val api = AlertApiProvider.alertApi
                    val query = "{" + "\"id_paciente\":$patientId}"
                    val orderBy = "{" + "\"hora_programada\":\"DESC\"}"
                    val resp = api.getAlertas(query, orderBy)
                    if (resp.isSuccessful) {
                        Result.success(resp.body()?.items ?: emptyList<Alerta>())
                    } else {
                        Result.failure(Exception("Error ${resp.code()} listando historial"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                val rawList = result.getOrNull() ?: emptyList()
                _patientHistory.value = rawList
                // Precomputar items para UI (parseo y estado) en hilo principal pero usando datos ya listos
                val computed = withContext(Dispatchers.Default) { computeHistoryItems(rawList) }
                _historyItems.value = computed
            } else {
                _uiState.value = UiState(errorMessage = result.exceptionOrNull()?.message)
                _patientHistory.value = emptyList()
                _historyItems.value = emptyList()
            }
        }
    }

    private fun computeHistoryItems(alertas: List<Alerta>): List<HistoryItem> {
        if (alertas.isEmpty()) return emptyList()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val nowMs = System.currentTimeMillis()
        return alertas.map { a ->
            val scheduledMs = try { sdf.parse(a.hora_programada)?.time } catch (_: Exception) { null }
            val confirmMs = try { if (!a.hora_confirmacion.isNullOrBlank()) sdf.parse(a.hora_confirmacion)?.time else null } catch (_: Exception) { null }
            val status = when {
                a.estado.equals("TOMADO", ignoreCase = true) && confirmMs != null -> "TOMADO"
                (confirmMs == null && scheduledMs != null && scheduledMs < nowMs) -> "ATRASADO"
                else -> "PENDIENTE"
            }
            val timeText = when {
                status == "TOMADO" && confirmMs != null -> a.hora_confirmacion ?: a.hora_programada
                scheduledMs != null -> a.hora_programada
                else -> "(hora desconocida)"
            }
            HistoryItem(
                idAlerta = a.id_alerta,
                timeText = timeText ?: "(hora desconocida)",
                status = status,
                dosis = a.dosis
            )
        }
    }

    private fun generateCompositeRelationId(caregiverId: Int, patientId: Int): Int {
        val factor = 1_000_000L // espacio reservado para hasta 6 dígitos del patientId
        val candidate = caregiverId.toLong() * factor + patientId.toLong()
        return if (candidate <= Int.MAX_VALUE) {
            candidate.toInt()
        } else {
            // Fallback: usar hash positivo del par para garantizar que quepa en Int
            ("${caregiverId}_$patientId".hashCode().let { if (it < 0) -it else it })
        }
    }
}
