package com.jmml.gazege.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.ui.fragments.LoadingSaldoActualSettings
import com.jmml.gazege.ui.fragments.SaldoActualSettings
import com.jmml.zoo.clases.Result
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenSaldoActualSettings(
    viewModelSaldoActualSettings: MainViewModel.ViewModelSaldoActualSettings
) {
    composable("saldoActualSettings") {
        val accountAndOwnerWithTransactions =
            viewModelSaldoActualSettings.rememberAccountAndOwnerWithTransactions().value
        val personSummaryState by viewModelSaldoActualSettings.rememberPersonSummaryState()
        val principalPerson by viewModelSaldoActualSettings.rememberPrincipalPerson()
        val personList by viewModelSaldoActualSettings.rememberPersonList(principalPersonId = principalPerson?.id)
        val incluirPresupuestoEnSaldoActual by viewModelSaldoActualSettings.rememberSettingsIncluirPresupuestoEnSaldoActualFlow()
        val incluirDeudasEnSaldoActual by viewModelSaldoActualSettings.rememberSettingsIncluirDeudasEnSaldoActualFlow()
        val coroutineScope = rememberCoroutineScope()

        var saving: Int by remember { mutableIntStateOf(0) }
        when (accountAndOwnerWithTransactions) {
            is Result.Error -> Text("Error ${accountAndOwnerWithTransactions.exception}")
            Result.Loading -> LoadingSaldoActualSettings()
            is Result.Success -> SaldoActualSettings(
                accountAndOwnerWithTransactions.data.filter { it.owner.id == principalPerson?.id },
                personList = personList,
                summaryState = personSummaryState,
                saving = saving,
                incluirDeudasEnSaldoActual = incluirDeudasEnSaldoActual,
                incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual,
                onIncluirDeudasEnSaldoActualChanged = viewModelSaldoActualSettings::settingsIncluirDeudasEnSaldoActualFlow,
                onIncluirPresupuestoEnSaldoActualChanged = viewModelSaldoActualSettings::settingsIncluirPresupuestoEnSaldoActualFlow,
                onPersonStateChanged = { person, nuevoValor ->
                    saving += 1
                    coroutineScope.launch {
                        viewModelSaldoActualSettings
                            .updatePerson(person.copy(debtsIncludedInTotal = nuevoValor)) {}.join()
                    }.invokeOnCompletion {
                        if (it?.cause == null) {
                            saving -= 1
                        }
                    }
                }
            ) { account, nuevoEstado ->
                saving += 1
                coroutineScope.launch {
                    viewModelSaldoActualSettings.updateAccount(
                        account = account.copy(includedInTotal = nuevoEstado),
                        onErrorAction = {},
                        onCompleitionAction = {}).join()
                }.invokeOnCompletion {
                    saving -= 1
                }
            }
        }
    }
}

fun NavController.navigateToSaldoActualSettings() {
    navigate("saldoActualSettings")
}