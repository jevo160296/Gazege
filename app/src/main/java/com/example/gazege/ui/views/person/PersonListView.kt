package com.example.gazege.ui.views.person

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.PersonSummaryState
import com.example.gazege.R
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.templates.ClickableListItemViewHolder
import com.example.gazege.ui.templates.SimpleLazyList
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.SmallEmphasis
import kotlin.math.absoluteValue

@Composable
private fun PersonViewHolder(
    principalPersonSummaryState: PersonSummaryState,
    person: Person
) {
    val flujo = principalPersonSummaryState.deudasFlujo[person] ?: 0.0
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(1F)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            LargeBody(text = person.name)
        }
        if (flujo != 0.0) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                SmallEmphasis(personDeudaString(flujo))
                LargeBody(text = doubleToMoneyString(flujo.absoluteValue))
            }
        }
    }
}

@Composable
fun personDeudaString(flujo: Double) = if (flujo > 0) {
    stringResource(id = R.string.me_debe)
} else {
    stringResource(R.string.le_debo)
}

@Composable
private fun PersonRecyclerView(
    principalPersonSummaryState: PersonSummaryState,
    personList: List<Person>,
    delPerson: (Person) -> Unit,
    editPerson: (Person) -> Unit,
    detailPerson: (Person) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    state: LazyListState
) {
    var menuIdExpanded: Int? by remember {
        mutableStateOf(null)
    }
    SimpleLazyList(
        modifier = modifier,
        state = state,
        contentPadding = itemHolderPaddingValues,
        items = personList
    ) {
        ClickableListItemViewHolder(
            onItemTapped = { detailPerson(it) },
            onItemLongPressed = { menuIdExpanded = it.id }
        ) {
            Box {
                PersonViewHolder(
                    person = it,
                    principalPersonSummaryState = principalPersonSummaryState
                )
                DropdownMenu(
                    expanded = menuIdExpanded == it.id,
                    onDismissRequest = { menuIdExpanded = null }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.Editar)) },
                        onClick = {
                            menuIdExpanded = null
                            editPerson(it)
                        })
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.Eliminar)) },
                        onClick = {
                            menuIdExpanded = null
                            delPerson(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    principalPersonSummaryState: PersonSummaryState?,
    allPerson: List<Person>,
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
        if (principalPersonSummaryState == null) {
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
                principalPersonSummaryState = principalPersonSummaryState,
                personList = allPerson,
                delPerson = { delPerson(it) },
                editPerson = { editPerson(it) },
                detailPerson = { detailPerson(it) },
                itemHolderPaddingValues = itemHolderPaddingValues,
                state = state
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonItem() {
    DatabaseSample {
        PersonViewHolder(
            person = personSample.first(),
            principalPersonSummaryState = personSummaryStateSample
        )
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 620)
@Composable
private fun PreviewPersonPage() {
    DatabaseSample {
        GazegeTheme {
            PersonPage(
                allPerson = personSample,
                state = LazyListState(),
                editPerson = {},
                delPerson = {},
                onTitleSetted = {},
                onConfigurePrincipalPersonRequested = {},
                itemHolderPaddingValues = PaddingValues(vertical = 50.dp),
                detailPerson = {},
                principalPersonSummaryState = personSummaryStateSample
            )
        }
    }
}