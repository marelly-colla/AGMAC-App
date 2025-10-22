package com.example.agmac.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp

data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
@Composable
fun BottomNavigationBar(selected: String) {
    val items = listOf(
        BottomNavItem("Inicio", Icons.Outlined.Home),
        BottomNavItem("Medicación", Icons.Outlined.MedicalServices),
        BottomNavItem("Reportes", Icons.Outlined.Analytics),
        BottomNavItem("Ajustes", Icons.Outlined.Settings)
    )
    var selectedIndex by remember { mutableStateOf(items.indexOfFirst { it.label == selected })  }

    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 12.sp) },
                selected = selectedIndex == index,
                onClick = { selectedIndex = index },
                alwaysShowLabel = true
            )
        }
    }
}