package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.core.entities.Transaction
import com.example.gazege.ui.doubleToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.RecyclerView
import com.example.gazege.ui.widgets.SmallEmphasis
import java.time.LocalDate
import kotlin.math.absoluteValue

@Composable
private fun PersonViewHolder(
    principalPerson: PersonWithAccounts, person: PersonWithAccounts
) {
    val flujo = principalPerson.getFlujo(person)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(1F)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            SmallEmphasis(text = "${stringResource(id = R.string.nombre)}: ")
            LargeBody(text = person.person.name)
        }
        if (flujo != 0.0) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (flujo > 0) {
                    SmallEmphasis(text = stringResource(id = R.string.me_debe))
                    LargeBody(text = doubleToString(flujo.absoluteValue))
                } else {
                    SmallEmphasis(text = stringResource(R.string.le_debo))
                    LargeBody(text = doubleToString(flujo.absoluteValue))
                }
            }
        }
    }
}

@Composable
private fun PersonRecyclerView(
    principalPerson: PersonWithAccounts,
    personList: List<PersonWithAccounts>,
    delPerson: (PersonWithAccounts) -> Unit,
    editPerson: (PersonWithAccounts) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    state: LazyListState
) {
    RecyclerView(
        elements = personList,
        modifier = modifier,
        onItemTapped = editPerson,
        onItemLongPressed = delPerson,
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state
    ) {
        PersonViewHolder(person = it, principalPerson = principalPerson)
    }

}

@Composable
fun PersonPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    principalPerson: PersonWithAccounts?,
    personList: List<PersonWithAccounts>,
    delPerson: (Person) -> Unit,
    editPerson: (Person) -> Unit,
    state: LazyListState,
    onConfigurePrincipalPersonRequested: () -> Unit,
    onTitleSetted: (String) -> Unit
) {
    onTitleSetted(stringResource(id = R.string.personas))
    Column(modifier = modifier) {
        val padding = Modifier.padding(horizontal = 8.dp)
        if (principalPerson == null) {
            LargeBody(
                text = stringResource(id = R.string.persona_principal_vacia),
                modifier = padding,
                textAlign = TextAlign.Justify
            )
            ButtonField(onClick = onConfigurePrincipalPersonRequested, modifier = padding) {
                SmallEmphasis(text = stringResource(id = R.string.configurar_persona_principal))
            }
        } else {
            PersonRecyclerView(
                principalPerson = principalPerson,
                personList = personList,
                delPerson = { delPerson(it.person) },
                editPerson = { editPerson(it.person) },
                itemHolderPaddingValues = itemHolderPaddingValues,
                state = state
            )
        }
    }
}

fun getPersonSample(): List<Person> {
    return (1..40).map {
        Person(
            name = "Person $it"
        )
    }
}

fun getPersonWithAccountsSample(): List<PersonWithAccounts> {
    val persons = getPersonSample()
    return persons.map {
        PersonWithAccounts(
            person = it, accounts = listOf()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonItem() {
    val person = Person(name = "Persona")
    val account = Account(name = "Acc", ownerId = 0)
    val personWithAccounts = PersonWithAccounts(person = person, accounts = (0..10).map {
        AccountAndOwnerWithTransactions(
            account = account, outTransactions = (1..2).map {
                Transaction(
                    amount = it.toDouble(),
                    description = "Trans",
                    sourceId = 1,
                    destinationId = 2,
                    date = LocalDate.now()
                )
        }, inTransactions = (1..4).map {
            Transaction(
                amount = it.toDouble(),
                description = "Trans2",
                sourceId = 1,
                destinationId = 2,
                date = LocalDate.now()
            )
        }, owner = person
        )
    })
    PersonViewHolder(person = personWithAccounts, principalPerson = personWithAccounts)
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonList() {
    GazegeTheme {
        RecyclerView(
            elements = getPersonWithAccountsSample(), viewHolder = { person ->
                PersonViewHolder(
                    person = person, principalPerson = person
                )
            }, state = LazyListState()
        )
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 620)
@Composable
private fun PreviewPersonPage() {
    val persons = getPersonWithAccountsSample()
    GazegeTheme {
        PersonPage(
            personList = getPersonWithAccountsSample(),
            state = LazyListState(),
            editPerson = {},
            delPerson = {},
            onTitleSetted = {},
            principalPerson = persons[0],
            onConfigurePrincipalPersonRequested = {},
            itemHolderPaddingValues = PaddingValues(vertical = 50.dp)
        )
    }
}