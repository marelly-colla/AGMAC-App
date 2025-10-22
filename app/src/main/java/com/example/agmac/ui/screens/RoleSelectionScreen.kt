package com.example.agmac.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
//import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agmac.ui.theme.AppTheme
//import com.example.agmac.ui.components.BottomNavigationBar

@Composable
fun RoleSelectionScreen(onRoleSelected:(String)->Unit){
    Scaffold(
        //bottomBar = { BottomNavigationBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo (reemplaza con tu imagen o SVG)
            Icon(
                imageVector = Icons.Outlined.Favorite,
                contentDescription = "Logo Agmac",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Título principal
            Text(
                text = "AGMAC",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(32.dp))

            // Subtítulo
            Text(
                text = "¿Quién está usando la aplicación?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Descripción
            Text(
                text = "Selecciona tu rol para personalizar tu experiencia y ayudarte a gestionar la medicación de forma segura y sencilla.",
                fontSize = 16.sp,
                color = if (isSystemInDarkTheme()) Color(0xFFB0B0B0) else Color.Gray,
                modifier = Modifier.widthIn(max = 300.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Botones de roles con efecto "scale" al tocar
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RoleButton("Paciente", Icons.Outlined.Person, Color(0xFF13A4EC), true, onClick = { onRoleSelected("paciente") } )
                RoleButton("Cuidador", Icons.Outlined.Group, Color(0xFF13A4EC), false, onClick = { onRoleSelected("cuidador") } )
            }
        }
    }
}
@Composable
fun RoleButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, filled: Boolean, onClick: () -> Unit ) {
    var pressed by remember { mutableStateOf(false) }
    val scale = if (pressed) 1.05f else 1.0f

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) color else color.copy(alpha = 0.2f),
            contentColor = if (filled) Color.White else color
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .animateContentSize(animationSpec = spring()),
        shape = RoundedCornerShape(16.dp),
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = text)
            Spacer(Modifier.width(8.dp))
            Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRoleSelectionScreen() {
    AppTheme {
        RoleSelectionScreen(
            onRoleSelected = {}
        )
    }
}