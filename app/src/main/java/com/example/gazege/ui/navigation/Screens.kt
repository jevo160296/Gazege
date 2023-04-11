package com.example.gazege.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.ui.fragments.*
import com.example.gazege.ui.views.AddTransactionAction
import kotlinx.coroutines.launch
import java.time.LocalDate

fun NavGraphBuilder.screenAddAccount(
    viewModel: MainViewModel,
    onNavigateToAddPerson: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable("addAccount") {
        val allPerson by viewModel.allPerson.observeAsState(emptyList())
        val allAccount by viewModel.allAccount.observeAsState(emptyList())
        val incomeAccount by viewModel.incomeAccount.observeAsState()
        val outcomeAccount by viewModel.outcomeAccount.observeAsState()
        val accountAndOwnerWithTransactions by viewModel.accountAndOwnerWithTransactions.observeAsState(
            emptyList()
        )

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
        val accountAndOwnerWithTransactionsAndPockets by viewModel.accountAndOwnerWithTransactionsAndPockets.observeAsState(
            emptyList()
        )
        val allPerson by viewModel.allPerson.observeAsState(emptyList())
        val allAccount by viewModel.allAccount.observeAsState(emptyList())
        val incomeAccount by viewModel.incomeAccount.observeAsState()
        val outcomeAccount by viewModel.outcomeAccount.observeAsState()
        val accountAndOwnerWithTransactions by viewModel.accountAndOwnerWithTransactions.observeAsState(
            emptyList()
        )

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
    onNavigateToEditCategory: (Int?) -> Unit
) {
    composable("editCategories") {
        val categoriesWithSubCategories by viewModel.categoriesWithSubCategories.observeAsState(
            emptyList()
        )

        EditarCategorias(
            categoriesWithSubCategories,
            onAddCategoryRequested = onNavigateToAddCategory,
            onEditCategoryRequested = { onNavigateToEditCategory(it.category.id) },
            onDeleteCategoryRequested = { viewModel.deleteCategory(it.category) }
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
        val allPerson by viewModel.allPerson.observeAsState(emptyList())

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
        val allPerson by viewModel.allPerson.observeAsState(emptyList())

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
        val accountAndOwnerWithTransactions by viewModel.accountAndOwnerWithTransactions.observeAsState(
            emptyList()
        )
        val principalPerson by viewModel.principalPerson.observeAsState()
        val coroutineScope = rememberCoroutineScope()

        var saving: Int by remember {
            mutableStateOf(0)
        }
        SaldoActualSettings(
            accountAndOwnerWithTransactions.filter { it.owner.id == principalPerson?.id },
            saving = saving
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
    onNavigateToEditCategories: () -> Unit
) {
    composable("settings") {
        val allPerson by viewModel.allPerson.observeAsState(emptyList())
        val principalPerson by viewModel.principalPerson.observeAsState()
        val accountAndOwnerWithTransactions by viewModel.accountAndOwnerWithTransactions.observeAsState(
            emptyList()
        )
        val incomeAccount by viewModel.incomeAccount.observeAsState()
        val outcomeAccount by viewModel.outcomeAccount.observeAsState()

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
            accountList = accountAndOwnerWithTransactions.map {
                AccountAndOwner(
                    it.account,
                    it.owner
                )
            },
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
            onEditCategoriesRequested = onNavigateToEditCategories
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
        val incomeAccount by viewModel.incomeAccount.observeAsState()
        val outcomeAccount by viewModel.outcomeAccount.observeAsState()
        val accountAndOwnerWithTransactions by viewModel.accountAndOwnerWithTransactions.observeAsState(
            emptyList()
        )
        val allPerson by viewModel.allPerson.observeAsState(emptyList())
        val categories by viewModel.categories.observeAsState(emptyList())

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
                viewModel.accountAndOwnerWithTransactionsUserFirst.observeAsState(
                    emptyList()
                ).value
            }
        TransactionFormFragment(
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
    val yearmonthday = date.let { it.year * 10000 + it.monthValue * 100 + it.dayOfMonth }
    val transactionaction = transactionAction.name
    navigate("addTransaction/$yearmonthday/$transactionaction")
}

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
        val filteredTransactionAndAccountsAndCategory by viewModel.filteredTransactionAndAccountsAndCategory.observeAsState(
            emptyList()
        )
        val accountAndOwnerWithTransactions by viewModel.accountAndOwnerWithTransactions.observeAsState(
            emptyList()
        )
        val allPerson by viewModel.allPerson.observeAsState(emptyList())
        val categories by viewModel.categories.observeAsState(emptyList())

        val transactionId = navBackStackEntry.arguments?.getInt("transactionId")
        val selectedTransactionAndAccounts =
            filteredTransactionAndAccountsAndCategory
                .firstOrNull { it.transaction.id == transactionId }
        TransactionFormFragment(
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