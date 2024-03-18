package com.example.gazege.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.ui.fragments.LoadingShowOnBoarding

const val INITIALSCREENROUTE = "initialScreen"

fun NavGraphBuilder.screenInitialScreen(
    viewModelInitialScreen: MainViewModel.ViewModelInitial,
    onNavigateToOnBoarding: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    composable(INITIALSCREENROUTE) {
        val showOnBoarding by viewModelInitialScreen.rememberShowOnBoarding()
        if (showOnBoarding == null) {
            LoadingShowOnBoarding()
        }
        LaunchedEffect(key1 = showOnBoarding) {
            showOnBoarding?.let {
                if (it) {
                    onNavigateToOnBoarding()
                } else {
                    viewModelInitialScreen.setShowOnBoarding(false)
                    onNavigateToMain()
                }
            }
        }
    }
}

fun NavController.navigateToInitialScreen() {
    navigate(INITIALSCREENROUTE)
}