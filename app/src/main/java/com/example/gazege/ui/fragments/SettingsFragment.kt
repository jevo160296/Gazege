package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.views.AccountAndOwnerNode
import com.example.gazege.ui.views.AccountDropDownMenu
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.DropDownMenu
import com.example.gazege.ui.widgets.MediumHeadline

@Composable
fun SettingsFragment(
    personList: List<Person>,
    principalPerson: Person?,
    onPrincipalPersonChanged: (Person) -> Unit,
    accountList: List<AccountAndOwner>,
    incomeAccount: Account?,
    outcomeAccount: Account?,
    onIncomeOutcomeAccountChanged: (Account?, Account?) -> Unit,
    onAddAccountRequested: () -> Unit,
    onAddPersonRequested: () -> Unit,
    onNavigateUpRequested: () -> Unit
) {
    var principalPersonExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var incomeIdSelected by rememberSaveable {
        mutableStateOf(incomeAccount?.id)
    }
    var outcomeIdSelected by rememberSaveable {
        mutableStateOf(outcomeAccount?.id)
    }
    var personIdSelected by rememberSaveable {
        mutableStateOf(principalPerson?.id)
    }
    val personSelected = personList.firstOrNull { it.id == personIdSelected }
    val incomeSelected = accountList.firstOrNull { it.account.id == incomeIdSelected }
    val outcomeSelected = accountList.firstOrNull { it.account.id == outcomeIdSelected }
    val accountListNoIncome = accountList.filter { it.account.id != incomeIdSelected }
    val accountListNoOutcome = accountList.filter { it.account.id != outcomeIdSelected }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
                onClick = {
                    if (personSelected != null) {
                        onPrincipalPersonChanged(personSelected)
                    }
                    if (incomeSelected != null || outcomeSelected != null) {
                        onIncomeOutcomeAccountChanged(
                            incomeSelected?.account,
                            outcomeSelected?.account
                        )
                    }
                    onNavigateUpRequested()
                }, shape = Shapes.small
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.round_save_24),
                    contentDescription = "Save"
                )
            }
        },
        topBar = { MediumHeadline(text = stringResource(id = R.string.Ajustes)) },
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(8.dp)) {
            if (personList.isEmpty()) {
                ButtonField(onClick = onAddPersonRequested) {
                    Text(text = stringResource(id = R.string.Nueva_persona))
                }
            } else {
                DropDownMenu(
                    dropDownExpanded = principalPersonExpanded,
                    onExpandedChange = { principalPersonExpanded = it },
                    options = personList,
                    selectedItem = personSelected,
                    itemToString = { it?.name ?: "" },
                    onItemClick = { personIdSelected = it.id },
                    label = { Text(stringResource(id = R.string.Persona_principal)) }
                )
            }
            if (accountListNoOutcome.isEmpty()) {
                ButtonField(onClick = onAddAccountRequested) {
                    Text(text = stringResource(id = R.string.Nueva_cuenta))
                }
            } else {
                AccountDropDownMenu(
                    accountsList = accountListNoOutcome,
                    selectedAccountNode = incomeSelected?.let {
                        AccountAndOwnerNode(it, accountListNoOutcome, 0, 0)
                    },
                    label = { Text(stringResource(id = R.string.Ingreso)) },
                    onItemClick = { incomeIdSelected = it.content.account.id }
                )
            }
            if (accountListNoIncome.isEmpty()) {
                ButtonField(onClick = onAddAccountRequested) {
                    Text(text = stringResource(id = R.string.Nueva_cuenta))
                }
            } else {
                AccountDropDownMenu(
                    accountsList = accountListNoIncome,
                    selectedAccountNode = outcomeSelected?.let {
                        AccountAndOwnerNode(
                            it,
                            accountListNoIncome,
                            0,
                            0
                        )
                    },
                    label = { Text(stringResource(id = R.string.Gasto)) },
                    onItemClick = { outcomeIdSelected = it.content.account.id }
                )
            }
        }
    }
}