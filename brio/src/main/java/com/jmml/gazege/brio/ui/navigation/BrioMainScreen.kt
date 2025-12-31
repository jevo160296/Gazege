package com.jmml.gazege.brio.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.jmml.gazege.brio.ui.screens.BrioScreen

const val BRIO_ROUTE = "brio"

fun NavGraphBuilder.brioNavGraph() {
    navigation(startDestination = "BrioMain", route = BRIO_ROUTE) {
        composable("BrioMain") {
            BrioScreen()
        }
    }
}