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
        val currentCashSettingsState =
            viewModelSaldoActualSettings.rememberCurrentCashSettingsState().value
        var saving: Int by remember { mutableIntStateOf(0) }

        when (currentCashSettingsState) {
            is Result.Error -> Text("Error ${currentCashSettingsState.exception}")
            Result.Loading -> LoadingSaldoActualSettings()
            is Result.Success -> {
                val accountAndOwnerWithTransactions =
                    currentCashSettingsState.data.accountAndOwnerWithTransactions
                val personSummaryState = currentCashSettingsState.data.personSummaryState
                val principalPerson = currentCashSettingsState.data.principalPerson
                val personList = currentCashSettingsState.data.personList
                val incluirPresupuestoEnSaldoActual =
                    currentCashSettingsState.data.incluirPresupuestoEnSaldoActual
                val incluirDeudasEnSaldoActual =
                    currentCashSettingsState.data.incluirDeudasEnSaldoActual
                val coroutineScope = rememberCoroutineScope()
                SaldoActualSettings(
                    accountAndOwnerWithTransactions.filter { it.owner.id == principalPerson?.id },
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
                                .updatePerson(person.copy(debtsIncludedInTotal = nuevoValor)) {}
                                .join()
                        }.invokeOnCompletion {
                            if (it?.cause == null) {
                                saving -= 1
                            }
                        }
                    },
                    onUpdateSeleccion = { account, nuevoEstado ->
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
                )
            }
        }
    }
}

fun NavController.navigateToSaldoActualSettings() {
    navigate("saldoActualSettings")
}