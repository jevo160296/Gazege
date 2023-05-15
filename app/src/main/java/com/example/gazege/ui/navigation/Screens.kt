package com.example.gazege.ui.navigation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gazege.MainViewModel

const val URI = "https://www.example.gazege"

@Composable
fun MainNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    onCloseApp: () -> Unit,
    onDataLoaded: () -> Unit
) {
    val current = navController.currentBackStackEntryAsState()
    val currentRoute = current.value?.destination?.route
    Box(modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .fillMaxSize()
        .statusBarsPadding()
        .run {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                this.navigationBarsPadding()
            } else {
                this
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier
                .run {
                    if (currentRoute != "main" && currentRoute?.isNotEmpty() == true) {
                        this.navigationBarsPadding()
                    } else {
                        this
                    }
                }
        ) {
            screenMain(
                viewModel = mainViewModel,
                onNavigateToEditPerson = navController::navigateToEditPerson,
                onNavigateToEditTransaction = navController::navigateToEditTransaction,
                onNavigateToEditAccount = navController::navigateToEditAccount,
                onNavigateToAddPerson = navController::navigateToAddPerson,
                onNavigateToAddAccount = navController::navigateToAddAccount,
                onNavigateToSettings = navController::navigateToSettings,
                onNavigateToAccountDetail = navController::navigateToAccountDetail,
                onNavigateToAddTransaction = navController::navigateToAddTransaction,
                onNavigateToPersonDetail = navController::navigateToPersonDetail,
                onNavigateToSaldoActualSettings = navController::navigateToSaldoActualSettings,
                onNavigateToCategories = navController::navigateToEditarCategorias,
                onNavigateToBudget = navController::navigateToEditBudget,
                onDataLoaded = onDataLoaded
            )
            screenAddAccount(
                viewModel = mainViewModel,
                onNavigateToAddPerson = navController::navigateToAddPerson,
                onNavigateUp = navController::navigateUp,
                onNavigateToSettings = navController::navigateToSettings
            )
            screenEditAccount(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp,
                onNavigateToSettings = navController::navigateToSettings,
                onNavigateToAddPerson = navController::navigateToAddPerson
            )
            screenAddPerson(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp
            )
            screenEditPerson(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp
            )
            screenAddTransaction(
                viewModel = mainViewModel,
                onNavigateUp = { navController.navigateUpOrClose { onCloseApp() } },
                onNavigateToAddAccount = navController::navigateToAddAccount,
                onDataLoaded = onDataLoaded
            )
            screenEditTransaction(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp,
                onNavigateToAddAccount = navController::navigateToAddAccount
            )
            screenSettings(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp,
                onNavigateToAddAccount = navController::navigateToAddAccount,
                onNavigateToAddPerson = navController::navigateToAddPerson,
                onNavigateToAddBudget = navController::navigateToAddOneBudget,
                onNavigateToAddCategory = navController::navigateToAddCategory
            )
            screenSaldoActualSettings(viewModel = mainViewModel)
            screenEditarCategorias(
                viewModel = mainViewModel,
                onNavigateToAddCategory = navController::navigateToAddCategory,
                onNavigateToEditCategory = navController::navigateToEditCategory,
                onNavigateToAddBudget = {
                    if (it != null) {
                        navController.navigateToAddOneBudget(it)
                    } else {
                        navController.navigateToAddOneBudget()
                    }
                }
            )
            screenAddCategory(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp
            )
            screenEditCategory(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp,
                onNavigateToEditOneBudgetRequested = navController::navigateToEditOneBudget
            )
            screenAccountDetail(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp,
                onNavigateToEditAccount = navController::navigateToEditAccount,
                onNavigateToEditTransaction = navController::navigateToEditTransaction,
                onNavigateToAddTransaction = navController::navigateToAddTransaction
            )
            screenPersonDetail(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp,
                onNavigateToEditTransaction = navController::navigateToEditTransaction,
                onNavigateToEditPerson = navController::navigateToEditPerson
            )
            screenEditBudget(
                viewModel = mainViewModel,
                onNavigateToOneBudgetDetail = navController::navigateToOneBudgetDetail,
                onNavigateToAddOneBudget = navController::navigateToAddOneBudget,
                onNavigateToOneBudgetEdit = navController::navigateToEditOneBudget
            )
            screenAddOneBudget(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp
            )
            screenOneBudgetDetail(viewModel = mainViewModel)
            screenEditOneBudget(
                viewModel = mainViewModel,
                onNavigateUp = navController::navigateUp
            )
        }
        AnimatedVisibility(
            visible = currentRoute != "main" && currentRoute?.isNotEmpty() == true,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            enter = fadeIn(initialAlpha = 1f),
            exit = fadeOut(tween(delayMillis = 50))
        ) {
            Spacer(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
            )
        }
    }
}

fun NavController.navigateUpOrClose(
    onCloseApp: () -> Unit
) {
    navigateUp()
    if (this.backQueue.size <= 1) {
        onCloseApp()
    }
}