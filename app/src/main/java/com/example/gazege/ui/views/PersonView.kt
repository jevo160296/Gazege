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
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.core.entities.Transaction
import com.example.gazege.ui.doubleToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.RecyclerView
import com.example.gazege.ui.widgets.SmallEmphasis
import java.time.LocalDate

fun getPersonSample(): List<Person> {
    return (1..4).map {
        Person(
            name = "Person $it"
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
private fun PersonViewHolder(
    person: PersonWithAccounts,
    startDate: LocalDate?,
    endDate: LocalDate?
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(1F)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            SmallEmphasis(text = "Name: ")
            LargeBody(text = person.person.name)
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            SmallEmphasis(text = "Total")
            LargeBody(text = doubleToString(person.getTotal(startDate, endDate)))
        }
    }
}

@Composable
private fun PersonRecyclerView(
    personList: List<PersonWithAccounts>,
    delPerson: (PersonWithAccounts) -> Unit,
    editPerson: (PersonWithAccounts) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    state: LazyListState,
    startDate: LocalDate?,
    endDate: LocalDate?
) {
    RecyclerView(
        elements = personList,
        modifier = modifier,
        onItemTapped = editPerson,
        onItemLongPressed = delPerson,
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state
    ) {
        PersonViewHolder(person = it, startDate, endDate)
    }
}

@Composable
fun PersonPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    personList: List<PersonWithAccounts>,
    delPerson: (Person) -> Unit,
    editPerson: (Person) -> Unit,
    state: LazyListState,
    startDate: LocalDate?,
    endDate: LocalDate?
) {
    Column(modifier = modifier) {
        MediumHeadline(text = "Persons")
        PersonRecyclerView(
            personList = personList,
            delPerson = { delPerson(it.person) },
            editPerson = { editPerson(it.person) },
            itemHolderPaddingValues = itemHolderPaddingValues,
            state = state,
            startDate = startDate,
            endDate = endDate
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonItem() {
    val person = Person(name = "Persona")
    val account = Account(name = "Acc", ownerId = 0, initial_balance = 0.0)
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
                        date = LocalDate.now()
                    )
                },
                inTransactions = (1..4).map {
                    Transaction(
                        amount = it.toDouble(),
                        description = "Trans2",
                        sourceId = 1,
                        destinationId = 2,
                        date = LocalDate.now()
                    )
                },
                owner = person
            )
        }
    )
    PersonViewHolder(person = personWithAccounts, startDate = null, endDate = null)
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonList() {
    GazegeTheme {
        RecyclerView(
            elements = getPersonWithAccountsSample(),
            viewHolder = { person ->
                PersonViewHolder(
                    person = person, startDate = null,
                    endDate = null
                )
            },
            state = LazyListState()
        )
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 620)
@Composable
private fun PreviewPersonPage() {
    GazegeTheme {
        PersonPage(
            personList = getPersonWithAccountsSample(),
            state = LazyListState(),
            editPerson = {},
            delPerson = {},
            startDate = null,
            endDate = null
        )
    }
}