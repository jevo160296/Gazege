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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gazege.core.AppDatabase
import com.example.gazege.core.AppRepository
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
                ){
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                    ){
                        composable("main"){
                            MainFragment(
                                personList,
                                onAddPersonRequested = {
                                    navController.navigate("addPerson")
                                },
                                delPerson = {
                                    mainViewModel.deletePerson(it)
                                },
                                accountList,
                                onAddAccountRequested = {
                                    navController.navigate(
                                        route = "addAccount"
                                    )
                                },
                                delAccount = {
                                    mainViewModel.deleteAccount(it)
                                },
                                transactionList,
                                onAddTransactionRequested = {
                                    navController.navigate(route="addTransaction")
                                },
                                delTransaction = {
                                    mainViewModel.deleteTransaction(it)
                                },
                                navPosition = navPosition,
                                onNavStatusChanged = {
                                    navPosition = it
                                })
                        }
                        composable("addAccount"){
                            AccountFormFragment(
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
                        composable("addPerson"){
                            PersonFormFragment(
                                onPersonAddRequested = {
                                    mainViewModel.insertPerson(it)
                                    navController.navigateUp()
                                }
                            )
                        }
                        composable("addTransaction"){
                            TransactionFormFragment(
                                accountList = accountList.map { it.account },
                                onAccountAddRequested = { navController.navigate("addAccount") },
                                onTransactionAndAccountsAdd = {
                                    mainViewModel.insertTransaction(it)
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}