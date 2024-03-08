package com.example.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.ui.fragments.SaldoActualSettings
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenSaldoActualSettings(
    viewModelSaldoActualSettings: MainViewModel.ViewModelSaldoActualSettings
) {
    composable("saldoActualSettings") {
        val accountAndOwnerWithTransactions by viewModelSaldoActualSettings.rememberAccountAndOwnerWithTransactions()
        val personSummaryState by viewModelSaldoActualSettings.rememberPersonSummaryState()
        val principalPerson by viewModelSaldoActualSettings.rememberPrincipalPerson()
        val incluirPresupuestoEnSaldoActual by viewModelSaldoActualSettings.rememberSettingsIncluirPresupuestoEnSaldoActualFlow()
        val incluirDeudasEnSaldoActual by viewModelSaldoActualSettings.rememberSettingsIncluirDeudasEnSaldoActualFlow()
        val coroutineScope = rememberCoroutineScope()

        var saving: Int by remember { mutableIntStateOf(0) }
        SaldoActualSettings(
            accountAndOwnerWithTransactions.filter { it.owner.id == principalPerson?.id },
            summaryState = personSummaryState,
            saving = saving,
            incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual,
            incluirDeudasEnSaldoActual = incluirDeudasEnSaldoActual,
            onIncluirPresupuestoEnSaldoActualChanged = viewModelSaldoActualSettings::settingsIncluirPresupuestoEnSaldoActualFlow,
            onIncluirDeudasEnSaldoActualChanged = viewModelSaldoActualSettings::settingsIncluirDeudasEnSaldoActualFlow
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