package com.jmml.gazege.ui.navigation

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jmml.gazege.MainViewModel

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
            startDestination = INITIALSCREENROUTE,
            modifier = Modifier
                .run {
                    if (currentRoute != "main" && currentRoute?.isNotEmpty() == true) {
                        this.navigationBarsPadding()
                    } else {
                        this
                    }
                },
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            screenInitialScreen(
                viewModelInitialScreen = mainViewModel.viewModelInitial,
                onNavigateToOnBoarding = navController::navigateToOnBoarding,
                onNavigateToMain = navController::navigateoToMain
            )
            screenOnBoardingScreen(
                viewModelOnBoarding = mainViewModel.viewModelOnBoarding,
                onNavigateToMainScreen = navController::navigateoToMain,
                onImportData = mainViewModel::startActivityToLoadData
            )
            screenMain(
                viewModelMain = mainViewModel.viewModelMain,
                viewModelCategoryList = mainViewModel.viewModelCategoryList,
                sampleModule = mainViewModel.sampleModule,
                onNavigateToAddPerson = navController::navigateToAddPerson,
                onNavigateToEditPerson = navController::navigateToEditPerson,
                onNavigateToPersonDetail = navController::navigateToPersonDetail,
                onNavigateToAddAccount = navController::navigateToAddAccount,
                onNavigateToEditAccount = navController::navigateToEditAccount,
                onNavigateToAccountDetail = navController::navigateToAccountDetail,
                onNavigateToAddTransaction = navController::navigateToAddTransaction,
                onNavigateToEditTransaction = navController::navigateToEditTransaction,
                onNavigateToEditPromissoryNote = navController::navigateToEditPromissoryNote,
                onNavigateToSettings = navController::navigateToSettings,
                onNavigateToSaldoActualSettings = navController::navigateToSaldoActualSettings,
                onNavigateToAddCategory = navController::navigateToAddCategory,
                onNavigateToEditCategory = navController::navigateToEditCategory,
                onNavigateToAddBudget = {
                    if (it != null) {
                        navController.navigateToAddOneBudget(it)
                    } else {
                        navController.navigateToAddOneBudget()
                    }
                },
                onExportCategoryRequested = {
                    val categoryName = it.name
                    val categoryId = it.id ?: 0
                    mainViewModel.startActivityToExportDetails(
                        "CategoryDetails $categoryName.csv",
                        categoryId
                    )
                },
                onDataLoaded = onDataLoaded
            )
            screenAddAccount(
                viewModelAddAccount = mainViewModel.viewModelAddAccount,
                onNavigateToAddPerson = navController::navigateToAddPerson,
                onNavigateUp = navController::navigateUp,
                onNavigateToSettings = navController::navigateToSettings
            )
            screenEditAccount(
                viewModelEditAccount = mainViewModel.viewModelEditAccount,
                onNavigateUp = navController::navigateUp,
                onNavigateToSettings = navController::navigateToSettings,
                onNavigateToAddPerson = navController::navigateToAddPerson
            )
            screenAddPerson(
                viewModelAddPerson = mainViewModel.viewModelAddPerson,
                onNavigateUp = navController::navigateUp
            )
            screenEditPerson(
                viewModelEditPerson = mainViewModel.viewModelEditPerson,
                onNavigateUp = navController::navigateUp
            )
            screenAddTransaction(
                viewModelAddTransaction = mainViewModel.viewModelAddTransaction,
                onNavigateUp = { navController.navigateUpOrClose { onCloseApp() } },
                onNavigateToAddAccount = navController::navigateToAddAccount,
                onDataLoaded = onDataLoaded
            )
            screenAddPromissoryNote(
                viewModelAddPromissoryNote = mainViewModel.viewModelAddPromissoryNote,
                onNavigateUp = { navController.navigateUpOrClose(onCloseApp) },
                onNavigateToAddPerson = navController::navigateToAddPerson,
                onDataLoaded = onDataLoaded
            )
            screenEditTransaction(
                viewModelEditTransaction = mainViewModel.viewModelEditTransaction,
                onNavigateUp = navController::navigateUp,
                onNavigateToAddAccount = navController::navigateToAddAccount
            )
            screenEditPromissoryNote(
                viewModelEditPromissoryNote = mainViewModel.viewModelEditPromissoryNote,
                onNavigateUp = navController::navigateUp
            )
            screenSettings(
                viewModelSettings = mainViewModel.viewModelSettings,
                onNavigateUp = navController::navigateUp,
                onNavigateToAddAccount = navController::navigateToAddAccount,
                onNavigateToAddPerson = navController::navigateToAddPerson,
                onNavigateToAddBudget = navController::navigateToAddOneBudget,
                onNavigateToAddCategory = navController::navigateToAddCategory,
                onNavigateToBudget = navController::navigateToEditBudget,
                onExportDataRequested = {
                    mainViewModel.startActivityToSaveData("backup.gazip")
                },
                onImportDataRequested = {
                    mainViewModel.startActivityToLoadData()
                }
            )
            screenSaldoActualSettings(viewModelSaldoActualSettings = mainViewModel.viewModelSaldoActualSettings)
            screenAddCategory(
                viewModelAddCategory = mainViewModel.viewModelAddCategory,
                onNavigateUp = navController::navigateUp
            )
            screenEditCategory(
                viewModelEditCategory = mainViewModel.viewModelEditCategory,
                onNavigateUp = navController::navigateUp,
                onNavigateToEditOneBudgetRequested = navController::navigateToEditOneBudget,
                onNavigateToAddOneBudgetRequested = {
                    navController.navigateToAddOneBudget(
                        it.id ?: 0
                    )
                }
            )
            screenAccountDetail(
                viewModelAccountDetail = mainViewModel.viewModelAccountDetail,
                onNavigateUp = navController::navigateUp,
                onNavigateToEditAccount = navController::navigateToEditAccount,
                onNavigateToEditTransaction = navController::navigateToEditTransaction,
                onNavigateToAddTransaction = navController::navigateToAddTransaction
            )
            screenPersonDetail(
                viewModelPersonDetail = mainViewModel.viewModelPersonDetail,
                onNavigateUp = navController::navigateUp,
                onNavigateToEditTransaction = navController::navigateToEditTransaction,
                onNavigateToEditPerson = navController::navigateToEditPerson
            )
            screenEditBudget(
                viewModelEditBudget = mainViewModel.viewModelEditBudget,
                onNavigateToOneBudgetDetail = navController::navigateToOneBudgetDetail,
                onNavigateToAddOneBudget = navController::navigateToAddOneBudget,
                onNavigateToOneBudgetEdit = navController::navigateToEditOneBudget
            )
            screenAddOneBudget(
                viewModelAddOneBudget = mainViewModel.viewModelAddOneBudget,
                onNavigateUp = navController::navigateUp
            )
            screenOneBudgetDetail(viewModelOneBudgetDetail = mainViewModel.viewModelOneBudgetDetail)
            screenEditOneBudget(
                viewModel = mainViewModel.viewModelEditOneBudget,
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

fun NavController.navigateUpOrClose(onCloseApp: () -> Unit) {
    val lastScreenIsEmpty = previousBackStackEntry
        ?.destination
        ?.route
        ?.let { it == INITIALSCREENROUTE }
        ?: true
    navigateUp()
    if (lastScreenIsEmpty) onCloseApp()
}