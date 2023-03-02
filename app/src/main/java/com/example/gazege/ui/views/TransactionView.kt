package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import com.example.gazege.ui.doubleToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.RecyclerView
import com.example.gazege.ui.widgets.SmallBody
import com.example.gazege.ui.widgets.SmallEmphasis
import java.time.LocalDate

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
                date = LocalDate.now()
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

@Composable
private fun TransactionViewHolder(
    transaction: TransactionAndAccounts
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row {
                SmallEmphasis(text = "Source account: ")
                SmallBody(text = transaction.sourceAccount.name)
            }
            Row {
                SmallEmphasis(text = "Destination account: ")
                SmallBody(text = transaction.destinationAccount.name)
            }
            Row {
                SmallEmphasis(text = "Description: ")
                SmallBody(text = transaction.transaction.description)
            }
        }
        Column(
            modifier = Modifier.align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.End
        ) {
            SmallEmphasis(text = "Amount")
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
        state = state
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
) {
    Column(modifier = modifier) {
        MediumHeadline("Transacciones")
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
        name = "Account1", ownerId = person.id ?: -1,
        initial_balance = 0.0
    )
    val destinationAccount = Account(
        name = "Account2", ownerId = person.id ?: -1,
        initial_balance = 0.0
    )
    val transaction = Transaction(
        amount = 0.0, description = "Trans", date = LocalDate.now(),
        destinationId = destinationAccount.id ?: -1, sourceId = sourceAccount.id ?: -1
    )
    val transactionAndAccounts = TransactionAndAccounts(
        transaction = transaction, sourceAccount = sourceAccount,
        destinationAccount = destinationAccount
    )
    TransactionViewHolder(transaction = transactionAndAccounts)
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
            delTransaction = {}
        )
    }
}