package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun SplashScreenFragment(
    onNavigateToInitialScreen: () -> Unit
) {
    Surface(Modifier.fillMaxSize(), color = Color.Transparent) {
        Text("Cargando")
    }
    LaunchedEffect("splashScreen") {
        delay(500L)
        onNavigateToInitialScreen()
    }
}