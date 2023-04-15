package com.example.gazege

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.gazege.core.AppDatabase
import com.example.gazege.core.AppRepository
import com.example.gazege.ui.Settings
import com.example.gazege.ui.navigation.navigateToAccountDetail
import com.example.gazege.ui.navigation.navigateToAddAccount
import com.example.gazege.ui.navigation.navigateToAddCategory
import com.example.gazege.ui.navigation.navigateToAddOneBudget
import com.example.gazege.ui.navigation.navigateToAddPerson
import com.example.gazege.ui.navigation.navigateToAddTransaction
import com.example.gazege.ui.navigation.navigateToEditAccount
import com.example.gazege.ui.navigation.navigateToEditBudget
import com.example.gazege.ui.navigation.navigateToEditCategory
import com.example.gazege.ui.navigation.navigateToEditOneBudget
import com.example.gazege.ui.navigation.navigateToEditPerson
import com.example.gazege.ui.navigation.navigateToEditTransaction
import com.example.gazege.ui.navigation.navigateToEditarCategorias
import com.example.gazege.ui.navigation.navigateToOneBudgetDetail
import com.example.gazege.ui.navigation.navigateToPersonDetail
import com.example.gazege.ui.navigation.navigateToSaldoActualSettings
import com.example.gazege.ui.navigation.navigateToSettings
import com.example.gazege.ui.navigation.screenAccountDetail
import com.example.gazege.ui.navigation.screenAddAccount
import com.example.gazege.ui.navigation.screenAddCategory
import com.example.gazege.ui.navigation.screenAddOneBudget
import com.example.gazege.ui.navigation.screenAddPerson
import com.example.gazege.ui.navigation.screenAddTransaction
import com.example.gazege.ui.navigation.screenEditAccount
import com.example.gazege.ui.navigation.screenEditBudget
import com.example.gazege.ui.navigation.screenEditCategory
import com.example.gazege.ui.navigation.screenEditOneBudget
import com.example.gazege.ui.navigation.screenEditPerson
import com.example.gazege.ui.navigation.screenEditTransaction
import com.example.gazege.ui.navigation.screenEditarCategorias
import com.example.gazege.ui.navigation.screenMain
import com.example.gazege.ui.navigation.screenOneBudgetDetail
import com.example.gazege.ui.navigation.screenPersonDetail
import com.example.gazege.ui.navigation.screenSaldoActualSettings
import com.example.gazege.ui.navigation.screenSettings
import com.example.gazege.ui.theme.GazegeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    private val repository: AppRepository by lazy {
        AppRepository(
            personDao = database.personDao(),
            accountDao = database.accountDao(),
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            budgetDao = database.budgetDao()
        )
    }
    private val settings by lazy { Settings(this) }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository, settings)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GazegeTheme {
                // A surface container using the 'background' color from the theme
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()

                LaunchedEffect(systemUiController, useDarkIcons) {
                    // Update all of the system bar colors to be transparent, and use
                    // dark icons if we're in light theme
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent, darkIcons = useDarkIcons
                    )

                    // setStatusBarColor() and setNavigationBarColor() also exist
                }

                var modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()

                if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    modifier = modifier.navigationBarsPadding()
                }

                val navController = rememberNavController()

                Surface(
                    modifier = modifier, color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "main",
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
                            onNavigateToSaldoActualSettings = navController::navigateToSaldoActualSettings
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
                            onNavigateUp = navController::navigateUp,
                            onNavigateToAddAccount = navController::navigateToAddAccount
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
                            onNavigateToEditCategory = navController::navigateToEditCategory
                        )
                        screenAddCategory(
                            viewModel = mainViewModel,
                            onNavigateUp = navController::navigateUp
                        )
                        screenEditCategory(
                            viewModel = mainViewModel,
                            onNavigateUp = navController::navigateUp
                        )
                        screenAccountDetail(
                            viewModel = mainViewModel,
                            onNavigateUp = navController::navigateUp,
                            onNavigateToEditAccount = navController::navigateToEditAccount,
                            onNavigateToEditTransaction = navController::navigateToEditTransaction
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
            }
        }
    }
}