package com.jmml.gazege.ui.fragments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionWithDetails
import com.jmml.gazege.core.entities.TransactionWithDetailsAndAccounts
import com.jmml.gazege.ui.savers.PartialTransactionDetails
import com.jmml.gazege.ui.savers.PartialTransactionWithDetails
import com.jmml.gazege.ui.savers.PartialTransactionWithDetailsAndAccounts
import com.jmml.gazege.ui.savers.transactionDetailsListSaver
import com.jmml.gazege.ui.views.AddTransactionAction
import com.jmml.gazege.ui.views.transaction.TransactionAndAccountsForm
import com.jmml.gazege.ui.widgets.Form
import com.jmml.zoo.ui.state.ZIndefiniteCircularProgressIndicator
import java.time.LocalDate


@Composable
fun TransactionFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    transactionWithDetailsAndAccounts: TransactionWithDetailsAndAccounts? = null,
    accountList: List<AccountAndOwner>,
    personList: List<Person>,
    categoryList: List<Category>,
    budgetWithCalculatedDataAndCategory: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    defaultDate: LocalDate? = null,
    fixedSourceAccount: Account? = null,
    fixedDestinationAccount: Account? = null,
    onAccountAddRequested: () -> Unit,
    onTransactionAndAccountsAdd: (TransactionWithDetails, Boolean) -> Unit
) {
    val id by rememberSaveable(transactionWithDetailsAndAccounts) {
        mutableStateOf(
            transactionWithDetailsAndAccounts?.transaction?.transaction?.id
        )
    }
    val transactionDetails =
        rememberSaveable(transactionWithDetailsAndAccounts, saver = transactionDetailsListSaver) {
            mutableStateListOf(
                *(transactionWithDetailsAndAccounts?.transaction?.transactionDetails?.map {
                    PartialTransactionDetails.from(
                        it
                    )
                } ?: emptyList()).toTypedArray()
            )
        }
    var sourceId by rememberSaveable(transactionWithDetailsAndAccounts, fixedSourceAccount) {
        mutableStateOf(
            transactionWithDetailsAndAccounts?.transaction?.transaction?.sourceId
                ?: fixedSourceAccount?.id
        )
    }
    var destinationId by rememberSaveable(
        transactionWithDetailsAndAccounts,
        fixedDestinationAccount
    ) {
        mutableStateOf(
            transactionWithDetailsAndAccounts?.transaction?.transaction?.destinationId
                ?: fixedDestinationAccount?.id
        )
    }
    var date by rememberSaveable(transactionWithDetailsAndAccounts, defaultDate) {
        mutableStateOf(
            transactionWithDetailsAndAccounts?.transaction?.transaction?.date ?: defaultDate
        )
    }

    val currentTransaction =
        sourceId?.let { sourceIdNotNull ->
            destinationId?.let { destinationId ->
                date?.let { date ->
                    transactionDetails.takeIf { list -> list.all { it.isComplete() } }
                        ?.let { transactionDetails ->
                            TransactionWithDetails(
                                transaction = Transaction(
                                    id = id,
                                    sourceId = sourceIdNotNull,
                                    destinationId = destinationId,
                                    date = date
                                ),
                                transactionDetails = transactionDetails.map { it.toFull() }
                            )
                        }
                }
            }
        }

    var addAnotherTransaction by rememberSaveable {
        mutableStateOf(false)
    }
    val completeState = currentTransaction != null
    val sourceAccount = accountList.firstOrNull { it.account.id == sourceId }
    val destinationAccount = accountList.firstOrNull { it.account.id == destinationId }
    val showAddAnotherTransactionButton = transactionWithDetailsAndAccounts == null
    val saveTransaction: () -> Unit = {
        currentTransaction?.let { fullTransaction ->
            onTransactionAndAccountsAdd(fullTransaction, addAnotherTransaction)
            if (showAddAnotherTransactionButton) {
                transactionDetails.clear()
            }
        }
        //focusRequester.requestFocus()
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
            transactionAndAccounts = PartialTransactionWithDetailsAndAccounts.from(
                currentTransaction?.let { PartialTransactionWithDetails.from(currentTransaction) }
                    ?: PartialTransactionWithDetails(
                        transactionId = id,
                        sourceId = sourceId,
                        destinationId = destinationId,
                        date = date,
                        transactionDetails = listOf(*transactionDetails.toTypedArray()),
                    ),
                sourceAccount = sourceAccount?.account,
                destinationAccount = destinationAccount?.account
            ),
            accountList = accountList,
            onAccountAddRequested = onAccountAddRequested,
            onDateChanged = { date = it },
            personList = personList,
            onRealizarAnombreDeIdChanged = { detailIndexId, personId ->
                transactionDetails[detailIndexId] =
                    transactionDetails[detailIndexId].copy(aNombreDe = personId)
            },
            categoryList = categoryList,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            onDoneAction = saveTransaction,
            isComplete = completeState,
            showSourceAccountField = fixedSourceAccount == null,
            showDestinationAccountField = fixedDestinationAccount == null,
            onAmountChanged = { detailIndexId, amount ->
                transactionDetails[detailIndexId] =
                    transactionDetails[detailIndexId].copy(amount = amount)
            },
            onCategoryIdChanged = { detailIndexId, categoryId ->
                transactionDetails[detailIndexId] =
                    transactionDetails[detailIndexId].copy(categoryId = categoryId)
            },
            onDescriptionChanged = { detailIndexId, description ->
                transactionDetails[detailIndexId] =
                    transactionDetails[detailIndexId].copy(description = description)
            },
            onDestinationAccountIdChanged = { destinationId = it },
            onSourceAccountIdChanged = { sourceId = it },
            showAddAnotherTransactionButton = showAddAnotherTransactionButton,
            addAnotherTransaction = addAnotherTransaction,
            onAddAnotherTransactionChanged = { addAnotherTransaction = it }
        )
    }
}

@Composable
fun LoadingTransactionFormFragment() {
    Box(Modifier.fillMaxSize()) { ZIndefiniteCircularProgressIndicator() }
}