package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.ui.savers.PartialTransaction
import com.example.gazege.ui.savers.PartialTransactionAndAccounts
import com.example.gazege.ui.savers.transactionSaver
import com.example.gazege.ui.views.transaction.TransactionAndAccountsForm
import com.example.gazege.ui.widgets.Form
import com.example.gazege.ui.widgets.toSignedBigDecimal
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
        title = stringResource(R.string.Transaccion),
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
