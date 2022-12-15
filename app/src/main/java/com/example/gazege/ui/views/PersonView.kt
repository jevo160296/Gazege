package com.example.gazege.ui.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.*
import com.example.gazege.ui.doubleToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.RecyclerView
import com.example.gazege.ui.widgets.SmallBody
import com.example.gazege.ui.widgets.SmallEmphasis
import java.util.*

fun getPersonSample(): List<Person>{
    return (1..4).map {
        Person(
            name = "Person %".format(it)
        )
    }
}

fun getPersonWithAccountsSample(): List<PersonWithAccounts> {
    val persons = getPersonSample()
    return persons.map {
        PersonWithAccounts(
            person = it,
            accounts = listOf()
        )
    }
}

@Composable
private fun PersonViewHolder(person: PersonWithAccounts) {
    Row {
        Row{
            SmallEmphasis(text = "Name: ")
            SmallBody(text = person.person.name)
        }
        Row{
            SmallEmphasis(text = "Total: ")
            SmallBody(text = doubleToString(person.getTotal()))
        }
    }
}

@Composable
fun PersonRecyclerView(
    personList: List<PersonWithAccounts>,
    onItemTapped: (PersonWithAccounts) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    state: LazyListState
) {
    RecyclerView(
        elements = personList,
        modifier = modifier,
        onItemTapped = onItemTapped,
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state
    ) {
        PersonViewHolder(person = it)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonItem() {
    val person = Person(name = "Persona")
    val account = Account(name="Acc", ownerId = 0, initial_balance = 0.0)
    val personWithAccounts = PersonWithAccounts(
        person = person,
        accounts = (0..10).map {
            AccountAndOwnerWithTransactions(
                account = account,
                outTransactions = (1..2).map {
                    Transaction(
                        amount = it.toDouble(),
                        description = "Trans",
                        sourceId = 1,
                        destinationId = 2,
                        date = Date()
                    )
                },
                inTransactions = (1..4).map {
                    Transaction(
                        amount = it.toDouble(),
                        description = "Trans2",
                        sourceId = 1,
                        destinationId = 2,
                        date = Date()
                    )
                },
                owner = person
            )
        }
    )
    PersonViewHolder(person = personWithAccounts)
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonList() {
    GazegeTheme {
        RecyclerView(
            elements = getPersonWithAccountsSample(),
            viewHolder = { person -> PersonViewHolder(person = person) },
            state = LazyListState()
        )
    }
}