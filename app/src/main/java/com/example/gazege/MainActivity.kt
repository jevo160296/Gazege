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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.AccountAndOwnerWithTransactionsAndPockets
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.core.entities.TransactionAndAccounts
import com.example.gazege.ui.fragments.AccountFormFragment
import com.example.gazege.ui.fragments.MainFragment
import com.example.gazege.ui.fragments.PersonFormFragment
import com.example.gazege.ui.fragments.SaldoActualSettings
import com.example.gazege.ui.fragments.SettingsFragment
import com.example.gazege.ui.fragments.TransactionFormFragment
import com.example.gazege.ui.theme.GazegeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import java.time.LocalDate

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
                val allTransactions by mainViewModel.allTransactions.observeAsState(emptyList())
                val transactionList by mainViewModel.rangeTransactions.observeAsState(emptyList())
                val range by mainViewModel.range.observeAsState(
                    Pair(
                        LocalDate.now(),
                        LocalDate.now()
                    )
                )
                val incomeAccount by mainViewModel.incomeAccount.observeAsState()
                val outcomeAccount by mainViewModel.outcomeAccount.observeAsState()
                val principalPersonState = mainViewModel.principalPerson.observeAsState()
                val principalPerson = principalPersonState.value

                val accountAndOwnerWithTransactions = AccountAndOwnerWithTransactions
                    .from(accountList, personList, allTransactions)
                val accountAndOwnerWithTransactionsAndPockets = accountAndOwnerWithTransactions
                    .map {
                        AccountAndOwnerWithTransactionsAndPockets
                            .from(it, accountAndOwnerWithTransactions)
                    }
                val personWithAccounts =
                    PersonWithAccounts.from(personList, accountAndOwnerWithTransactionsAndPockets)
                val transactionAndAccounts =
                    TransactionAndAccounts.from(transactionList, accountList)

                var navPosition: NavPosition by rememberSaveable {
                    mutableStateOf(NavPosition.TRANSACCIONES)
                }
                val navController = rememberNavController()
                // A surface container using the 'background' color from the theme
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                val coroutineScope = rememberCoroutineScope()

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
                                personWithAccounts,
                                onAddPersonRequested = {
                                    navController.navigate("addPerson")
                                },
                                delPerson = { mainViewModel.deletePerson(it) },
                                accountList = accountAndOwnerWithTransactions,
                                onAddAccountRequested = {
                                    navController.navigate(
                                        route = "addAccount"
                                    )
                                },
                                delAccount = { mainViewModel.deleteAccount(it) },
                                transactionList = transactionAndAccounts,
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
                                    navController.navigate(route = "addTransaction/" +
                                            "${
                                                date.let {
                                                    it.year * 10000 + it.monthValue * 100 + it.dayOfMonth
                                                }
                                            }"
                                    )
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
                                snackbarHostState = snackbarHostState,
                                range = range,
                                onRangeChanged = { startDate, endDate ->
                                    mainViewModel.updateRange(startDate, endDate)
                                },
                                onSettingsClicked = {
                                    navController.navigate("settings")
                                },
                                principalPerson = principalPerson,
                                onSaldoActualClick = {
                                    navController.navigate("saldoActualSettings")
                                }
                            )
                        }
                        composable("addAccount") {
                            AccountFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                personList = personList,
                                onPersonAddRequested = {
                                    navController.navigate("addPerson")
                                },
                                onAccountAndOwnerAdd = { account, newBalance, snackBarHostState, incomeAccountId, outcomeAccountId ->
                                    val accountOwnerIdList = accountList.map {
                                        Pair(it.name, it.ownerId)
                                    }
                                    val accountOwnerId = Pair(account.name, account.ownerId)
                                    val sePuedeAgregar = accountOwnerId !in accountOwnerIdList
                                    if (sePuedeAgregar) {
                                        mainViewModel.insertAccount(
                                            account,
                                            onErrorAction = {
                                                coroutineScope.launch {
                                                    snackBarHostState.showSnackbar("Error añadiento cuenta $it")
                                                }
                                            },
                                            onCompleitionAction = { addedId ->
                                                if (incomeAccountId != null && outcomeAccountId != null) {
                                                    val valorAjuste = newBalance
                                                    mainViewModel.realizarAjuste(
                                                        accountId = addedId.toInt(),
                                                        amount = valorAjuste,
                                                        incomeAccountId = incomeAccountId,
                                                        outcomeAccountId = outcomeAccountId
                                                    )
                                                }
                                            }).invokeOnCompletion {
                                            if (it == null) {
                                                navController.navigateUp()
                                            }
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackBarHostState.showSnackbar("Las personas no pueden tener cuentas con nombres repetidos")
                                        }
                                    }
                                },
                                currentBalance = 0.0,
                                incomeAccount = incomeAccount,
                                outcomeAccount = outcomeAccount,
                                onSetIncomeOutcomeAccount = {
                                    navController.navigate("settings")
                                },
                                accountAndOwnerList = accountAndOwnerWithTransactions.map {
                                    AccountAndOwner(it.account, it.owner)
                                }
                            )
                        }
                        composable(
                            "editAccount/{accountId}",
                            arguments = listOf(navArgument("accountId") { type = NavType.IntType })
                        ) { navStack ->
                            val accountId = navStack.arguments?.getInt("accountId")
                            val selectedAccountAndOwnerWithTransactions =
                                accountAndOwnerWithTransactionsAndPockets
                                    .firstOrNull { it.accountAndOwnerWithTransactions.account.id == accountId }
                            val selectedAccountAndOwnerBalance =
                                selectedAccountAndOwnerWithTransactions?.let {
                                    AccountDao.getTotal(
                                        it.accountAndOwnerWithTransactions,
                                        null,
                                        null
                                    ) +
                                            AccountDao.getChildrenTotal(it, null, null)
                                }
                            val selectedAccountAndOwner = selectedAccountAndOwnerWithTransactions
                                ?.let {
                                    AccountAndOwner(
                                        account = it.accountAndOwnerWithTransactions.account,
                                        owner = it.accountAndOwnerWithTransactions.owner
                                    )
                                }
                            AccountFormFragment(
                                personList = personList,
                                itemSpacing = 8.dp,
                                contentPadding = PaddingValues(8.dp),
                                onPersonAddRequested = { navController.navigate("addPerson") },
                                onAccountAndOwnerAdd = { account, newBalance, snackBarHostState, incomeAccountId, outcomeAccountId ->
                                    val accountOwnerIdList = accountList
                                        .filter { it.id != account.id }
                                        .map { Pair(it.name, it.ownerId) }
                                    val accountOwnerId = Pair(account.name, account.ownerId)
                                    val sePuedeAgregar = accountOwnerId !in accountOwnerIdList
                                    if (sePuedeAgregar) {
                                        mainViewModel.updateAccount(
                                            account,
                                            onErrorAction = {
                                                coroutineScope.launch {
                                                    snackBarHostState.showSnackbar("Error añadiendo la cuenta: $it")
                                                }
                                            },
                                            onCompleitionAction = { addedId ->
                                                if (incomeAccountId != null && outcomeAccountId != null) {
                                                    val valorAjuste =
                                                        newBalance - (selectedAccountAndOwnerBalance
                                                            ?: 0.0)
                                                    mainViewModel.realizarAjuste(
                                                        accountId = addedId.toInt(),
                                                        amount = valorAjuste,
                                                        incomeAccountId = incomeAccountId,
                                                        outcomeAccountId = outcomeAccountId
                                                    )
                                                }
                                            }
                                        ).invokeOnCompletion {
                                            if (it == null) {
                                                navController.navigateUp()
                                            }
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackBarHostState.showSnackbar("Las personas no pueden tener cuentas con nombres repetidos")
                                        }
                                    }
                                },
                                accountAndOwner = selectedAccountAndOwner,
                                currentBalance = selectedAccountAndOwnerBalance ?: 0.0,
                                incomeAccount = incomeAccount,
                                outcomeAccount = outcomeAccount,
                                onSetIncomeOutcomeAccount = {
                                    navController.navigate("settings")
                                },
                                accountAndOwnerList = accountAndOwnerWithTransactions.map {
                                    AccountAndOwner(it.account, it.owner)
                                }
                            )
                        }
                        composable("addPerson") {
                            PersonFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                onPersonAddRequested = { person, snackBarHostSate ->
                                    val namesList =
                                        personList.map { persona -> persona.name }
                                    val sePuedeAgregar = person.name !in namesList
                                    if (sePuedeAgregar) {
                                        mainViewModel.insertPerson(person, onErrorAction = {
                                            coroutineScope.launch {
                                                snackBarHostSate.showSnackbar("Error agregando a la persona: $it")
                                            }
                                        }).invokeOnCompletion {
                                            if (it == null) {
                                                navController.navigateUp()
                                            }
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackBarHostSate.showSnackbar("Error, nombre repetido.")
                                        }
                                    }
                                }
                            )
                        }
                        composable(
                            "editPerson/{personId}",
                            arguments = listOf(navArgument("personId") { type = NavType.IntType })
                        ) { navBack ->
                            val personId = navBack.arguments?.getInt("personId")
                            val selectedPerson = personList
                                .firstOrNull { it.id == personId }
                            PersonFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                onPersonAddRequested = { person, snackBarHostSate ->
                                    val namesList =
                                        personList.map { persona -> persona.name }
                                    val sePuedeEditar = person.name !in namesList
                                    if (sePuedeEditar) {
                                        mainViewModel.updatePerson(person, onErrorAction = {
                                            coroutineScope.launch {
                                                snackBarHostSate.showSnackbar("Error editando persona $it")
                                            }
                                        }).invokeOnCompletion {
                                            if (it == null) {
                                                navController.navigateUp()
                                            }
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackBarHostSate.showSnackbar("Error, nombre repetido.")
                                        }
                                    }
                                },
                                person = selectedPerson
                            )
                        }
                        composable(
                            "addTransaction/{yearmonthday}",
                            arguments = listOf(navArgument("yearmonthday") {
                                type = NavType.IntType
                            })
                        ) { navBackStackEntry ->
                            val yearMonthDay = navBackStackEntry.arguments?.getInt("yearmonthday")
                                ?: LocalDate.now().let {
                                    it.year * 100 + it.monthValue
                                }
                            TransactionFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                accountList = accountAndOwnerWithTransactions.map {
                                    AccountAndOwner(
                                        it.account,
                                        it.owner
                                    )
                                },
                                onAccountAddRequested = { navController.navigate("addAccount") },
                                onTransactionAndAccountsAdd = {
                                    mainViewModel.insertTransaction(it)
                                    navController.navigateUp()
                                },
                                defaultDate = LocalDate.of(
                                    yearMonthDay.div(10000),
                                    yearMonthDay.mod(10000).div(100),
                                    yearMonthDay.mod(100)
                                ),
                                personList = personList
                            )
                        }
                        composable(
                            "editTransaction/{transactionId}",
                            arguments = listOf(navArgument("transactionId") {
                                type = NavType.IntType
                            })
                        ) { navBackStackEntry ->
                            val transactionId = navBackStackEntry.arguments?.getInt("transactionId")
                            val selectedTransactionAndAccounts = transactionAndAccounts
                                .firstOrNull { it.transaction.id == transactionId }
                            TransactionFormFragment(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                accountList = accountAndOwnerWithTransactions.map {
                                    AccountAndOwner(
                                        it.account,
                                        it.owner
                                    )
                                },
                                onAccountAddRequested = { navController.navigate("addAccount") },
                                onTransactionAndAccountsAdd = {
                                    mainViewModel.updateTransaction(it)
                                    navController.navigateUp()
                                },
                                transactionAndAccounts = selectedTransactionAndAccounts,
                                personList = personList
                            )
                        }
                        composable("settings") {
                            SettingsFragment(
                                personList = personList,
                                principalPerson = principalPerson,
                                onPrincipalPersonChanged = {
                                    if (principalPerson != null) {
                                        mainViewModel.updatePerson(
                                            principalPerson.copy(importance = null)
                                        ) {}
                                    }
                                    mainViewModel.updatePerson(it.copy(importance = 1)) {}
                                },
                                onNavigateUpRequested = {
                                    navController.navigateUp()
                                },
                                onAddPersonRequested = { navController.navigate("addPerson") },
                                accountList = accountAndOwnerWithTransactions.map {
                                    AccountAndOwner(
                                        it.account,
                                        it.owner
                                    )
                                },
                                incomeAccount = incomeAccount,
                                outcomeAccount = outcomeAccount,
                                onAddAccountRequested = { navController.navigate("addAccount") },
                                onIncomeOutcomeAccountChanged = { newIncome, newOutcome ->
                                    val castedIncomeAccount = incomeAccount
                                    val castedOutcomeAccount = outcomeAccount
                                    if (castedIncomeAccount != null) {
                                        mainViewModel.updateAccount(
                                            castedIncomeAccount.copy(
                                                isIncome = false
                                            ), onCompleitionAction = {}, onErrorAction = {})
                                    }
                                    if (castedOutcomeAccount != null) {
                                        mainViewModel.updateAccount(
                                            castedOutcomeAccount.copy(
                                                isOutcome = false
                                            ), onCompleitionAction = {}, onErrorAction = {})
                                    }
                                    if (newIncome != null) {
                                        mainViewModel.updateAccount(
                                            newIncome.copy(
                                                isIncome = true,
                                                isOutcome = false
                                            ), onErrorAction = {}, onCompleitionAction = {})
                                    }
                                    if (newOutcome != null) {
                                        mainViewModel.updateAccount(
                                            newOutcome.copy(
                                                isIncome = false,
                                                isOutcome = true
                                            ), onErrorAction = {}, onCompleitionAction = {})
                                    }
                                }
                            )
                        }
                        composable("saldoActualSettings") {
                            var saving: Int by remember {
                                mutableStateOf(0)
                            }
                            SaldoActualSettings(
                                accountAndOwnerWithTransactions.filter { it.owner.id == principalPerson?.id },
                                saving = saving
                            ) { account, nuevoEstado ->
                                saving += 1
                                coroutineScope.launch {
                                    mainViewModel.updateAccount(
                                        account = account.copy(includedInTotal = nuevoEstado),
                                        onErrorAction = {},
                                        onCompleitionAction = {}).join()
                                }.invokeOnCompletion {
                                    saving -= 1
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}