package com.example.gazege.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.*
import com.example.gazege.ui.savers.PartialTransaction
import com.example.gazege.ui.savers.PartialTransactionAndAccounts
import com.example.gazege.ui.savers.transactionSaver
import com.example.gazege.ui.views.AddTransactionAction
import com.example.gazege.ui.views.transaction.TransactionAndAccountsForm
import com.example.gazege.ui.widgets.Form
import com.example.gazege.ui.widgets.toSignedBigDecimal
import java.time.LocalDate


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

@Composable
fun TransactionFormScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    transactionAndAccounts: TransactionAndAccounts? = null,
    accountList: List<AccountAndOwner>,
    personList: List<Person>,
    categoryList: List<Category>,
    defaultDate: LocalDate = LocalDate.now(),
    fixedSourceAccount: Account? = null,
    fixedDestinationAccount: Account? = null,
    onAccountAddRequested: () -> Unit,
    onTransactionAndAccountsAdd: (Transaction) -> Unit
) {
    var transactionAndAccountsState by rememberSaveable(
        stateSaver = transactionSaver
    ) {
        mutableStateOf(
            if (transactionAndAccounts != null) {
                PartialTransactionAndAccounts(
                    transaction = transactionAndAccounts.transaction.let {
                        PartialTransaction(
                            id = it.id,
                            amount = it.amount.toSignedBigDecimal(),
                            description = it.description,
                            sourceId = it.sourceId,
                            destinationId = it.destinationId,
                            date = it.date,
                            aNombreDe = it.aNombreDe,
                            categoryId = it.categoryId
                        )
                    },
                    sourceAccount = transactionAndAccounts.sourceAccount,
                    destinationAccount = transactionAndAccounts.destinationAccount
                )
            } else {
                PartialTransactionAndAccounts
                    .blankEntity()
                    .apply {
                        sourceAccount = fixedSourceAccount
                        destinationAccount = fixedDestinationAccount
                        transaction.apply {
                            sourceId = fixedSourceAccount?.id
                            destinationId = fixedDestinationAccount?.id
                        }
                    }
            }
        )
    }
    var realizarANombreDe by rememberSaveable {
        mutableStateOf(transactionAndAccountsState.transaction.aNombreDe != null)
    }
    val completeState = transactionAndAccountsState.isComplete()
    val saveTransaction = {
        val fullTransactionAndAccounts = transactionAndAccountsState.toFull()
        onTransactionAndAccountsAdd(fullTransactionAndAccounts.transaction)
    }
    Form(
        modifier = modifier,
        isSavedButtonEnabled = completeState,
        title = "Transaction",
        onSaveClicked = saveTransaction
    ) {
        TransactionAndAccountsForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            transactionAndAccounts = transactionAndAccountsState,
            accountList = accountList,
            onAccountAddRequested = onAccountAddRequested,
            onTransactionAndAccountsChanged = {
                transactionAndAccountsState = it
            },
            defaultDate = defaultDate,
            onDateChanged = {
                transactionAndAccountsState = transactionAndAccountsState.copy().apply {
                    transaction = this.transaction.copy(date = it)
                }
            },
            realizarANombreDe = realizarANombreDe,
            onRealizarANombreDeChanged = { realizarANombreDe = it },
            personList = personList,
            onRealizarAnombreDeIdChanged = {
                transactionAndAccountsState = transactionAndAccountsState.copy().apply {
                    transaction = this.transaction.copy(aNombreDe = it)
                }
            },
            categoryList = categoryList,
            onDoneAction = saveTransaction,
            isComplete = completeState,
            showSourceAccountField = fixedSourceAccount == null,
            showDestinationAccountField = fixedDestinationAccount == null
        )
    }
}
