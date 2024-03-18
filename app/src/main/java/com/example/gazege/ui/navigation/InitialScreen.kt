package com.example.gazege.ui.navigation

import androidx.compose.runtime.LaunchedEffect
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
        val showOnBoarding = viewModelInitialScreen.rememberShowOnBoarding().value
        val principalPersonId = viewModelInitialScreen.rememberPrincipalPersonId().value
        val accounts = viewModelInitialScreen.rememberPrincipalAccounts(principalPersonId).value

        if (showOnBoarding == null || principalPersonId == null || accounts == null) {
            LoadingShowOnBoarding()
        }
        LaunchedEffect(
            key1 = showOnBoarding,
            key2 = principalPersonId,
            key3 = accounts
        ) {
            if (showOnBoarding != null && principalPersonId != -1 && accounts != null) {
                if (
                    showOnBoarding ||
                    principalPersonId == null ||
                    accounts.isEmpty()
                ) {
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