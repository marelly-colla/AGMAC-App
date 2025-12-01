package com.example.agmac.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.agmac.data.model.MedicamentoEstado
import com.example.agmac.data.model.MedicamentoHoy

@Composable
fun MedicamentoCard(medicamento: MedicamentoHoy) {
    // Usamos Triple para devolver 3 valores: background color, icon color y el ImageVector del icono
    val (bg, iconColor, icon) = when (medicamento.estado) {
        MedicamentoEstado.PENDIENTE ->
            Triple(Color(0xFFFFF3E0), Color(0xFFFF9800), Icons.Outlined.Schedule)
        MedicamentoEstado.TOMADO ->
            Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), Icons.Outlined.CheckCircle)
    }
    // Nuevo fondo oscuro para TODA la tarjeta
    val cardBackground = Color(0xFF1E1E1E)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBackground, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Medication,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = medicamento.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = medicamento.dosis,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                medicamento.hora,
                color = Color.LightGray,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(Modifier.width(8.dp))
            Icon(icon, contentDescription = null, tint = iconColor)
        }
    }
}