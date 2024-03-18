package com.example.gazege.ui.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.views.account.AccountDropDownMenu
import com.example.gazege.ui.views.transaction.AccountAndOwnerNode
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.ComboBox
import com.example.gazege.ui.widgets.Form
import com.example.gazege.ui.widgets.GazegeSegmentedButton
import com.example.gazege.ui.widgets.SegmentedButtonItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsFragment(
    personList: List<Person>,
    principalPerson: Person?,
    onPrincipalPersonChanged: (Person) -> Unit,
    accountList: List<AccountAndOwner>,
    incomeAccount: Account?,
    outcomeAccount: Account?,
    showOnBoardingNextRestart: Boolean?,
    onIncomeOutcomeAccountChanged: (Account?, Account?) -> Unit,
    onAddAccountRequested: () -> Unit,
    onAddPersonRequested: () -> Unit,
    onAddCategoryRequested: () -> Unit,
    onAddBudgetRequested: () -> Unit,
    onNavigateUpRequested: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onExportDataRequested: () -> Unit,
    onShowOnBoardingNextRestart: (value: Boolean) -> Unit,
    onImportDataRequested: () -> Unit
) {
    val showOnBoardingEnabled = showOnBoardingNextRestart != null && !showOnBoardingNextRestart

    var principalPersonExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var incomeIdSelected by rememberSaveable(incomeAccount?.id) {
        mutableStateOf(incomeAccount?.id)
    }
    var outcomeIdSelected by rememberSaveable(outcomeAccount?.id) {
        mutableStateOf(outcomeAccount?.id)
    }
    var personIdSelected by rememberSaveable(principalPerson?.id) {
        mutableStateOf(principalPerson?.id)
    }
    val personSelected = personList.firstOrNull { it.id == personIdSelected }
    val incomeSelected = accountList.firstOrNull { it.account.id == incomeIdSelected }
    val outcomeSelected = accountList.firstOrNull { it.account.id == outcomeIdSelected }
    val deactivatedAccountListNoIncome = accountList.filter { it.account.id == incomeIdSelected }
    val deactivatedAccountListNoOutcome = accountList.filter { it.account.id == outcomeIdSelected }

    Form(
        onSaveClicked = {
            if (personSelected != null) {
                onPrincipalPersonChanged(personSelected)
            }
            onIncomeOutcomeAccountChanged(
                incomeSelected?.account,
                outcomeSelected?.account
            )
            onNavigateUpRequested()
        },
        isSavedButtonEnabled = true,
        title = stringResource(R.string.Ajustes),
        itemSpacing = 8.dp,
        itemsColumnsModifier = Modifier.padding(PaddingValues(8.dp))
    ) {
        GazegeSegmentedButton(
            modifier = Modifier.fillMaxWidth(),
            selectedIndex = null,
            items = listOf(
                SegmentedButtonItem(
                    text = {
                        Text(text = "Importar data")
                    },
                    leadingIcon = {}
                ),
                SegmentedButtonItem(
                    text = {
                        Text(text = "Exportar data")
                    },
                    leadingIcon = {}
                )
            )) {
            when (it) {
                0 -> onImportDataRequested()
                1 -> onExportDataRequested()
            }
        }
        ButtonField(
            onClick = onNavigateToBudget
        ) {
            Icon(
                painter = painterResource(id = R.drawable.presupuesto),
                contentDescription = stringResource(
                    id = R.string.Presupuesto
                )
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
            Text(stringResource(id = R.string.Presupuesto))
        }
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple(
                    stringResource(id = R.string.Nueva_persona),
                    painterResource(id = R.drawable.ic_baseline_person_24),
                    onAddPersonRequested
                ),
                Triple(
                    stringResource(R.string.Nueva_cuenta),
                    painterResource(id = R.drawable.ic_baseline_account_balance_wallet_24),
                    onAddAccountRequested
                ),
                Triple(
                    "Nueva categoría",
                    painterResource(id = R.drawable.categorias),
                    onAddCategoryRequested
                ),
                Triple(
                    "Nuevo presupuesto",
                    painterResource(id = R.drawable.presupuesto),
                    onAddBudgetRequested
                )
            )
                .map {
                    OutlinedCard(
                        onClick = it.third,
                        modifier = Modifier
                            .width(120.dp)
                            .height(120.dp)
                    ) {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterVertically
                            )
                        ) {
                            Icon(painter = it.second, contentDescription = it.first)
                            Text(text = it.first, textAlign = TextAlign.Center)
                        }
                    }
                }
        }
        if (personList.isEmpty()) {
            ButtonField(onClick = onAddPersonRequested) {
                Text(text = stringResource(id = R.string.Nueva_persona))
            }
        } else {
            ComboBox(
                dropDownExpanded = principalPersonExpanded,
                onExpandedChange = { principalPersonExpanded = it },
                options = personList,
                selectedItem = personSelected,
                itemToString = { it?.name ?: "" },
                onItemClick = { personIdSelected = it.id },
                label = { Text(stringResource(id = R.string.Persona_principal)) }
            )
        }
        AccountDropDownMenu(
            accountsList = accountList,
            selectedAccountNode = incomeSelected?.let {
                AccountAndOwnerNode(it, accountList, 0, 0, listOf(), null)
            },
            label = { Text(stringResource(id = R.string.Ingreso)) },
            onItemClick = { incomeIdSelected = it.content.account.id },
            deactivatedAccountList = deactivatedAccountListNoOutcome,
            canClearSelection = true,
            onClearSelectionClicked = { incomeIdSelected = null },
            onAccountAddRequested = onAddAccountRequested
        )
        AccountDropDownMenu(
            accountsList = accountList,
            selectedAccountNode = outcomeSelected?.let {
                AccountAndOwnerNode(
                    it,
                    accountList,
                    0,
                    0,
                    listOf(),
                    null
                )
            },
            label = { Text(stringResource(id = R.string.Gasto)) },
            onItemClick = { outcomeIdSelected = it.content.account.id },
            deactivatedAccountList = deactivatedAccountListNoIncome,
            canClearSelection = true,
            onClearSelectionClicked = { outcomeIdSelected = null },
            onAccountAddRequested = onAddAccountRequested
        )
        ButtonField(
            onClick = { onShowOnBoardingNextRestart(true) },
            enabled = showOnBoardingEnabled
        ) {
            AnimatedVisibility(visible = !showOnBoardingEnabled) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_check_24),
                    contentDescription = stringResource(
                        id = R.string.showOnboardingNextRestart
                    )
                )
            }
            AnimatedVisibility(visible = !showOnBoardingEnabled) {
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
            }
            Text(stringResource(id = R.string.showOnboardingNextRestart))
        }
    }
}