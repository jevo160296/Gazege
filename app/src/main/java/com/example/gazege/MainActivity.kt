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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gazege.core.AppDatabase
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.*
import com.example.gazege.ui.fragments.*
import com.example.gazege.ui.navigation.*
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    private val repository: AppRepository by lazy {
        AppRepository(
            personDao = database.personDao(),
            accountDao = database.accountDao(),
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao()
        )
    }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GazegeTheme {
                val range by mainViewModel.range.observeAsState(
                    Pair(
                        LocalDate.now(),
                        LocalDate.now()
                    )
                )
                val personFilterValue by mainViewModel.personFilterValue.observeAsState(false)
                val principalPersonWithAccounts by mainViewModel.principalPersonWithAccounts.observeAsState()

                val accountAndOwnerWithTransactions by mainViewModel.accountAndOwnerWithTransactions.observeAsState(
                    emptyList()
                )
                val personWithAccounts by mainViewModel.personWithAccounts.observeAsState(emptyList())
                val filteredTransactionAndAccountsAndCategory by mainViewModel.filteredTransactionAndAccountsAndCategory.observeAsState(
                    emptyList()
                )
                val allTransactionAndAccountsAndCategory by mainViewModel.allTransactionAndAccountsAndCategory.observeAsState(
                    emptyList()
                )
                var navPosition: NavPosition by rememberSaveable {
                    mutableStateOf(NavPosition.TRANSACCIONES)
                }
                val navController = rememberNavController()
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

                Surface(
                    modifier = modifier, color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                    ) {
                        composable("main") {
                            val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
                            val snackbarHostState = SnackbarHostState()
                            MainFragment(
                                personList = personWithAccounts,
                                accountList = accountAndOwnerWithTransactions,
                                allTransactionList = allTransactionAndAccountsAndCategory,
                                filteredTransactionList = filteredTransactionAndAccountsAndCategory,
                                principalPersonWithAccounts = principalPersonWithAccounts,
                                navPosition = navPosition,
                                range = range,
                                personFilterValue = personFilterValue,
                                sheetState = sheetState,
                                snackbarHostState = snackbarHostState,
                                delPerson = { mainViewModel.deletePerson(it) },
                                delAccount = { mainViewModel.deleteAccount(it) },
                                delTransaction = { mainViewModel.deleteTransaction(it) },
                                onAddPersonRequested = {
                                    navController.navigateToAddPerson()
                                },
                                onEditPersonRequested = {
                                    navController.navigateToEditPerson(it.id)
                                },
                                onPersonDetailRequested = {
                                    val personId = it.id
                                    navController.navigateToPersonDetail(personId)
                                },
                                onAddAccountRequested = {
                                    navController.navigate(
                                        route = "addAccount"
                                    )
                                },
                                onEditAccountRequested = {
                                    navController.navigate(route = "editAccount/${it.id}")
                                },
                                onAccountDetailRequested = {
                                    val accountId = it.id
                                    navController.navigateToAccountDetail(accountId)
                                },
                                onAddTransactionRequested = {
                                    val startDate = range.first
                                    val esMesActual =
                                        range.first?.withDayOfMonth(1) == LocalDate.now()
                                            .withDayOfMonth(1)
                                    val esMesPosterior =
                                        startDate != null &&
                                                startDate.withDayOfMonth(1) > LocalDate.now()
                                            .withDayOfMonth(1)
                                    val date = if (esMesActual || startDate == null) LocalDate.now()
                                    else if (esMesPosterior) startDate.withDayOfMonth(1) else
                                        startDate.withDayOfMonth(1).plusMonths(1L)
                                            .minusDays(1L)
                                    navController.navigateToAddTransaction(date, it)
                                },
                                onEditTransactionRequested = {
                                    navController.navigate("editTransaction/${it.id}")
                                },
                                onNavStatusChanged = {
                                    navPosition = it
                                },
                                onRangeChanged = { startDate, endDate ->
                                    mainViewModel.updateRange(startDate, endDate)
                                },
                                onSettingsClicked = {
                                    navController.navigateToSettings()
                                },
                                onSaldoActualClick = navController::navigateToSaldoActualSettings
                            ) {
                                mainViewModel.updatePersonFilterValue(
                                    it
                                )
                            }
                        }
                        screenAddAccount(
                            viewModel = mainViewModel,
                            onNavigateToAddPerson = { navController.navigateToAddPerson() },
                            onNavigateUp = { navController.navigateUp() },
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
                            onNavigateUp = { navController.navigateUp() }
                        )
                        screenEditPerson(
                            viewModel = mainViewModel,
                            onNavigateUp = { navController.navigateUp() }
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
                            onNavigateToEditCategories = navController::navigateToEditarCategorias
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
                    }
                }
            }
        }
    }
}