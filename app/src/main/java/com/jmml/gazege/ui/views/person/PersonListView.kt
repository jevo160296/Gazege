package com.jmml.gazege.ui.views.person

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.navigation.FullPersonSummaryState
import com.jmml.gazege.ui.templates.ClickableListItemViewHolder
import com.jmml.gazege.ui.templates.SimpleLazyList
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.ButtonField
import com.jmml.gazege.ui.widgets.LargeBody
import com.jmml.gazege.ui.widgets.SmallEmphasis
import kotlin.math.absoluteValue

@Composable
private fun LoadedPersonViewHolder(
    principalPersonSummaryState: FullPersonSummaryState,
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
private fun LoadedPersonRecyclerView(
    principalPersonSummaryState: FullPersonSummaryState,
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
                LoadedPersonViewHolder(
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
fun LoadedPersonPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    principalPersonSummaryState: FullPersonSummaryState,
    allPerson: List<Person>,
    delPerson: (Person) -> Unit,
    editPerson: (Person) -> Unit,
    detailPerson: (Person) -> Unit,
    state: LazyListState,
    nestedScrollConnection: NestedScrollConnection? = null,
    onZeroElementsChanged: (Boolean) -> Unit,
    onTitleSetted: (String) -> Unit,
    onFirstElementVisibleChanged: (isVisible: Boolean) -> Unit
) {
    LaunchedEffect(allPerson.isEmpty()) { onZeroElementsChanged(allPerson.isEmpty()) }
    val firstElementIsVisible by remember { derivedStateOf { state.firstVisibleItemIndex == 0 } }
    LaunchedEffect(firstElementIsVisible) { onFirstElementVisibleChanged(firstElementIsVisible) }
    onTitleSetted(stringResource(id = R.string.personas))
    Column(modifier = modifier) {
        LoadedPersonRecyclerView(
            principalPersonSummaryState = principalPersonSummaryState,
            personList = allPerson,
            delPerson = { delPerson(it) },
            editPerson = { editPerson(it) },
            detailPerson = { detailPerson(it) },
            itemHolderPaddingValues = itemHolderPaddingValues,
            state = state,
            modifier = nestedScrollConnection?.let { Modifier.nestedScroll(nestedScrollConnection) }
                ?: Modifier
        )
    }
}

@Composable
fun PersonSelectionPage(
    modifier: Modifier = Modifier,
    principalPersonSummaryState: FullPersonSummaryState,
    personList: List<Person>,
    enabled: Boolean = true,
    onPersonStateChanged: (Person, newValue: Boolean) -> Unit,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues()
) {
    SimpleLazyList(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        items = personList
    ) { person ->
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
            ) {
                Checkbox(
                    checked = person.debtsIncludedInTotal,
                    enabled = enabled,
                    onCheckedChange = {
                        onPersonStateChanged(person, it)
                    })
                Text(text = person.name)
            }
            Text(text = principalPersonSummaryState.deudasFlujo[person]
                ?.takeIf { it != 0.0 }
                ?.let { valor -> "${personDeudaString(flujo = valor)} ${doubleToMoneyString(valor.absoluteValue)}" }
                ?: ""
            )
        }
    }
}

@Composable
fun NoPrincipalPersonPersonPage(
    onConfigurePrincipalPersonRequested: () -> Unit,
    onTitleSetted: (String) -> Unit
) {
    val padding = Modifier.padding(horizontal = 8.dp)
    onTitleSetted(stringResource(id = R.string.personas))
    Column {
        LargeBody(
            text = stringResource(id = R.string.persona_principal_vacia),
            modifier = padding,
            textAlign = TextAlign.Justify
        )
        ButtonField(onClick = onConfigurePrincipalPersonRequested, modifier = padding) {
            SmallEmphasis(text = stringResource(id = R.string.configurar_persona_principal))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonItem() {
    DatabaseSample {
        LoadedPersonViewHolder(
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
            LoadedPersonPage(
                itemHolderPaddingValues = PaddingValues(vertical = 50.dp),
                principalPersonSummaryState = personSummaryStateSample,
                allPerson = personSample,
                delPerson = {},
                editPerson = {},
                detailPerson = {},
                state = LazyListState(),
                onZeroElementsChanged = {},
                onTitleSetted = {},
                onFirstElementVisibleChanged = { }
            )
        }
    }
}