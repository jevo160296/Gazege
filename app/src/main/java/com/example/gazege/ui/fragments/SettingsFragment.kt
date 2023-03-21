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
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.DropDownMenu
import com.example.gazege.ui.widgets.MediumHeadline

@Composable
fun SettingsFragment(
    personList: List<Person>,
    principalPerson: Person?,
    onPrincipalPersonChanged: (Person) -> Unit,
    accountList: List<AccountAndOwnerWithTransactions>,
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
    var incomeExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var outcomeExpanded by rememberSaveable {
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
        topBar = { MediumHeadline(text = "Ajustes") },
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(8.dp)) {
            if (personList.isEmpty()) {
                ButtonField(onClick = onAddPersonRequested) {
                    Text(text = "Nueva persona")
                }
            } else {
                DropDownMenu(
                    dropDownExpanded = principalPersonExpanded,
                    onExpandedChange = { principalPersonExpanded = it },
                    options = personList,
                    selectedItem = personSelected,
                    itemToString = { it?.name ?: "" },
                    onItemClick = { personIdSelected = it.id },
                    label = { Text("Principal person") }
                )
            }
            if (accountListNoOutcome.isEmpty()) {
                ButtonField(onClick = onAddAccountRequested) {
                    Text(text = "Nueva cuenta")
                }
            } else {
                DropDownMenu(
                    dropDownExpanded = incomeExpanded,
                    onExpandedChange = { incomeExpanded = it },
                    options = accountListNoOutcome,
                    selectedItem = incomeSelected,
                    itemToString = { it?.account?.name ?: "" },
                    onItemClick = { incomeIdSelected = it.account.id },
                    label = { Text("Income") }
                ) {
                    it.owner.name
                }
            }
            if (accountListNoIncome.isEmpty()) {
                ButtonField(onClick = onAddAccountRequested) {
                    Text(text = "Nueva cuenta")
                }
            } else {
                DropDownMenu(
                    dropDownExpanded = outcomeExpanded,
                    onExpandedChange = { outcomeExpanded = it },
                    options = accountListNoIncome,
                    selectedItem = outcomeSelected,
                    itemToString = { it?.account?.name ?: "" },
                    onItemClick = { outcomeIdSelected = it.account.id },
                    label = { Text("Outcome") }
                ) {
                    it.owner.name
                }
            }
        }
    }
}