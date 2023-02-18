package com.example.gazege

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gazege.core.AppDatabase
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.ui.fragments.AccountFormFragment
import com.example.gazege.ui.fragments.MainFragment
import com.example.gazege.ui.fragments.PersonFormFragment
import com.example.gazege.ui.fragments.TransactionFormFragment
import com.example.gazege.ui.theme.GazegeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    private val repository: AppRepository by lazy {
        AppRepository(
            personDao = database.personDao(),
            accountDao = database.accountDao(),
            transactionDao = database.transactionDao()
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
                val personList by mainViewModel.allPerson.observeAsState(emptyList())
                val accountList by mainViewModel.allAccount.observeAsState(emptyList())
                val transactionList by mainViewModel.allTransactions.observeAsState(emptyList())
                var navPosition: NavPosition by rememberSaveable {
                    mutableStateOf(NavPosition.TRANSACCIONES)
                }
                val navController = rememberNavController()
                // A surface container using the 'background' color from the theme
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()

                DisposableEffect(systemUiController, useDarkIcons) {
                    // Update all of the system bar colors to be transparent, and use
                    // dark icons if we're in light theme
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent, darkIcons = useDarkIcons
                    )

                    // setStatusBarColor() and setNavigationBarColor() also exist

                    onDispose {}
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
                                personList,
                                onAddPersonRequested = {
                                    navController.navigate("addPerson")
                                },
                                delPerson = { mainViewModel.deletePerson(it) },
                                accountList = accountList,
                                onAddAccountRequested = {
                                    navController.navigate(
                                        route = "addAccount"
                                    )
                                },
                                delAccount = { mainViewModel.deleteAccount(it) },
                                transactionList = transactionList,
                                onAddTransactionRequested = {
                                    navController.navigate(route = "addTransaction")
                                },
                                delTransaction = { mainViewModel.deleteTransaction(it) },
                                navPosition = navPosition,
                                onNavStatusChanged = {
                                    navPosition = it
                                },
                                sheetState = sheetState,
                                onEditTransactionRequested = {
                                    navController.navigate("editTransaction/${it.id}")
                                },
                                onEditPersonRequested = {
                                    navController.navigate("editPerson/${it.id}")
                                },
                                onEditAccountRequested = {
                                    navController.navigate(route = "editAccount/${it.id}")
                                },
                                snackbarHostState = snackbarHostState
                            )
                        }
                        composable("addAccount") {
                            AccountFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                personList = personList
                                    .map {
                                        it.person
                                    },
                                onPersonAddRequested = {
                                    navController.navigate("addPerson")
                                },
                                onAccountAndOwnerAdd = {
                                    mainViewModel.insertAccount(it)
                                    navController.navigateUp()
                                }
                            )
                        }
                        composable(
                            "editAccount/{accountId}",
                            arguments = listOf(navArgument("accountId") { type = NavType.IntType })
                        ) { navStack ->
                            val accountId = navStack.arguments?.getInt("accountId")
                            val selectedAccountAndOwner = mainViewModel
                                .allAccount
                                .value
                                ?.firstOrNull { it.account.id == accountId }
                                ?.let {
                                    AccountAndOwner(
                                        account = it.account,
                                        owner = it.owner
                                    )
                                }
                            AccountFormFragment(
                                personList = personList.map { it.person },
                                onPersonAddRequested = { navController.navigate("addPerson") },
                                onAccountAndOwnerAdd = {

                                },
                                accountAndOwner = selectedAccountAndOwner
                            )
                        }
                        composable("addPerson") {
                            PersonFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                onPersonAddRequested = {
                                    mainViewModel.insertPerson(it)
                                    navController.navigateUp()
                                }
                            )
                        }
                        composable(
                            "editPerson/{personId}",
                            arguments = listOf(navArgument("personId") { type = NavType.IntType })
                        ) { navBack ->
                            val personId = navBack.arguments?.getInt("personId")
                            val selectedPerson = mainViewModel
                                .allPerson
                                .value
                                ?.firstOrNull { it.person.id == personId }
                                ?.person
                            PersonFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                onPersonAddRequested = {

                                },
                                person = selectedPerson
                            )
                        }
                        composable("addTransaction") {
                            TransactionFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                accountList = accountList.map { it.account },
                                onAccountAddRequested = { navController.navigate("addAccount") },
                                onTransactionAndAccountsAdd = {
                                    mainViewModel.insertTransaction(it)
                                    navController.navigateUp()
                                }
                            )
                        }
                        composable(
                            "editTransaction/{transactionId}",
                            arguments = listOf(navArgument("transactionId") {
                                type = NavType.IntType
                            })
                        ) { navBackStackEntry ->
                            val transactionId = navBackStackEntry.arguments?.getInt("transactionId")
                            val selectedTransactionAndAccounts = mainViewModel
                                .allTransactions
                                .value
                                ?.firstOrNull { it.transaction.id == transactionId }
                            TransactionFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                accountList = accountList.map { it.account },
                                onAccountAddRequested = { navController.navigate("addAccount") },
                                onTransactionAndAccountsAdd = {
                                },
                                transactionAndAccounts = selectedTransactionAndAccounts
                            )
                        }
                    }
                }
            }
        }
    }
}