package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.BudgetWithCalculatedDataAndCategory
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import com.example.gazege.ui.savers.PartialTransaction
import com.example.gazege.ui.savers.PartialTransactionAndAccounts
import com.example.gazege.ui.views.AddTransactionAction
import com.example.gazege.ui.views.transaction.TransactionAndAccountsForm
import com.example.gazege.ui.widgets.Form
import java.time.LocalDate


@Composable
fun TransactionFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    transactionAndAccounts: TransactionAndAccounts? = null,
    accountList: List<AccountAndOwner>,
    personList: List<Person>,
    categoryList: List<Category>,
    budgetWithCalculatedDataAndCategory: Map<Category, BudgetWithCalculatedDataAndCategory>,
    defaultDate: LocalDate = LocalDate.now(),
    fixedSourceAccount: Account? = null,
    fixedDestinationAccount: Account? = null,
    onAccountAddRequested: () -> Unit,
    onTransactionAndAccountsAdd: (Transaction, Boolean) -> Unit
) {

    val id by rememberSaveable(transactionAndAccounts) { mutableStateOf(transactionAndAccounts?.transaction?.id) }
    var amount by rememberSaveable(transactionAndAccounts) { mutableStateOf(transactionAndAccounts?.transaction?.amount) }
    var description by rememberSaveable(transactionAndAccounts) {
        mutableStateOf(
            transactionAndAccounts?.transaction?.description ?: ""
        )
    }
    var sourceId by rememberSaveable(transactionAndAccounts, fixedSourceAccount) {
        mutableStateOf(
            transactionAndAccounts?.transaction?.sourceId ?: fixedSourceAccount?.id
        )
    }
    var destinationId by rememberSaveable(
        transactionAndAccounts,
        fixedDestinationAccount
    ) {
        mutableStateOf(
            transactionAndAccounts?.transaction?.destinationId ?: fixedDestinationAccount?.id
        )
    }
    var categoryId by rememberSaveable(transactionAndAccounts) {
        mutableStateOf(
            transactionAndAccounts?.transaction?.categoryId
        )
    }
    var date by rememberSaveable(transactionAndAccounts) {
        mutableStateOf(
            transactionAndAccounts?.transaction?.date ?: LocalDate.now()
        )
    }
    var aNombreDe by rememberSaveable(transactionAndAccounts) {
        mutableStateOf(
            transactionAndAccounts?.transaction?.aNombreDe
        )
    }

    val currentTransaction =
        amount?.let { _amount ->
            sourceId?.let { _sourceId ->
                destinationId?.let { _destinationId ->
                    date?.let { _date ->
                        Transaction(
                            id = id,
                            amount = _amount,
                            description = description,
                            sourceId = _sourceId,
                            destinationId = _destinationId,
                            categoryId = categoryId,
                            date = _date,
                            aNombreDe = aNombreDe
                        )
                    }
                }
            }
        }

    var realizarANombreDe by rememberSaveable(aNombreDe) {
        mutableStateOf(aNombreDe != null)
    }
    var addAnotherTransaction by rememberSaveable {
        mutableStateOf(false)
    }
    val completeState = currentTransaction != null
    val sourceAccount = accountList.firstOrNull { it.account.id == sourceId }
    val destinationAccount = accountList.firstOrNull { it.account.id == destinationId }
    val focusRequester = remember { FocusRequester() }
    val showAddAnotherTransactionButton = transactionAndAccounts == null
    val saveTransaction: () -> Unit = {
        currentTransaction?.let { fullTransaction ->
            onTransactionAndAccountsAdd(fullTransaction, addAnotherTransaction)
            if (showAddAnotherTransactionButton) {
                amount = null
            }
        }
        focusRequester.requestFocus()
    }
    val addTransactionAction: AddTransactionAction =
        if (sourceAccount?.account?.isIncome == true && destinationAccount?.account?.isOutcome != true) {
            AddTransactionAction.ADD_INCOME
        } else if (sourceAccount?.account?.isIncome != true && destinationAccount?.account?.isOutcome == true) {
            AddTransactionAction.ADD_EXPENSE
        } else {
            AddTransactionAction.ADD_TRANSFER
        }
    Form(
        modifier = modifier,
        isSavedButtonEnabled = completeState,
        title = stringResource(
            when (addTransactionAction) {
                AddTransactionAction.ADD_EXPENSE -> R.string.Gasto
                AddTransactionAction.ADD_INCOME -> R.string.Ingreso
                AddTransactionAction.ADD_TRANSFER -> R.string.Transaccion
            }
        ),
        onSaveClicked = saveTransaction
    ) {
        TransactionAndAccountsForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            transactionAndAccounts = PartialTransactionAndAccounts.from(
                currentTransaction?.let { PartialTransaction.from(currentTransaction) }
                    ?: PartialTransaction(
                        id = id,
                        amount = amount,
                        description = description,
                        sourceId = sourceId,
                        destinationId = destinationId,
                        date = date,
                        aNombreDe = aNombreDe,
                        categoryId = categoryId
                    ),
                sourceAccount = sourceAccount?.account,
                destinationAccount = destinationAccount?.account
            ),
            accountList = accountList,
            onAccountAddRequested = onAccountAddRequested,
            defaultDate = defaultDate,
            onDateChanged = { date = it },
            realizarANombreDe = realizarANombreDe,
            onRealizarANombreDeChanged = { realizarANombreDe = it },
            personList = personList,
            onRealizarAnombreDeIdChanged = { aNombreDe = it },
            categoryList = categoryList,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            onDoneAction = saveTransaction,
            isComplete = completeState,
            showSourceAccountField = fixedSourceAccount == null,
            showDestinationAccountField = fixedDestinationAccount == null,
            onAmountChanged = { amount = it },
            onCategoryIdChanged = { categoryId = it },
            onDescriptionChanged = { description = it },
            onDestinationAccountIdChanged = { destinationId = it },
            onSourceAccountIdChanged = { sourceId = it },
            showAddAnotherTransactionButton = showAddAnotherTransactionButton,
            addAnotherTransaction = addAnotherTransaction,
            onAddAnotherTransactionChanged = { addAnotherTransaction = it },
            focusRequester = focusRequester
        )
    }
}
