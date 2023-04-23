package com.example.gazege.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.gazege.MainViewModel

const val URI = "https://www.example.gazege"

@Composable
fun MainNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    onCloseApp: () -> Unit,
    onDataLoaded: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "main"
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
            onNavigateToEditCategories = navController::navigateToEditarCategorias,
            onNavigateToEditBudget = navController::navigateToEditBudget
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
}

fun NavController.navigateUpOrClose(
    onCloseApp: () -> Unit
) {
    navigateUp()
    if (this.backQueue.size <= 1) {
        onCloseApp()
    }
}