package com.example.gazege

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
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
import com.example.gazege.ui.theme.Shapes
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

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
                            val scope = rememberCoroutineScope()
                            var action by remember {
                                mutableStateOf({})
                            }
                            var nombreItem by remember {
                                mutableStateOf("")
                            }
                            ModalBottomSheetLayout(
                                sheetState = sheetState,
                                sheetShape = Shapes.medium,
                                sheetContent = {
                                    Column(
                                        Modifier.navigationBarsPadding()
                                    ) {
                                        Text(
                                            text = "Confirmar eliminación",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = "¿Confirma la eliminación de $nombreItem?"
                                        )
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    action()
                                                    action = {}
                                                    nombreItem = ""
                                                    scope.launch { sheetState.hide() }
                                                }) {
                                                Text(
                                                    text = "Si"
                                                )
                                            }
                                            TextButton(
                                                onClick = {
                                                    action = {}
                                                    nombreItem = ""
                                                    scope.launch { sheetState.hide() }
                                                }
                                            ) {
                                                Text(
                                                    text = "No"
                                                )
                                            }
                                        }
                                    }
                                }) {
                                MainFragment(
                                    personList,
                                    onAddPersonRequested = {
                                        navController.navigate("addPerson")
                                    },
                                    delPerson = {
                                        action = { mainViewModel.deletePerson(it) }
                                        nombreItem =
                                            "la persona ${it.name} sus cuentas y transacciones asociadas"
                                        scope.launch { sheetState.show() }
                                    },
                                    accountList,
                                    onAddAccountRequested = {
                                        navController.navigate(
                                            route = "addAccount"
                                        )
                                    },
                                    delAccount = {
                                        action = { mainViewModel.deleteAccount(it) }
                                        nombreItem =
                                            "la cuenta ${it.name} y sus transacciones asociadas"
                                        scope.launch { sheetState.show() }
                                    },
                                    transactionList,
                                    onAddTransactionRequested = {
                                        navController.navigate(route = "addTransaction")
                                    },
                                    delTransaction = {
                                        action = { mainViewModel.deleteTransaction(it) }
                                        nombreItem = "la transacción"
                                        scope.launch { sheetState.show() }
                                    },
                                    navPosition = navPosition,
                                    onNavStatusChanged = {
                                        navPosition = it
                                    })
                            }
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
                    }
                }
            }
        }
    }
}