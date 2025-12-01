package com.example.agmac.data.datasource

import android.content.Context
import com.example.agmac.data.model.Paciente
import com.example.agmac.data.model.EstadoPaciente
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class PacienteDataSource(private val context: Context) {

    private val gson = Gson()
    private val fileName = "Pacientes.json"

    // --- Obtener archivo en filesDir ---
    private fun getFile(): File {
        val file = File(context.filesDir, fileName)
        // Si no existe, inicializarlo copiando de assets
        if (!file.exists()) {
            context.assets.open(fileName).use { input ->
                file.writeBytes(input.readBytes())
            }
        }
        return file
    }

    // --- Leer todos los pacientes ---
    fun getPacientes(): List<Paciente> {
        val file = getFile()
        val json = file.readText()
        val listaDto: List<PacienteDTO> = gson.fromJson(json, object : TypeToken<List<PacienteDTO>>() {}.type)
        return listaDto.map { dto ->
            Paciente(
                id = dto.id,
                nombre = dto.nombre,
                estado = EstadoPaciente.valueOf(dto.estado),
                fotoUrl = dto.fotoUrl
            )
        }
    }

    // --- Guardar lista de pacientes ---
    private fun savePacientes(lista: List<Paciente>) {
        val listaDto = lista.map { dto ->
            PacienteDTO(
                id = dto.id,
                nombre = dto.nombre,
                estado = dto.estado.name,
                fotoUrl = dto.fotoUrl
            )
        }
        getFile().writeText(gson.toJson(listaDto))
    }

    // --- Agregar paciente ---
    fun addPaciente(paciente: Paciente) {
        val lista = getPacientes().toMutableList()
        lista.add(paciente)
        savePacientes(lista)
    }

    // --- Editar paciente ---
    fun updatePaciente(paciente: Paciente) {
        val lista = getPacientes().map {
            if (it.id == paciente.id) paciente else it
        }
        savePacientes(lista)
    }

    // --- Borrar paciente ---
    fun deletePaciente(id: Int) {
        val lista = getPacientes().filter { it.id != id }
        savePacientes(lista)
    }
}

// DTO para mapear JSON
data class PacienteDTO(
    val id: Int,
    val nombre: String,
    val estado: String,
    val fotoUrl: String
)