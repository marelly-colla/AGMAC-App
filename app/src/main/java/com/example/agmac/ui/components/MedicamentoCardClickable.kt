package com.example.agmac.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.agmac.data.model.MedicamentoEstado
import com.example.agmac.data.model.MedicamentoHoy
import com.example.agmac.data.model.MedicamentoHoyConId

@Composable
fun MedicamentoCardClickable(
    medicamento: MedicamentoHoyConId,
    onClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick(medicamento.id) }
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MedicamentoCard(
            medicamento = MedicamentoHoy(
                nombre = medicamento.nombre,
                dosis = medicamento.dosis,
                hora = medicamento.hora,
                estado = medicamento.estado
            )
        )

        Spacer(Modifier.width(6.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Detalle",
            tint = Color.White
        )
    }
}
