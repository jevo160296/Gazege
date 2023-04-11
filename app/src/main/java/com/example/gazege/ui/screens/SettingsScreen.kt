package com.example.gazege.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.views.account.AccountDropDownMenu
import com.example.gazege.ui.views.transaction.AccountAndOwnerNode
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.DropDownMenu
import com.example.gazege.ui.widgets.Form

fun NavGraphBuilder.screenSettings(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToEditCategories: () -> Unit
) {
    composable("settings") {
        val allPerson by viewModel.allPerson.observeAsState(emptyList())
        val principalPerson by viewModel.principalPerson.observeAsState()
        val accountAndOwnerWithTransactions by viewModel.accountAndOwnerWithTransactions.observeAsState(
            emptyList()
        )
        val incomeAccount by viewModel.incomeAccount.observeAsState()
        val outcomeAccount by viewModel.outcomeAccount.observeAsState()

        SettingsScreen(
            personList = allPerson,
            principalPerson = principalPerson,
            onPrincipalPersonChanged = {
                val notNullPrincipalPerson = principalPerson
                if (notNullPrincipalPerson != null) {
                    viewModel.updatePerson(
                        notNullPrincipalPerson.copy(importance = null)
                    ) {}
                }
                viewModel.updatePerson(it.copy(importance = 1)) {}
            },
            onNavigateUpRequested = onNavigateUp,
            onAddPersonRequested = onNavigateToAddPerson,
            accountList = accountAndOwnerWithTransactions.map {
                AccountAndOwner(
                    it.account,
                    it.owner
                )
            },
            incomeAccount = incomeAccount,
            outcomeAccount = outcomeAccount,
            onAddAccountRequested = onNavigateToAddAccount,
            onIncomeOutcomeAccountChanged = { newIncome, newOutcome ->
                val castedIncomeAccount = incomeAccount
                val castedOutcomeAccount = outcomeAccount
                if (castedIncomeAccount != null) {
                    viewModel.updateAccount(
                        castedIncomeAccount.copy(
                            isIncome = false
                        ), onCompleitionAction = {}, onErrorAction = {})
                }
                if (castedOutcomeAccount != null) {
                    viewModel.updateAccount(
                        castedOutcomeAccount.copy(
                            isOutcome = false
                        ), onCompleitionAction = {}, onErrorAction = {})
                }
                if (newIncome != null) {
                    viewModel.updateAccount(
                        newIncome.copy(
                            isIncome = true,
                            isOutcome = false
                        ), onErrorAction = {}, onCompleitionAction = {})
                }
                if (newOutcome != null) {
                    viewModel.updateAccount(
                        newOutcome.copy(
                            isIncome = false,
                            isOutcome = true
                        ), onErrorAction = {}, onCompleitionAction = {})
                }
            },
            onEditCategoriesRequested = onNavigateToEditCategories
        )
    }
}

fun NavController.navigateToSettings() {
    navigate("settings")
}

@Composable
fun SettingsScreen(
    personList: List<Person>,
    principalPerson: Person?,
    onPrincipalPersonChanged: (Person) -> Unit,
    accountList: List<AccountAndOwner>,
    incomeAccount: Account?,
    outcomeAccount: Account?,
    onIncomeOutcomeAccountChanged: (Account?, Account?) -> Unit,
    onAddAccountRequested: () -> Unit,
    onAddPersonRequested: () -> Unit,
    onEditCategoriesRequested: () -> Unit,
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
    val deactivatedAccountListNoIncome = accountList.filter { it.account.id == incomeIdSelected }
    val deactivatedAccountListNoOutcome = accountList.filter { it.account.id == outcomeIdSelected }

    Form(
        onSaveClicked = {
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
        },
        isSavedButtonEnabled = true,
        title = "Settings",
        itemSpacing = 8.dp,
        itemsColumnsModifier = Modifier.padding(PaddingValues(8.dp))
    ) {
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
        if (accountList.isEmpty()) {
            ButtonField(onClick = onAddAccountRequested) {
                Text(text = stringResource(id = R.string.Nueva_cuenta))
            }
        } else {
            AccountDropDownMenu(
                accountsList = accountList,
                selectedAccountNode = incomeSelected?.let {
                    AccountAndOwnerNode(it, accountList, 0, 0, listOf(), null)
                },
                label = { Text(stringResource(id = R.string.Ingreso)) },
                onItemClick = { incomeIdSelected = it.content.account.id },
                deactivatedAccountList = deactivatedAccountListNoOutcome,
                canClearSelection = true,
                onClearSelectionClicked = { incomeIdSelected = null }
            )
        }
        if (accountList.isEmpty()) {
            ButtonField(onClick = onAddAccountRequested) {
                Text(text = stringResource(id = R.string.Nueva_cuenta))
            }
        } else {
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
                onClearSelectionClicked = { outcomeIdSelected = null }
            )
        }
        ButtonField(onClick = onEditCategoriesRequested) {
            Text(text = stringResource(id = R.string.ConfigurarCategorias))
        }
    }
}