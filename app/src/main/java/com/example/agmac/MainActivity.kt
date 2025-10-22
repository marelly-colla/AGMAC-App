package com.example.agmac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.navigation.compose.rememberNavController
import com.example.agmac.navigation.AppNavHost
import com.example.agmac.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppScreen()
            }
        }
    }
}

@Composable
fun AppScreen() {
    val navController = rememberNavController()
    AppNavHost(navController)
}