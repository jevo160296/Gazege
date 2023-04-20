package com.example.gazege.ui.navigation

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.gazege.MainViewModel
import com.example.gazege.NavPosition
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.BudgetType
import com.example.gazege.ui.fragments.*
import com.example.gazege.ui.views.AccountAction
import com.example.gazege.ui.views.AddTransactionAction
import com.example.gazege.ui.views.PersonAction
import com.example.gazege.ui.views.TransactionAction
import com.example.gazege.ui.views.account.AccountDetail
import com.example.gazege.ui.views.budget.BudgetDetailView
import com.example.gazege.ui.views.budget.BudgetFormView
import com.example.gazege.ui.views.budget.EmptyBudgetDetailView
import com.example.gazege.ui.views.category.CategoryForm
import com.example.gazege.ui.views.person.PersonDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

val URI = "https://www.example.gazege"

fun NavController.navigateUpOrClose(
    onCloseApp: () -> Unit
) {
    navigateUp()
    if (this.backQueue.size <= 1) {
        onCloseApp()
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun NavGraphBuilder.screenMain(
    viewModel: MainViewModel,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToEditPerson: (Int?) -> Unit,
    onNavigateToPersonDetail: (Int?) -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToEditAccount: (Int?) -> Unit,
    onNavigateToAccountDetail: (Int?) -> Unit,
    onNavigateToAddTransaction: (date: LocalDate, action: AddTransactionAction) -> Unit,
    onNavigateToEditTransaction: (Int?) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSaldoActualSettings: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable("main") {
        val allPerson by viewModel.rememberAllPerson()
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()
        val filteredTransactionListItemDetails by viewModel.rememberFilteredTransactionListItemDetails()
        val principalPersonSummaryState by viewModel.rememberPersonSummaryState()
        val range by viewModel.rememberRange()
        val personFilterValue by viewModel.rememberPersonFilterValue()

        var navPosition: NavPosition by rememberSaveable {
            mutableStateOf(NavPosition.TRANSACCIONES)
        }
        val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val snackbarHostState = SnackbarHostState()

        val dataLoaded = filteredTransactionListItemDetails.isNotEmpty()

        LaunchedEffect(key1 = dataLoaded) {
            if (dataLoaded) {
                onDataLoaded()
            }
        }

        BoxWithConstraints {
            val showVertical = maxWidth <= 700.dp
            MainFragment(
                allPerson = allPerson,
                accountList = accountAndOwnerWithTransactions,
                filteredTransactionList = filteredTransactionListItemDetails,
                navPosition = navPosition,
                range = range,
                personFilterValue = personFilterValue,
                sheetState = sheetState,
                snackbarHostState = snackbarHostState,
                delPerson = viewModel::deletePerson,
                delAccount = viewModel::deleteAccount,
                delTransaction = viewModel::deleteTransaction,
                onAddPersonRequested = onNavigateToAddPerson,
                onEditPersonRequested = { onNavigateToEditPerson(it.id) },
                onPersonDetailRequested = { onNavigateToPersonDetail(it.id) },
                onAddAccountRequested = onNavigateToAddAccount,
                onEditAccountRequested = { onNavigateToEditAccount(it.id) },
                onAccountDetailRequested = { onNavigateToAccountDetail(it.id) },
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
                    onNavigateToAddTransaction(date, it)
                },
                onEditTransactionRequested = { onNavigateToEditTransaction(it.id) },
                onNavStatusChanged = { navPosition = it },
                onRangeChanged = { startDate, endDate ->
                    viewModel.updateRange(
                        startDate,
                        endDate
                    )
                },
                onSettingsClicked = onNavigateToSettings,
                onSaldoActualClick = onNavigateToSaldoActualSettings,
                onPersonFilterValueChanged = viewModel::updatePersonFilterValue,
                showVertical = showVertical,
                principalPersonSummaryState = principalPersonSummaryState
            )
        }
    }
}

fun NavGraphBuilder.screenAddAccount(
    viewModel: MainViewModel,
    onNavigateToAddPerson: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable("addAccount") {
        val allPerson by viewModel.rememberAllPerson()
        val allAccount by viewModel.rememberAllAccount()
        val incomeAccount by viewModel.rememberIncomeAccount()
        val outcomeAccount by viewModel.rememberOutcomeAccount()
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()

        val coroutineScope = rememberCoroutineScope()
        AccountFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            personList = allPerson,
            onPersonAddRequested = onNavigateToAddPerson,
            onAccountAndOwnerAdd = { account, newBalance, snackBarHostState, incomeAccountId, outcomeAccountId ->
                val accountOwnerIdList = allAccount.map {
                    Pair(it.name, it.ownerId)
                }
                val accountOwnerId = Pair(account.name, account.ownerId)
                val sePuedeAgregar = accountOwnerId !in accountOwnerIdList
                if (sePuedeAgregar) {
                    viewModel.insertAccount(
                        account,
                        onErrorAction = {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Error añadiento cuenta $it")
                            }
                        },
                        onCompleitionAction = { addedId ->
                            if (incomeAccountId != null && outcomeAccountId != null) {
                                val valorAjuste = newBalance
                                viewModel.realizarAjuste(
                                    accountId = addedId.toInt(),
                                    amount = valorAjuste,
                                    incomeAccountId = incomeAccountId,
                                    outcomeAccountId = outcomeAccountId
                                )
                            }
                        }).invokeOnCompletion {
                        if (it == null) {
                            onNavigateUp()
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
            onSetIncomeOutcomeAccount = onNavigateToSettings,
            accountAndOwnerList = accountAndOwnerWithTransactions.map {
                AccountAndOwner(it.account, it.owner)
            }
        )
    }
}

fun NavController.navigateToAddAccount() {
    navigate("addAccount")
}

fun NavGraphBuilder.screenEditAccount(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable(
        "editAccount/{accountId}",
        arguments = listOf(navArgument("accountId") { type = NavType.IntType })
    ) { navStack ->
        val accountAndOwnerWithTransactionsAndPockets by viewModel.rememberAccountAndOwnerWithTransactionsAndPockets()
        val allPerson by viewModel.rememberAllPerson()
        val allAccount by viewModel.rememberAllAccount()
        val incomeAccount by viewModel.rememberIncomeAccount()
        val outcomeAccount by viewModel.rememberOutcomeAccount()
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()

        val coroutineScope = rememberCoroutineScope()

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
            personList = allPerson,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(8.dp),
            onPersonAddRequested = onNavigateToAddPerson,
            onAccountAndOwnerAdd = { account, newBalance, snackBarHostState, incomeAccountId, outcomeAccountId ->
                val accountOwnerIdList = allAccount
                    .filter { it.id != account.id }
                    .map { Pair(it.name, it.ownerId) }
                val accountOwnerId = Pair(account.name, account.ownerId)
                val sePuedeAgregar = accountOwnerId !in accountOwnerIdList
                if (sePuedeAgregar) {
                    viewModel.updateAccount(
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
                                viewModel.realizarAjuste(
                                    accountId = addedId.toInt(),
                                    amount = valorAjuste,
                                    incomeAccountId = incomeAccountId,
                                    outcomeAccountId = outcomeAccountId
                                )
                            }
                        }
                    ).invokeOnCompletion {
                        if (it == null) {
                            onNavigateUp()
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
            onSetIncomeOutcomeAccount = onNavigateToSettings,
            accountAndOwnerList = accountAndOwnerWithTransactions.map {
                AccountAndOwner(it.account, it.owner)
            }
        )
    }
}

fun NavController.navigateToEditAccount(accountId: Int?) {
    navigate("editAccount/$accountId")
}

fun NavGraphBuilder.screenEditarCategorias(
    viewModel: MainViewModel,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Int?) -> Unit,
    onNavigateToAddBudget: (Int?) -> Unit
) {
    composable("editCategories") {
        val categoriesWithSubCategories by viewModel.rememberCategoriesWithSubCategories()
        val categoriesWithCalculatedData by viewModel.rememberCategoriesWithCalculatedData()

        EditarCategorias(
            categoriesWithSubCategories,
            categoriesWithCalculatedData,
            onAddCategoryRequested = onNavigateToAddCategory,
            onEditCategoryRequested = { onNavigateToEditCategory(it.category.id) },
            onDeleteCategoryRequested = { viewModel.deleteCategory(it.category) },
            onSetBudgetRequested = { onNavigateToAddBudget(it.category.id) }
        )
    }
}

fun NavController.navigateToEditarCategorias() {
    navigate("editCategories")
}

fun NavGraphBuilder.screenAddPerson(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable("addPerson") {
        val allPerson by viewModel.rememberAllPerson()

        val coroutineScope = rememberCoroutineScope()
        PersonFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            onPersonAddRequested = { person, snackBarHostSate ->
                val namesList =
                    allPerson.map { persona -> persona.name }
                val sePuedeAgregar = person.name !in namesList
                if (sePuedeAgregar) {
                    viewModel.insertPerson(person, onErrorAction = {
                        coroutineScope.launch {
                            snackBarHostSate.showSnackbar("Error agregando a la persona: $it")
                        }
                    }).invokeOnCompletion {
                        if (it == null) {
                            onNavigateUp()
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
}

fun NavController.navigateToAddPerson() {
    navigate("addPerson")
}

fun NavGraphBuilder.screenEditPerson(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable(
        "editPerson/{personId}",
        arguments = listOf(navArgument("personId") { type = NavType.IntType })
    ) { navBack ->
        val allPerson by viewModel.rememberAllPerson()

        val coroutineScope = rememberCoroutineScope()

        val personId = navBack.arguments?.getInt("personId")
        val selectedPerson = allPerson
            .firstOrNull { it.id == personId }
        PersonFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            onPersonAddRequested = { person, snackBarHostSate ->
                val namesList =
                    allPerson.map { persona -> persona.name }
                val sePuedeEditar = person.name !in namesList
                if (sePuedeEditar) {
                    viewModel.updatePerson(person, onErrorAction = {
                        coroutineScope.launch {
                            snackBarHostSate.showSnackbar("Error editando persona $it")
                        }
                    }).invokeOnCompletion {
                        if (it == null) {
                            onNavigateUp()
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
}

fun NavController.navigateToEditPerson(personId: Int?) {
    navigate("editPerson/$personId")
}

fun NavGraphBuilder.screenSaldoActualSettings(
    viewModel: MainViewModel
) {
    composable("saldoActualSettings") {
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()
        val principalPerson by viewModel.rememberPrincipalPerson()
        val incluirPresupuestoEnSaldoActual by viewModel.rememberSettingsIncluirPresupuestoEnSaldoActualFlow()
        val incluirDeudasEnSaldoActual by viewModel.rememberSettingsIncluirDeudasEnSaldoActualFlow()
        val coroutineScope = rememberCoroutineScope()

        var saving: Int by remember {
            mutableStateOf(0)
        }
        SaldoActualSettings(
            accountAndOwnerWithTransactions.filter { it.owner.id == principalPerson?.id },
            saving = saving,
            incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual,
            incluirDeudasEnSaldoActual = incluirDeudasEnSaldoActual,
            onIncluirPresupuestoEnSaldoActualChanged = viewModel::settingsIncluirPresupuestoEnSaldoActualFlow,
            onIncluirDeudasEnSaldoActualChanged = viewModel::settingsIncluirDeudasEnSaldoActualFlow
        ) { account, nuevoEstado ->
            saving += 1
            coroutineScope.launch {
                viewModel.updateAccount(
                    account = account.copy(includedInTotal = nuevoEstado),
                    onErrorAction = {},
                    onCompleitionAction = {}).join()
            }.invokeOnCompletion {
                saving -= 1
            }
        }
    }
}

fun NavController.navigateToSaldoActualSettings() {
    navigate("saldoActualSettings")
}

fun NavGraphBuilder.screenSettings(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToEditCategories: () -> Unit,
    onNavigateToEditBudget: () -> Unit
) {
    composable("settings") {
        val allPerson by viewModel.rememberAllPerson()
        val principalPerson by viewModel.rememberPrincipalPerson()
        val accountAndOwner by viewModel.rememberAccountAndOwner()
        val incomeAccount by viewModel.rememberIncomeAccount()
        val outcomeAccount by viewModel.rememberOutcomeAccount()

        SettingsFragment(
            personList = allPerson,
            principalPerson = principalPerson,
            onPrincipalPersonChanged = {
                val notNullPrincipalPerson = principalPerson
                if (notNullPrincipalPerson != null) {
                    viewModel.updatePerson(
                        notNullPrincipalPerson.copy(importance = null)
                    ) {}
                }
                viewModel.updatePerson(it.copy(importance = 1)) {}
            },
            onNavigateUpRequested = onNavigateUp,
            onAddPersonRequested = onNavigateToAddPerson,
            accountList = accountAndOwner,
            incomeAccount = incomeAccount,
            outcomeAccount = outcomeAccount,
            onAddAccountRequested = onNavigateToAddAccount,
            onIncomeOutcomeAccountChanged = { newIncome, newOutcome ->
                val castedIncomeAccount = incomeAccount
                val castedOutcomeAccount = outcomeAccount
                if (castedIncomeAccount != null) {
                    viewModel.updateAccount(
                        castedIncomeAccount.copy(
                            isIncome = false
                        ), onCompleitionAction = {}, onErrorAction = {})
                }
                if (castedOutcomeAccount != null) {
                    viewModel.updateAccount(
                        castedOutcomeAccount.copy(
                            isOutcome = false
                        ), onCompleitionAction = {}, onErrorAction = {})
                }
                if (newIncome != null) {
                    viewModel.updateAccount(
                        newIncome.copy(
                            isIncome = true,
                            isOutcome = false
                        ), onErrorAction = {}, onCompleitionAction = {})
                }
                if (newOutcome != null) {
                    viewModel.updateAccount(
                        newOutcome.copy(
                            isIncome = false,
                            isOutcome = true
                        ), onErrorAction = {}, onCompleitionAction = {})
                }
            },
            onEditCategoriesRequested = onNavigateToEditCategories,
            onEditBudgetsRequested = onNavigateToEditBudget
        )
    }
}

fun NavController.navigateToSettings() {
    navigate("settings")
}

fun NavGraphBuilder.screenAddTransaction(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToAddAccount: () -> Unit
) {
    composable(
        "addTransaction?yearmonthday={yearmonthday}?transactionaction={transactionaction}?requestingAccountId={requestingAccountId}",
        deepLinks = listOf(navDeepLink {
            uriPattern = "$URI?transactionaction={transactionaction}"
            action = Intent.ACTION_VIEW
        }),
        arguments = listOf(
            navArgument("yearmonthday") {
                type = NavType.IntType
                defaultValue = LocalDate.now().toInt()
            },
            navArgument("transactionaction") {
                type = NavType.StringType
            },
            navArgument("requestingAccountId") {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    ) { navBackStackEntry ->
        val incomeAccount by viewModel.rememberIncomeAccount()
        val outcomeAccount by viewModel.rememberOutcomeAccount()
        val allPerson by viewModel.rememberAllPerson()
        val allAccount by viewModel.rememberAllAccount()
        val categories by viewModel.rememberCategories()

        val yearMonthDay = navBackStackEntry.arguments?.getInt("yearmonthday")
            ?: LocalDate.now().let {
                it.year * 100 + it.monthValue
            }
        val transactionActionName =
            navBackStackEntry.arguments?.getString("transactionaction")
        val transactionAction =
            transactionActionName?.let { AddTransactionAction.valueOf(it) }
                ?: AddTransactionAction.ADD_TRANSFER
        val requestingAccountId = navBackStackEntry.arguments?.getInt("requestingAccountId")
        val requestingAccount: Account? = allAccount.firstOrNull { it.id == requestingAccountId }
            ?.takeIf { acc -> acc.id != null && acc.id >= 0 }
        val initialSourceDestinationAccount: Pair<Account?, Account?> = when (transactionAction) {
            AddTransactionAction.ADD_EXPENSE -> Pair(requestingAccount, outcomeAccount)
            AddTransactionAction.ADD_INCOME -> Pair(incomeAccount, requestingAccount)
            AddTransactionAction.ADD_TRANSFER -> Pair(null, null)
        }
        val initialSourceAccount: Account? = initialSourceDestinationAccount.first
        val initialDestinationAccount: Account? = initialSourceDestinationAccount.second
        val orderedAccounts =
            if (transactionAction == AddTransactionAction.ADD_TRANSFER) {
                viewModel.rememberAccountAndOwner().value
            } else {
                viewModel.rememberAccountAndOwnerUserFirst().value
            }
        TransactionFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            accountList = orderedAccounts,
            personList = allPerson,
            categoryList = categories,
            defaultDate = LocalDate.of(
                yearMonthDay.div(10000),
                yearMonthDay.mod(10000).div(100),
                yearMonthDay.mod(100)
            ),
            fixedSourceAccount = initialSourceAccount,
            fixedDestinationAccount = initialDestinationAccount,
            onAccountAddRequested = onNavigateToAddAccount
        ) {
            viewModel.insertTransaction(it)
            onNavigateUp()
        }
    }
}

fun NavController.navigateToAddTransaction(
    date: LocalDate,
    transactionAction: AddTransactionAction
) {
    val yearmonthday = date.toInt()
    val transactionaction = transactionAction.name
    navigate("addTransaction?yearmonthday=$yearmonthday?transactionaction=$transactionaction?requestingAccountId=${-1}")
}

fun NavController.navigateToAddTransaction(
    date: LocalDate,
    transactionAction: AddTransactionAction,
    requestingAccount: Account
) {
    val yearmonthday = date.toInt()
    val transactionaction = transactionAction.name
    val requestingAccountId = requestingAccount.id ?: -1
    navigate("addTransaction?yearmonthday=$yearmonthday?transactionaction=$transactionaction?requestingAccountId=$requestingAccountId")
}

fun LocalDate.toInt() = let { it.year * 10000 + it.monthValue * 100 + it.dayOfMonth }

fun NavGraphBuilder.screenEditTransaction(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToAddAccount: () -> Unit
) {
    composable(
        "editTransaction/{transactionId}",
        arguments = listOf(navArgument("transactionId") {
            type = NavType.IntType
        })
    ) { navBackStackEntry ->
        val filteredTransactionListItemDetails by viewModel.rememberFilteredTransactionListItemDetails()
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()
        val allPerson by viewModel.rememberAllPerson()
        val categories by viewModel.rememberCategories()

        val transactionId = navBackStackEntry.arguments?.getInt("transactionId")
        val selectedTransactionListItemDetails =
            filteredTransactionListItemDetails
                .firstOrNull { it.transaction.id == transactionId }
        TransactionFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            transactionAndAccounts = selectedTransactionListItemDetails?.toTransactionAndAccounts(),
            accountList = accountAndOwnerWithTransactions.map {
                AccountAndOwner(
                    it.account,
                    it.owner
                )
            },
            personList = allPerson,
            categoryList = categories,
            onAccountAddRequested = onNavigateToAddAccount
        ) {
            viewModel.updateTransaction(it)
            onNavigateUp()
        }
    }
}

fun NavController.navigateToEditTransaction(transactionId: Int?) {
    navigate("editTransaction/$transactionId")
}

fun NavGraphBuilder.screenAddCategory(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable("addCategory") {
        val categories by viewModel.rememberCategories()

        val coroutineScope = rememberCoroutineScope()

        CategoryForm(
            null,
            categories,
            onCategorySave = { category, snackbar ->
                viewModel.insertCategory(
                    category,
                    onCompleitionAction = { onNavigateUp() }
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
}

fun NavController.navigateToAddCategory() {
    navigate("addCategory")
}

fun NavGraphBuilder.screenEditCategory(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable(
        "editCategory/{categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val categories by viewModel.rememberCategories()

        val coroutineScope = rememberCoroutineScope()

        val categoryId = navStack.arguments?.getInt("categoryId")
        val category = categories.firstOrNull { it.id == categoryId }
        CategoryForm(
            category,
            categories,
            onCategorySave = { newCategory, state ->
                viewModel.updateCategory(
                    newCategory,
                    onCompleitionAction = onNavigateUp
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
}

fun NavController.navigateToEditCategory(categoryId: Int?) {
    navigate("editCategory/$categoryId")
}

fun NavGraphBuilder.screenAccountDetail(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToEditAccount: (Int?) -> Unit,
    onNavigateToAddTransaction: (LocalDate, AddTransactionAction, Account) -> Unit,
    onNavigateToEditTransaction: (Int?) -> Unit
) {
    composable(
        "accountDetail/{accountId}",
        arguments = listOf(
            navArgument("accountId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val accountId = navStack.arguments?.getInt("accountId")
        viewModel.updateAccountDetailIdIfDifferent(accountId)
        val data by viewModel.rememberAccountDetailData()
        var fabExpanded by remember { mutableStateOf(false) }

        val accountAndOwner by viewModel.rememberAccountAndOwner()

        val coroutineScope = rememberCoroutineScope()

        val account = accountAndOwner
            .firstOrNull { it.account.id == accountId }
        if (account != null) {
            var showGraphs by remember {
                mutableStateOf(false)
            }
            AccountDetail(
                accountAndOwner = account,
                data = data,
                onAction = { actionAccount, action ->
                    when (action) {
                        AccountAction.EDIT -> onNavigateToEditAccount(accountId)
                        AccountAction.DELETE -> {
                            onNavigateUp()
                            viewModel.deleteAccount(actionAccount)
                        }
                    }
                },
                onTransactionAction = { transaction, action ->
                    val transactionId = transaction.id
                    when (action) {
                        TransactionAction.EDIT -> onNavigateToEditTransaction(transactionId)
                        TransactionAction.DELETE -> viewModel.deleteTransaction(
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
                },
                fabExpanded = fabExpanded,
                onFabExpandedChanged = { fabExpanded = it },
                onAddTransactionRequested = {
                    onNavigateToAddTransaction(
                        LocalDate.now(),
                        it,
                        account.account
                    )
                }
            )
        } else {
            Text("Cuenta vacía")
        }
    }
}

fun NavController.navigateToAccountDetail(accountId: Int?) {
    navigate("accountDetail/$accountId")
}

fun NavGraphBuilder.screenPersonDetail(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToEditPerson: (Int?) -> Unit,
    onNavigateToEditTransaction: (Int?) -> Unit
) {
    composable(
        "personDetail/{personId}",
        arguments = listOf(
            navArgument("personId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val allPerson by viewModel.rememberAllPerson()
        val allTransactions by viewModel.rememberAllTransactions()
        val allAccount by viewModel.rememberAllAccount()
        val categories by viewModel.rememberCategories()
        val personSummaryState by viewModel.rememberPersonSummaryState()

        val personId = navStack.arguments?.getInt("personId")
        val person = allPerson.firstOrNull { it.id == personId }
        val deuda = personSummaryState?.deudasFlujo?.get(person) ?: 0.0
        if (person != null) {
            PersonDetail(
                person = person,
                onPersonAction = { _, action ->
                    when (action) {
                        PersonAction.EDIT -> onNavigateToEditPerson(personId)
                        PersonAction.DELETE -> {
                            onNavigateUp()
                            viewModel.deletePerson(person)
                        }
                    }
                },
                allTransactions = allTransactions,
                allAccounts = allAccount,
                allCategories = categories,
                onTransactionAction = { transaction, action ->
                    val transactionId = transaction.id
                    when (action) {
                        TransactionAction.EDIT -> onNavigateToEditTransaction(transactionId)
                        TransactionAction.DELETE -> viewModel.deleteTransaction(transaction)
                    }
                },
                deuda = deuda
            )
        } else {
            Text(text = "Empty person")
        }
    }
}

fun NavController.navigateToPersonDetail(personId: Int?) {
    navigate("personDetail/$personId")
}

fun NavGraphBuilder.screenEditBudget(
    viewModel: MainViewModel,
    onNavigateToAddOneBudget: () -> Unit,
    onNavigateToOneBudgetDetail: (budgetId: Int) -> Unit,
    onNavigateToOneBudgetEdit: (budgetId: Int) -> Unit
) {
    composable("editarBudget") {
        val budget by viewModel.rememberBudgetAndCategoryWithCalculatedData()
        EditBudgetFragment(
            budget = budget,
            onAddOneBudgetRequested = onNavigateToAddOneBudget,
            onGetBudgetDetailRequested = onNavigateToOneBudgetDetail,
            onDeleteBudgetRequested = { id ->
                budget
                    .firstOrNull { it.budgetId == id }
                    ?.let { viewModel.deleteBudget(it.budget) }
            },
            onEditBudgetRequested = onNavigateToOneBudgetEdit
        )
    }
}

fun NavController.navigateToEditBudget() {
    navigate("editarBudget")
}

fun NavGraphBuilder.screenOneBudgetDetail(
    viewModel: MainViewModel
) {
    composable(
        "oneBudgetDetail/{budgetId}",
        arguments = listOf(
            navArgument("budgetId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val budgetId = navStack.arguments?.getInt("budgetId")
        val budgetAndCategoryWithTransactions by viewModel.rememberBudgetAndCategoryWithTransactions()
        val selectedBudget = budgetAndCategoryWithTransactions
            .firstOrNull { it.budgetId == budgetId }
        if (selectedBudget != null) {
            BudgetDetailView(selectedBudget)
        } else {
            EmptyBudgetDetailView()
        }
    }
}

fun NavController.navigateToOneBudgetDetail(budgetId: Int) {
    //TODO Aún no se tiene lista la página de detalles para los presupupestos.
    //navigate("oneBudgetDetail/$budgetId")
}

fun NavGraphBuilder.screenAddOneBudget(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable("addOneBudget?categoryId={categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    ) { navStack ->
        val categoryId = navStack.arguments?.getInt("categoryId")
        val categories by viewModel.rememberCategories()
        val fixedCategory = categories.firstOrNull { it.id == categoryId }
        if (fixedCategory == null) {
            BudgetFormView(
                budget = null,
                categories = categories,
                onSaveBudget = {
                    viewModel.insertBudget(
                        it,
                        onCompleitionAction = { onNavigateUp() },
                        onErrorAction = {})
                }
            )
        } else {
            BudgetFormView(
                budget = Budget.fromMonthly(
                    categoryId = fixedCategory.id!!,
                    value = 0.0,
                    budgetType = BudgetType.VARIABLE
                ),
                categories = categories,
                onSaveBudget = {
                    viewModel.insertBudget(
                        it,
                        onCompleitionAction = { onNavigateUp() },
                        onErrorAction = {})
                }
            )
        }
    }
}

fun NavController.navigateToAddOneBudget() {
    navigate("addOneBudget")
}

fun NavController.navigateToAddOneBudget(categoryId: Int) {
    navigate("addOneBudget?categoryId=$categoryId")
}

fun NavGraphBuilder.screenEditOneBudget(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable(
        "editOneBudget/{budgetId}",
        arguments = listOf(
            navArgument("budgetId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val budgetId = navStack.arguments?.getInt("budgetId")
        val budgets by viewModel.rememberBudget()
        val categories by viewModel.rememberCategories()
        BudgetFormView(
            budget = budgets.firstOrNull { it.id == budgetId },
            categories = categories,
            onSaveBudget = {
                viewModel.updateBudget(
                    it,
                    onCompleitionAction = { onNavigateUp() },
                    onErrorAction = {})
            }
        )
    }
}

fun NavController.navigateToEditOneBudget(budgetId: Int) {
    navigate("editOneBudget/$budgetId")
}