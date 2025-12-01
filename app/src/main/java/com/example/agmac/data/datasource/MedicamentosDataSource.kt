package com.example.agmac.data.datasource

import android.content.Context
import com.example.agmac.data.model.MedicamentoEstado
import com.example.agmac.data.model.MedicamentoHoyConId
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MedicamentoDataSource(private val context: Context) {

    private val gson = Gson()
    private val fileName = "Medicamentos.json"

    private fun getFile(): File {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            // Inicializamos con JSON vacío si no existe
            file.writeText("[]")
        }
        return file
    }

    private fun saveMedicamentos(lista: List<MedicamentoHoyConId>) {
        getFile().writeText(gson.toJson(lista))
    }

    fun getMedicamentos(): List<MedicamentoHoyConId> {
        val file = getFile()
        val json = file.readText()
        return gson.fromJson(json, object : TypeToken<List<MedicamentoHoyConId>>() {}.type)
    }

    fun addMedicamento(m: MedicamentoHoyConId) {
        val lista = getMedicamentos().toMutableList()
        lista.add(m)
        saveMedicamentos(lista)
    }

    fun updateMedicamento(m: MedicamentoHoyConId) {
        val lista = getMedicamentos().map { if (it.id == m.id) m else it }
        saveMedicamentos(lista)
    }

    fun deleteMedicamento(id: Int) {
        val lista = getMedicamentos().filter { it.id != id }
        saveMedicamentos(lista)
    }
}
