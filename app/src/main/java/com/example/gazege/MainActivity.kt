package com.example.gazege

import android.content.res.Configuration
import android.database.sqlite.SQLiteConstraintException
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.gazege.core.entities.*
import com.example.gazege.ui.navigation.addTransactionRoute
import com.example.gazege.ui.screens.*
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.*
import com.example.gazege.ui.views.account.AccountDetail
import com.example.gazege.ui.views.category.CategoryForm
import com.example.gazege.ui.views.person.PersonDetail
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                val allPerson by mainViewModel.allPerson.observeAsState(emptyList())
                val allAccount by mainViewModel.allAccount.observeAsState(emptyList())
                val allTransactions by mainViewModel.allTransactions.observeAsState(emptyList())
                val categories by mainViewModel.categories.observeAsState(emptyList())
                val range by mainViewModel.range.observeAsState(
                    Pair(
                        LocalDate.now(),
                        LocalDate.now()
                    )
                )
                val incomeAccount by mainViewModel.incomeAccount.observeAsState()
                val outcomeAccount by mainViewModel.outcomeAccount.observeAsState()
                val personFilterValue by mainViewModel.personFilterValue.observeAsState(false)
                val principalPerson by mainViewModel.principalPerson.observeAsState()
                val principalPersonWithAccounts by mainViewModel.principalPersonWithAccounts.observeAsState()

                val accountAndOwnerWithTransactions by mainViewModel.accountAndOwnerWithTransactions.observeAsState(
                    emptyList()
                )
                val accountAndOwnerWithTransactionsAndPockets by mainViewModel.accountAndOwnerWithTransactionsAndPockets.observeAsState(
                    emptyList()
                )
                val personWithAccounts by mainViewModel.personWithAccounts.observeAsState(emptyList())
                val filteredTransactionAndAccountsAndCategory by mainViewModel.filteredTransactionAndAccountsAndCategory.observeAsState(
                    emptyList()
                )
                val allTransactionAndAccountsAndCategory by mainViewModel.allTransactionAndAccountsAndCategory.observeAsState(
                    emptyList()
                )
                val categoriesWithSubCategories by mainViewModel.categoriesWithSubCategories.observeAsState(
                    emptyList()
                )

                var navPosition: NavPosition by rememberSaveable {
                    mutableStateOf(NavPosition.TRANSACCIONES)
                }
                val navController = rememberNavController()
                // A surface container using the 'background' color from the theme
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                val coroutineScope = rememberCoroutineScope()

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
                            MainScreen(
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
                                    navController.navigate("personDetail/${personId}")
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
                                    navController.navigate("accountDetail/${accountId}")
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
                                    navController.navigate(route = addTransactionRoute(date, it))
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
                                    navController.navigate("settings")
                                },
                                onSaldoActualClick = {
                                    navController.navigate("saldoActualSettings")
                                }
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
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                        screenEditAccount(
                            viewModel = mainViewModel,
                            onNavigateUp = navController::navigateUp,
                            onNavigateToSettings = { navController.navigate("settings") },
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
                        composable(
                            "addTransaction/{yearmonthday}/{transactionaction}",
                            arguments = listOf(
                                navArgument("yearmonthday") {
                                    type = NavType.IntType
                                },
                                navArgument("transactionaction") {
                                    type = NavType.StringType
                                }
                            )
                        ) { navBackStackEntry ->
                            val yearMonthDay = navBackStackEntry.arguments?.getInt("yearmonthday")
                                ?: LocalDate.now().let {
                                    it.year * 100 + it.monthValue
                                }
                            val transactionActionName =
                                navBackStackEntry.arguments?.getString("transactionaction")
                            val transactionAction =
                                transactionActionName?.let { AddTransactionAction.valueOf(it) }
                                    ?: AddTransactionAction.ADD_TRANSFER
                            val initialSourceAccount: Account? =
                                incomeAccount.takeIf { transactionAction == AddTransactionAction.ADD_INCOME }
                            val initialDestinationAccount: Account? =
                                outcomeAccount.takeIf { transactionAction == AddTransactionAction.ADD_EXPENSE }
                            val orderedAccounts =
                                if (transactionAction == AddTransactionAction.ADD_TRANSFER) {
                                    accountAndOwnerWithTransactions
                                } else {
                                    mainViewModel.accountAndOwnerWithTransactionsUserFirst.observeAsState(
                                        emptyList()
                                    ).value
                                }
                            TransactionFormScreen(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                accountList = orderedAccounts.map {
                                    AccountAndOwner(
                                        it.account,
                                        it.owner
                                    )
                                },
                                personList = allPerson,
                                categoryList = categories,
                                defaultDate = LocalDate.of(
                                    yearMonthDay.div(10000),
                                    yearMonthDay.mod(10000).div(100),
                                    yearMonthDay.mod(100)
                                ),
                                fixedSourceAccount = initialSourceAccount,
                                fixedDestinationAccount = initialDestinationAccount,
                                onAccountAddRequested = { navController.navigateToAddAccount() }
                            ) {
                                mainViewModel.insertTransaction(it)
                                navController.navigateUp()
                            }
                        }
                        composable(
                            "editTransaction/{transactionId}",
                            arguments = listOf(navArgument("transactionId") {
                                type = NavType.IntType
                            })
                        ) { navBackStackEntry ->
                            val transactionId = navBackStackEntry.arguments?.getInt("transactionId")
                            val selectedTransactionAndAccounts =
                                filteredTransactionAndAccountsAndCategory
                                    .firstOrNull { it.transaction.id == transactionId }
                            TransactionFormScreen(
                                contentPadding = PaddingValues(8.dp),
                                itemSpacing = 8.dp,
                                transactionAndAccounts = selectedTransactionAndAccounts?.toTransactionAndAccounts(),
                                accountList = accountAndOwnerWithTransactions.map {
                                    AccountAndOwner(
                                        it.account,
                                        it.owner
                                    )
                                },
                                personList = allPerson,
                                categoryList = categories,
                                onAccountAddRequested = { navController.navigateToAddAccount() }
                            ) {
                                mainViewModel.updateTransaction(it)
                                navController.navigateUp()
                            }
                        }
                        composable("settings") {
                            SettingsFragment(
                                personList = allPerson,
                                principalPerson = principalPerson,
                                onPrincipalPersonChanged = {
                                    val notNullPrincipalPerson = principalPerson
                                    if (notNullPrincipalPerson != null) {
                                        mainViewModel.updatePerson(
                                            notNullPrincipalPerson.copy(importance = null)
                                        ) {}
                                    }
                                    mainViewModel.updatePerson(it.copy(importance = 1)) {}
                                },
                                onNavigateUpRequested = {
                                    navController.navigateUp()
                                },
                                onAddPersonRequested = { navController.navigateToAddPerson() },
                                accountList = accountAndOwnerWithTransactions.map {
                                    AccountAndOwner(
                                        it.account,
                                        it.owner
                                    )
                                },
                                incomeAccount = incomeAccount,
                                outcomeAccount = outcomeAccount,
                                onAddAccountRequested = { navController.navigateToAddAccount() },
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
                                },
                                onEditCategoriesRequested = {
                                    navController.navigate("editCategories")
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
                        composable("editCategories") {
                            EditarCategorias(
                                categoriesWithSubCategories,
                                onAddCategoryRequested = {
                                    navController.navigate("addCategory")
                                },
                                onEditCategoryRequested = {
                                    navController.navigate("editCategory/${it.category.id}")
                                },
                                onDeleteCategoryRequested = { mainViewModel.deleteCategory(it.category) }
                            )
                        }
                        composable("addCategory") {
                            CategoryForm(
                                null,
                                categories,
                                onCategorySave = { category, snackbar ->
                                    mainViewModel.insertCategory(
                                        category,
                                        onCompleitionAction = {
                                            navController.navigateUp()
                                        }
                                    ) { error ->
                                        val msg = when (error) {
                                            is SQLiteConstraintException -> if (category.name in categories.map { it.name }) {
                                                "${category.name} ya existe."
                                            } else {
                                                "CONSTRAINT ERROR"
                                            }
                                            else -> error.toString()
                                        }
                                        coroutineScope.launch {
                                            snackbar.showSnackbar("Error agregando ${category.name}: \n$msg")
                                        }
                                    }
                                }
                            )
                        }
                        composable(
                            "editCategory/{categoryId}",
                            arguments = listOf(
                                navArgument("categoryId") {
                                    type = NavType.IntType
                                }
                            )
                        ) { navStack ->
                            val categoryId = navStack.arguments?.getInt("categoryId")
                            val category = categories.firstOrNull { it.id == categoryId }
                            CategoryForm(
                                category,
                                categories,
                                onCategorySave = { newCategory, state ->
                                    mainViewModel.updateCategory(newCategory,
                                        onCompleitionAction = {
                                            navController.navigateUp()
                                        }
                                    ) { error ->
                                        val msg = when (error) {
                                            is SQLiteConstraintException -> if (newCategory.name in categories.map { it.name }) {
                                                "${newCategory.name} ya existe."
                                            } else {
                                                "CONSTRAINT ERROR"
                                            }
                                            else -> error.toString()
                                        }
                                        coroutineScope.launch {
                                            state.showSnackbar("Error agregando ${newCategory.name}: \n$msg")
                                        }
                                    }
                                }
                            )
                        }
                        composable(
                            "accountDetail/{accountId}",
                            arguments = listOf(
                                navArgument("accountId") {
                                    type = NavType.IntType
                                }
                            )
                        ) { navStack ->
                            val data by mainViewModel.accountDetailData.observeAsState()
                            val accountId = navStack.arguments?.getInt("accountId")
                            val account = accountAndOwnerWithTransactionsAndPockets
                                .firstOrNull { it.accountAndOwnerWithTransactions.account.id == accountId }
                            if (account != null) {
                                var showGraphs by remember {
                                    mutableStateOf(false)
                                }
                                AccountDetail(
                                    accountAndOwnerWithTransactionsAndPockets = account,
                                    data = data,
                                    onDataUpdateRequested = { newAccount ->
                                        mainViewModel.updateAccountDetailData(account = newAccount)
                                    },
                                    onAction = { actionAccount, action ->
                                        when (action) {
                                            AccountAction.EDIT -> navController.navigateToEditAccount(
                                                accountId
                                            )
                                            AccountAction.DELETE -> {
                                                navController.navigateUp()
                                                mainViewModel.deleteAccount(actionAccount)
                                            }
                                        }
                                    },
                                    onTransactionAction = { transaction, action ->
                                        val transactionId = transaction.id
                                        when (action) {
                                            TransactionAction.EDIT -> navController.navigate("editTransaction/${transactionId}")
                                            TransactionAction.DELETE -> mainViewModel.deleteTransaction(
                                                transaction
                                            )
                                        }
                                    },
                                    showGraphs = showGraphs,
                                    onShowGraphsChanged = {
                                        coroutineScope.launch {
                                            withContext(Dispatchers.Default) {
                                                showGraphs = it
                                            }
                                        }
                                    }
                                )
                            } else {
                                Text("Cuenta vacía")
                            }
                        }
                        composable(
                            "personDetail/{personId}",
                            arguments = listOf(
                                navArgument("personId") {
                                    type = NavType.IntType
                                }
                            )
                        ) { navStack ->
                            val personId = navStack.arguments?.getInt("personId")
                            val person = allPerson.firstOrNull { it.id == personId }
                            if (person != null) {
                                PersonDetail(
                                    person = person,
                                    onPersonAction = { _, action ->
                                        when (action) {
                                            PersonAction.EDIT -> navController.navigateToEditPerson(
                                                personId
                                            )
                                            PersonAction.DELETE -> {
                                                navController.navigateUp()
                                                mainViewModel.deletePerson(person)
                                            }
                                        }
                                    },
                                    allTransactions = allTransactions,
                                    allAccounts = allAccount,
                                    allCategories = categories,
                                    onTransactionAction = { transaction, action ->
                                        val transactionId = transaction.id
                                        when (action) {
                                            TransactionAction.EDIT -> navController.navigate("editTransaction/${transactionId}")
                                            TransactionAction.DELETE -> mainViewModel.deleteTransaction(
                                                transaction
                                            )
                                        }
                                    }
                                )
                            } else {
                                Text(text = "Empty person")
                            }
                        }
                    }
                }
            }
        }
    }
}