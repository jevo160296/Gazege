package com.example.gazege.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import com.example.gazege.ui.DateFormat
import com.example.gazege.ui.doubleToString
import com.example.gazege.ui.localDateToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.RecyclerView
import com.example.gazege.ui.widgets.SmallBody
import com.example.gazege.ui.widgets.SmallEmphasis
import java.time.LocalDate

@Composable
private fun TransactionViewHolder(
    transaction: TransactionAndAccounts
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Row(modifier = Modifier.weight(1f)) {
                SmallEmphasis(text = "${stringResource(id = R.string.cuentas)}: ")
                SmallBody(text = transaction.sourceAccount.name)
                SmallEmphasis(text = " --> ")
                SmallBody(text = transaction.destinationAccount.name)
            }
            Row(modifier = Modifier.weight(2f)) {
                SmallEmphasis(text = "${stringResource(id = R.string.descripcion)}: ")
                SmallBody(text = transaction.transaction.description)
            }
        }
        Column(
            modifier = Modifier.align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.End
        ) {
            SmallEmphasis(text = stringResource(id = R.string.Valor))
            LargeBody(text = doubleToString(transaction.transaction.amount))
        }
    }
}

@Composable
private fun TransactionRecyclerView(
    transactionList: List<TransactionAndAccounts>,
    editTransaction: (TransactionAndAccounts) -> Unit,
    delTransaction: (TransactionAndAccounts) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    state: LazyListState
) {
    RecyclerView(
        elements = transactionList,
        onItemTapped = editTransaction,
        onItemLongPressed = delTransaction,
        modifier = modifier,
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state,
        groupSelector = {
            localDateToString(it.transaction.date, DateFormat.DAYMONTHYEAR)
        }
    ) {
        TransactionViewHolder(transaction = it)
    }
}

@Composable
fun TransactionPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    transactionList: List<TransactionAndAccounts>,
    delTransaction: (Transaction) -> Unit,
    editTransaction: (Transaction) -> Unit,
    state: LazyListState,
    onTitleSetted: (String) -> Unit
) {
    onTitleSetted(stringResource(id = R.string.transacciones))
    Column(modifier = modifier) {
        TransactionRecyclerView(
            transactionList = transactionList,
            editTransaction = { transactionAndAccounts ->
                editTransaction(transactionAndAccounts.transaction)
            },
            delTransaction = { transactionAndAccounts ->
                delTransaction(transactionAndAccounts.transaction)
            },
            itemHolderPaddingValues = itemHolderPaddingValues,
            state = state
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTransactionItem() {
    val person = Person(name = "Person", id = 0)
    val sourceAccount = Account(
        name = "Account1", ownerId = person.id ?: -1
    )
    val destinationAccount = Account(
        name = "Account2", ownerId = person.id ?: -1
    )
    val transaction = Transaction(
        amount = 0.0, description = "Trans", date = LocalDate.now(),
        destinationId = destinationAccount.id ?: -1, sourceId = sourceAccount.id ?: -1,
        aNombreDe = null, categoryId = null
    )
    val transactionAndAccounts = TransactionAndAccounts(
        transaction = transaction, sourceAccount = sourceAccount,
        destinationAccount = destinationAccount
    )
    TransactionViewHolder(transaction = transactionAndAccounts)
}

fun getTransactionSample(): List<TransactionAndAccounts> {
    val accountList = getAccountSample()
    var i = 0
    val transList = accountList.map { source ->
        accountList.map { destination ->
            val transaction = Transaction(
                amount = i * 10.0,
                description = "Trans $i",
                sourceId = source.account.id ?: -1,
                destinationId = destination.account.id ?: -1,
                date = LocalDate.now(),
                aNombreDe = null,
                categoryId = null
            )
            i++
            TransactionAndAccounts(
                transaction,
                source.account,
                destination.account
            )
        }
    }.flatten()
    return transList
}

@Preview(showBackground = true)
@Composable
private fun PreviewTransactionList() {
    val transList = getTransactionSample()
    TransactionRecyclerView(
        transactionList = transList,
        editTransaction = {},
        delTransaction = {},
        state = LazyListState()
    )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun PreviewTransactionPage() {
    GazegeTheme(darkTheme = true) {
        TransactionPage(
            transactionList = getTransactionSample(),
            state = LazyListState(),
            editTransaction = {},
            delTransaction = {},
            onTitleSetted = {}
        )
    }
}