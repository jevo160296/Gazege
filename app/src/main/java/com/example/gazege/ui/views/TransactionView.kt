package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.*
import com.example.gazege.ui.widgets.RecyclerView
import java.util.*

@Composable
private fun TransactionViewHolder(transaction: TransactionAndAccounts) {
    Column {
        Text(text = transaction.transaction.description)
        Text(text = transaction.sourceAccount.name)
        Text(text = transaction.destinationAccount.name)
        Text(text = transaction.transaction.amount.toString())
    }
}

@Composable
fun TransactionRecyclerView(transactionList: List<TransactionAndAccounts>,
                            modifier: Modifier = Modifier) {
    RecyclerView(elements = transactionList, modifier = modifier) {
        TransactionViewHolder(transaction = it)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAccountItem() {
    val person = Person(name="Person", id = 0)
    val sourceAccount = Account(name="Account1", ownerId = person.id ?: -1,
        initial_balance = 0.0)
    val destinationAccount = Account(name="Account2", ownerId = person.id ?: -1,
        initial_balance = 0.0)
    val transaction = Transaction(amount = 0.0, description = "Trans", date = Date(),
        destinationId = destinationAccount.id ?: -1, sourceId = sourceAccount.id ?: -1)
    val transactionAndAccounts = TransactionAndAccounts(
        transaction = transaction, sourceAccount = sourceAccount,
        destinationAccount=destinationAccount
    )
    TransactionViewHolder(transaction = transactionAndAccounts)
}

@Preview(showBackground = true)
@Composable
private fun PreviewAccountList() {
    val personList = listOf(
        Person(name = "Persona1"),
        Person(name = "Persona2"),
        Person(name = "Persona3"),
        Person(name = "Persona4")
    )
    val accountList = personList.map { person ->
        listOf(1, 2, 3, 4, 5).map{ index ->
            val account = Account(
                name = "Cuenta$index",
                ownerId = person.id ?: -1,
                initial_balance = (person.id ?: -1).toDouble()*10 + index
            )
            AccountAndOwner(
                account = account,
                owner = person
            )
        }
    }.flatten().sortedBy { it.account.id }
    var i = 0
    val transList = accountList.map { source ->
        accountList.map { destination ->
            val transaction = Transaction(
                amount = i * 10.0,
                description = "Trans $i",
                sourceId = source.account.id ?: -1,
                destinationId = destination.account.id ?: -1,
                date = Date()
            )
            i++
            TransactionAndAccounts(
                transaction,
                source.account,
                destination.account
            )
        }
    }.flatten()
    TransactionRecyclerView(transactionList = transList)
}