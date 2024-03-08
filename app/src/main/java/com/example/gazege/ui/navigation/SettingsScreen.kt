package com.example.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.ui.fragments.SettingsFragment

fun NavGraphBuilder.screenSettings(
    viewModelSettings: MainViewModel.ViewModelSettings,
    onNavigateUp: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToAddBudget: () -> Unit,
    onExportDataRequested: () -> Unit,
    onImportDataRequested: () -> Unit
) {
    composable("settings") {
        val allPerson by viewModelSettings.rememberAllPerson()
        val principalPerson by viewModelSettings.rememberPrincipalPerson()
        val accountAndOwner by viewModelSettings.rememberAccountAndOwner()
        val incomeAccount by viewModelSettings.rememberIncomeAccount()
        val outcomeAccount by viewModelSettings.rememberOutcomeAccount()

        SettingsFragment(
            personList = allPerson,
            principalPerson = principalPerson,
            onPrincipalPersonChanged = {
                val notNullPrincipalPerson = principalPerson
                if (notNullPrincipalPerson != null) {
                    viewModelSettings.updatePerson(
                        notNullPrincipalPerson.copy(importance = null)
                    ) {}
                }
                viewModelSettings.updatePerson(it.copy(importance = 1)) {}
            },
            onNavigateUpRequested = onNavigateUp,
            onAddPersonRequested = onNavigateToAddPerson,
            accountList = accountAndOwner,
            incomeAccount = incomeAccount,
            outcomeAccount = outcomeAccount,
            onAddAccountRequested = onNavigateToAddAccount,
            onIncomeOutcomeAccountChanged = { newIncome, newOutcome ->
                val castedIncomeAccount = incomeAccount
                val castedOutcomeAccount = outcomeAccount
                if (castedIncomeAccount != null) {
                    viewModelSettings.updateAccount(
                        castedIncomeAccount.copy(
                            isIncome = false
                        ), onCompleitionAction = {}, onErrorAction = {})
                }
                if (castedOutcomeAccount != null) {
                    viewModelSettings.updateAccount(
                        castedOutcomeAccount.copy(
                            isOutcome = false
                        ), onCompleitionAction = {}, onErrorAction = {})
                }
                if (newIncome != null) {
                    viewModelSettings.updateAccount(
                        newIncome.copy(
                            isIncome = true,
                            isOutcome = false
                        ), onErrorAction = {}, onCompleitionAction = {})
                }
                if (newOutcome != null) {
                    viewModelSettings.updateAccount(
                        newOutcome.copy(
                            isIncome = false,
                            isOutcome = true
                        ), onErrorAction = {}, onCompleitionAction = {})
                }
            },
            onAddBudgetRequested = onNavigateToAddBudget,
            onAddCategoryRequested = onNavigateToAddCategory,
            onExportDataRequested = onExportDataRequested,
            onImportDataRequested = onImportDataRequested
        )
    }
}

fun NavController.navigateToSettings() {
    navigate("settings")
}