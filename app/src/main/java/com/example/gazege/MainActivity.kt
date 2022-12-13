package com.example.gazege

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.core.AppDatabase
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.*
import com.example.gazege.ui.pages.AccountPage
import com.example.gazege.ui.pages.PersonPage
import com.example.gazege.ui.pages.TransactionPage
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.views.getAccountSample
import com.example.gazege.ui.views.getPersonSample
import com.example.gazege.ui.views.getTransactionSample
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.*
import kotlin.random.Random

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
                var navStatus by remember { mutableStateOf(NavStatus.TRANSACCIONES) }
                // A surface container using the 'background' color from the theme

                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()

                DisposableEffect(systemUiController, useDarkIcons) {
                    // Update all of the system bar colors to be transparent, and use
                    // dark icons if we're in light theme
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )

                    // setStatusBarColor() and setNavigationBarColor() also exist

                    onDispose {}
                }

                Surface(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Page(
                        personList,
                        addPerson = {
                            mainViewModel.insertPerson(it)
                        },
                        delPerson = {
                            mainViewModel.deletePerson(it)
                        },
                        accountList,
                        addAccount = {
                            mainViewModel.insertAccount(it)
                        },
                        delAccount = {
                            mainViewModel.deleteAccount(it)
                        },
                        transactionList,
                        addTransaction = {
                            mainViewModel.insertTransaction(it)
                        },
                        delTransaction = {
                            mainViewModel.deleteTransaction(it)
                        },
                        navStatus = navStatus,
                        onNavStatusChanged = {
                            navStatus = it
                        }
                    )
                }
            }
        }
    }
}

enum class NavStatus {
    PERSONS, CUENTAS, TRANSACCIONES
}

@Composable
fun Page(
    personList: List<Person>,
    addPerson: (Person) -> Unit,
    delPerson: (Person) -> Unit,
    accountList: List<AccountAndOwner>,
    addAccount: (Account) -> Unit,
    delAccount: (Account) -> Unit,
    transactionList: List<TransactionAndAccounts>,
    addTransaction: (Transaction) -> Unit,
    delTransaction: (Transaction) -> Unit,
    navStatus: NavStatus,
    onNavStatusChanged: (NavStatus) -> Unit
) {
    val transactionState = rememberLazyListState()
    val accountState = rememberLazyListState()
    val personState = rememberLazyListState()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val random = Random.nextInt(3)
                    val personAdd = random == 0
                    val transAdd = random == 1
                    val cuentaAdd = random == 2
                    val maxPersonasId = personList.maxOfOrNull { it.id ?: -1 } ?: -1
                    val maxAccountsId = accountList.maxOfOrNull { it.account.id ?: -1 } ?: -1
                    val maxTransactionsId =
                        transactionList.maxOfOrNull { it.transaction.id ?: -1 } ?: -1
                    if (personAdd) {
                        addPerson(Person(name = "Persona ${maxPersonasId + 1}"))
                    } else if (cuentaAdd && personList.isNotEmpty()) {
                        val cantPersonas = personList.size
                        val selectedPerson = Random.nextInt(cantPersonas)
                        addAccount(
                            Account(
                                name = "Account ${maxAccountsId + 1}",
                                initial_balance = 1.0,
                                ownerId = personList[selectedPerson].id ?: -1
                            )
                        )
                    } else if (transAdd && accountList.size >= 2) {
                        val cantAccounts = accountList.size
                        val selectedSourceAccount = Random.nextInt(cantAccounts)
                        val selectedDestinationAccount = Random.nextInt(cantAccounts)
                        addTransaction(
                            Transaction(
                                amount = 0.0,
                                description = "Trans ${maxTransactionsId + 1}",
                                sourceId = accountList[selectedSourceAccount].account.id ?: -1,
                                destinationId = accountList[selectedDestinationAccount].account.id
                                    ?: -1,
                                date = Date()
                            )
                        )
                    }
                },
                shape = Shapes.small
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_24),
                    contentDescription = "Add"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = false,
        backgroundColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = navStatus == NavStatus.CUENTAS,
                    onClick = { onNavStatusChanged(NavStatus.CUENTAS) },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_baseline_account_balance_wallet_24
                            ),
                            contentDescription = "Accounts"
                        )
                    }
                )
                NavigationBarItem(
                    selected = navStatus == NavStatus.TRANSACCIONES,
                    onClick = { onNavStatusChanged(NavStatus.TRANSACCIONES) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_home_24),
                            contentDescription = "Transactions"
                        )
                    }
                )
                NavigationBarItem(
                    selected = navStatus == NavStatus.PERSONS,
                    onClick = { onNavStatusChanged(NavStatus.PERSONS) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_person_24),
                            contentDescription = "Persons"
                        )
                    }
                )
            }
        }
    ) {
        val paddingValues = it.let {
            PaddingValues(
                top = it.calculateTopPadding(),
                bottom = it.calculateBottomPadding() + 90.dp,
                start = it.calculateStartPadding(LocalLayoutDirection.current) + 8.dp,
                end = it.calculateEndPadding(LocalLayoutDirection.current) + 8.dp
            )
        }
        Column {
            when (navStatus) {
                NavStatus.TRANSACCIONES -> {
                    TransactionPage(
                        modifier = Modifier.weight(1F),
                        transactionList = transactionList,
                        itemHolderPaddingValues = paddingValues,
                        state = transactionState
                    ) { transaction -> delTransaction(transaction) }
                }
                NavStatus.CUENTAS -> {
                    AccountPage(
                        modifier = Modifier.weight(1F),
                        accountList = accountList,
                        itemHolderPaddingValues = paddingValues,
                        state = accountState
                    ) { account -> delAccount(account) }
                }
                NavStatus.PERSONS -> {
                    PersonPage(
                        modifier = Modifier.weight(1F),
                        personList = personList,
                        itemHolderPaddingValues = paddingValues,
                        state = personState
                    ) { person -> delPerson(person) }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val personList = getPersonSample()
    val accounts = getAccountSample()
    val transactions = getTransactionSample()
    GazegeTheme(darkTheme = true) {
        Page(
            personList = personList,
            accountList = accounts,
            addPerson = {},
            addAccount = {},
            delPerson = {},
            transactionList = transactions,
            addTransaction = {},
            delAccount = {},
            delTransaction = {},
            navStatus = NavStatus.TRANSACCIONES,
            onNavStatusChanged = {}
        )
    }
}