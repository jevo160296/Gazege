package com.jmml.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.ui.fragments.SaldoActualSettings
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenSaldoActualSettings(
    viewModelSaldoActualSettings: MainViewModel.ViewModelSaldoActualSettings
) {
    composable("saldoActualSettings") {
        val accountAndOwnerWithTransactions by viewModelSaldoActualSettings.rememberAccountAndOwnerWithTransactions()
        val personSummaryState by viewModelSaldoActualSettings.rememberPersonSummaryState()
        val principalPerson by viewModelSaldoActualSettings.rememberPrincipalPerson()
        val personList by viewModelSaldoActualSettings.rememberPersonList(principalPersonId = principalPerson?.id)
        val incluirPresupuestoEnSaldoActual by viewModelSaldoActualSettings.rememberSettingsIncluirPresupuestoEnSaldoActualFlow()
        val incluirDeudasEnSaldoActual by viewModelSaldoActualSettings.rememberSettingsIncluirDeudasEnSaldoActualFlow()
        val coroutineScope = rememberCoroutineScope()

        var saving: Int by remember { mutableIntStateOf(0) }
        SaldoActualSettings(
            accountAndOwnerWithTransactions.filter { it.owner.id == principalPerson?.id },
            personList = personList,
            summaryState = personSummaryState,
            saving = saving,
            incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual,
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

fun NavController.navigateToSaldoActualSettings() {
    navigate("saldoActualSettings")
}