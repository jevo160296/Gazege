package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.RecyclerView

@Composable
private fun AccountViewHolder(account: AccountAndOwner) {
    Column {
        Text(text = account.account.name)
        Text(text = account.owner.name)
    }
}

@Composable
fun AccountRecyclerView(accountList: List<AccountAndOwner>, modifier: Modifier = Modifier) {
    RecyclerView(elements = accountList, modifier = modifier) {
        AccountViewHolder(account = it)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAccountItem() {
    val owner = Person(id = 0, name = "Persona")
    val account = Account(name = "Cuenta 1", ownerId = 0, initial_balance = 0.0)
    val accountAndOwner = AccountAndOwner(owner = owner, account = account)
    AccountViewHolder(account = accountAndOwner)
}

@Preview(showBackground = true, widthDp = 240, heightDp = 320)
@Composable
private fun PreviewAccountList() {
    val personList = listOf(
        Person(name = "Persona1"),
        Person(name = "Persona2"),
        Person(name = "Persona3"),
        Person(name = "Persona4")
    )
    val accountList = personList.map { person ->
        listOf(1, 2, 3, 4, 5).map { index ->
            val account = Account(
                name = "Cuenta$index",
                ownerId = person.id ?: -1,
                initial_balance = (person.id ?: -1).toDouble() * 10 + index
            )
            AccountAndOwner(
                account = account,
                owner = person
            )
        }
    }.flatten().sortedBy { it.account.id }
    GazegeTheme() {
        AccountRecyclerView(accountList = accountList)
    }
}