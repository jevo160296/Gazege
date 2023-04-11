package com.example.gazege.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.core.entities.*
import com.example.gazege.ui.savers.*
import com.example.gazege.ui.views.account.AccountAndOwnerForm
import com.example.gazege.ui.views.person.PersonForm
import com.example.gazege.ui.views.transaction.TransactionAndAccountsForm
import com.example.gazege.ui.widgets.Form
import com.example.gazege.ui.widgets.toSignedBigDecimal
import java.time.LocalDate

@Composable
fun PersonFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    person: Person? = null,
    onPersonAddRequested: (Person, SnackbarHostState) -> Unit
) {
    var personState by rememberSaveable(
        stateSaver = personSaver
    ) {
        mutableStateOf(
            if (person != null) {
                PartialPerson(
                    id = person.id,
                    name = person.name,
                    importance = person.importance
                )
            } else {
                PartialPerson.blankEntity()
            }
        )
    }
    val snackbarHostState = SnackbarHostState()
    val isComplete = personState.isComplete()
    val savePerson = {
        val fullPerson = personState.toFull()
        onPersonAddRequested(fullPerson, snackbarHostState)
    }
    Form(
        modifier = modifier,
        onSaveClicked = savePerson,
        isSavedButtonEnabled = isComplete,
        title = "Person",
        snackbarHostState = snackbarHostState
    ) {
        PersonForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            person = personState,
            onPersonChanged = {
                personState = it
            },

            onDoneAction = savePerson,
            imeAction = if (isComplete) {
                ImeAction.Done
            } else {
                ImeAction.None
            }
        )
    }
}

@Composable
fun AccountFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    accountAndOwner: AccountAndOwner? = null,
    accountAndOwnerList: List<AccountAndOwner>,
    personList: List<Person>,
    currentBalance: Double,
    incomeAccount: Account?,
    outcomeAccount: Account?,
    onSetIncomeOutcomeAccount: () -> Unit,
    onPersonAddRequested: () -> Unit,
    onAccountAndOwnerAdd: (Account, Double, SnackbarHostState, Int?, Int?) -> Unit
) {
    var currentBalanceState by rememberSaveable {
        mutableStateOf(currentBalance.toSignedBigDecimal())
    }
    var accountAndOwnerState by rememberSaveable(
        stateSaver = accountAndOwnerSaver
    ) {
        mutableStateOf(
            if (accountAndOwner != null) {
                PartialAccountAndOwner(
                    account = accountAndOwner.account.let {
                        PartialAccount(
                            id = it.id,
                            name = it.name,
                            ownerId = it.ownerId,
                            includedInTotal = it.includedInTotal,
                            isIncome = it.isIncome,
                            isOutcome = it.isOutcome,
                            parentId = it.parentId
                        )
                    },
                    owner = accountAndOwner.owner
                )
            } else {
                PartialAccountAndOwner.blankEntity()
            }
        )
    }
    val completeState = accountAndOwnerState.isComplete()
    val snackbarHostState = SnackbarHostState()
    val saveAccount = {
        val fullAccountAndOwner = accountAndOwnerState.toFull()
        onAccountAndOwnerAdd(
            fullAccountAndOwner.account,
            currentBalanceState.toDouble(),
            snackbarHostState,
            incomeAccount?.id,
            outcomeAccount?.id
        )
    }
    Form(
        modifier = modifier,
        onSaveClicked = saveAccount,
        isSavedButtonEnabled = completeState,
        snackbarHostState = snackbarHostState,
        title = "Account"
    ) {
        AccountAndOwnerForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            accountAndOwner = accountAndOwnerState,
            accountAndOwnerList = accountAndOwnerList,
            personList = personList,
            currentBalance = currentBalanceState,
            onCurrentBalanceChanged = { currentBalanceState = it },
            onPersonAddRequested = onPersonAddRequested,
            onAccountAndOwnerChanged = {
                accountAndOwnerState = it
            },
            incomeAccount = incomeAccount,
            outcomeAccount = outcomeAccount,
            onSetIncomeOutcomeAccount = onSetIncomeOutcomeAccount,
            onDoneAction = saveAccount,
            isComplete = completeState
        )
    }
}


@Composable
fun TransactionFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    transactionAndAccounts: TransactionAndAccounts? = null,
    accountList: List<AccountAndOwner>,
    onAccountAddRequested: () -> Unit,
    onTransactionAndAccountsAdd: (Transaction) -> Unit,
    personList: List<Person>,
    categoryList: List<Category>,
    defaultDate: LocalDate = LocalDate.now(),
    fixedSourceAccount: Account? = null,
    fixedDestinationAccount: Account? = null
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
