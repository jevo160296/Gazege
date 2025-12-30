package com.jmml.gazege.brio.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jmml.gazege.brio.ui.screens.BrioScreen

fun NavGraphBuilder.screenBrioMain() {
    composable("BrioMain") {
        BrioScreen()
    }
}