package com.example.gazege.ui.views.person

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.entities.*
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
    principalPerson: PersonWithAccounts,
    person: PersonWithAccounts,
    transactions: List<TransactionAndAccounts>
) {
    val flujo = PersonDao.getFlujo(principalPerson, person, transactions)
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
    transacciones: List<TransactionAndAccounts>,
    delPerson: (PersonWithAccounts) -> Unit,
    editPerson: (PersonWithAccounts) -> Unit,
    detailPerson: (PersonWithAccounts) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    state: LazyListState
) {
    var menuIdExpanded: Int? by remember {
        mutableStateOf(null)
    }
    RecyclerView(
        elements = personList,
        modifier = modifier,
        onItemTapped = detailPerson,
        onItemLongPressed = {
            menuIdExpanded = it.person.id
        },
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state,
        viewHolder = {
            Box {
                PersonViewHolder(
                    person = it,
                    principalPerson = principalPerson,
                    transactions = transacciones
                )
                DropdownMenu(
                    expanded = menuIdExpanded == it.person.id,
                    onDismissRequest = { menuIdExpanded = null }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Edit") },
                        onClick = {
                            menuIdExpanded = null
                            editPerson(it)
                        })
                    DropdownMenuItem(
                        text = { Text(text = "Delete") },
                        onClick = {
                            menuIdExpanded = null
                            delPerson(it)
                        }
                    )
                }
            }
        }
    )

}

@Composable
fun PersonPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    principalPerson: PersonWithAccounts?,
    personList: List<PersonWithAccounts>,
    transacciones: List<TransactionAndAccounts>,
    delPerson: (Person) -> Unit,
    editPerson: (Person) -> Unit,
    detailPerson: (Person) -> Unit,
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
                detailPerson = { detailPerson(it.person) },
                itemHolderPaddingValues = itemHolderPaddingValues,
                state = state,
                transacciones = transacciones
            )
        }
    }
}

fun getPersonSample(): List<Person> {
    return listOf(
        "Pablo",
        "Banco",
        "Petunia",
        "Hortensia",
        "__ESPECIAL__"
    ).mapIndexed { index, s ->
        Person(
            index, s, if (index == 0) {
                1
            } else {
                null
            }
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
        AccountAndOwnerWithTransactionsAndPockets.from(
            AccountAndOwnerWithTransactions(
                account = account, outTransactions = (1..2).map {
                    Transaction(
                        amount = it.toDouble(),
                        description = "Trans",
                        sourceId = 1,
                        destinationId = 2,
                        date = LocalDate.now(),
                        aNombreDe = null,
                        categoryId = null
                    )
                }, inTransactions = (1..4).map {
                    Transaction(
                        amount = it.toDouble(),
                        description = "Trans2",
                        sourceId = 1,
                        destinationId = 2,
                        date = LocalDate.now(),
                        aNombreDe = null,
                        categoryId = null
                    )
                }, owner = person
            ),
            listOf()
        )
    })
    PersonViewHolder(
        person = personWithAccounts,
        principalPerson = personWithAccounts,
        transactions = listOf()
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonList() {
    GazegeTheme {
        RecyclerView(
            elements = getPersonWithAccountsSample(), viewHolder = { person ->
                PersonViewHolder(
                    person = person, principalPerson = person, transactions = listOf()
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
            itemHolderPaddingValues = PaddingValues(vertical = 50.dp),
            transacciones = listOf(),
            detailPerson = {}
        )
    }
}