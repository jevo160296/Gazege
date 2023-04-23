package com.example.gazege.ui.navigation

import androidx.navigation.NavController

const val URI = "https://www.example.gazege"

fun NavController.navigateUpOrClose(
    onCloseApp: () -> Unit
) {
    navigateUp()
    if (this.backQueue.size <= 1) {
        onCloseApp()
    }
}

