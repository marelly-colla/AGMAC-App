package com.example.agmac.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agmac.data.SessionManager
import com.example.agmac.data.model.Alerta
import com.example.agmac.data.repository.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class PatientMedicationViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = getApplication<Application>().applicationContext
    private val _alertas = MutableStateFlow<List<Alerta>>(emptyList())
    val alertas = _alertas.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Nuevo: estado para mensajes de error desde operaciones remotas
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun loadAlertas() {
        val idPaciente = SessionManager.getUserId(appContext)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = AlertRepository.loadAlertsFromServer(appContext, idPaciente)
                _alertas.value = result
            } catch (e: Exception) {
                Log.e("PatientMedicationVM", "Error cargando alertas", e)
                _errorMessage.value = "Error cargando alertas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAlertAsTaken(idAlerta: Int, horaConfirmacion: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                AlertRepository.markAlertAsTaken(appContext, idAlerta, horaConfirmacion)
                loadAlertas()
            } catch (e: Exception) {
                Log.e("PatientMedicationVM", "Error marcando alerta", e)
                _errorMessage.value = "Error marcando alerta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAlert(idAlerta: Int) {
        val idPaciente = SessionManager.getUserId(appContext)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                AlertRepository.deleteAlert(appContext, idAlerta)
                loadAlertas()
            } catch (e: Exception) {
                Log.e("PatientMedicationVM", "Error borrando alerta", e)
                _errorMessage.value = "Error borrando alerta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAlertSchedule(
        idMedicamento: Int,
        dosis: String,
        fechaInicioStr: String,
        numeroDeDias: Int,
        horasDelDia: List<String>
    ) {
        val idPaciente = SessionManager.getUserId(appContext)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ok = AlertRepository.createAlertSchedule(
                    appContext,
                    idPaciente,
                    idMedicamento,
                    dosis,
                    fechaInicioStr,
                    numeroDeDias,
                    horasDelDia
                )
                if (!ok) {
                    _errorMessage.value = "No se pudieron crear todas las alertas. Revisa los datos e intenta de nuevo."
                } else {
                    _errorMessage.value = null
                }
                loadAlertas()
            } catch (e: Exception) {
                Log.e("PatientMedicationVM", "Error creando alertas", e)
                _errorMessage.value = "Error creando alertas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
