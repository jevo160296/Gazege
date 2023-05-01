package com.example.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.ui.fragments.SettingsFragment

fun NavGraphBuilder.screenSettings(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToAddAccount: () -> Unit
) {
    composable("settings") {
        val allPerson by viewModel.rememberAllPerson()
        val principalPerson by viewModel.rememberPrincipalPerson()
        val accountAndOwner by viewModel.rememberAccountAndOwner()
        val incomeAccount by viewModel.rememberIncomeAccount()
        val outcomeAccount by viewModel.rememberOutcomeAccount()

        SettingsFragment(
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
            accountList = accountAndOwner,
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
            }
        )
    }
}

fun NavController.navigateToSettings() {
    navigate("settings")
}