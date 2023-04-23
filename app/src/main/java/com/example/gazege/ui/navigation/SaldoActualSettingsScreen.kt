package com.example.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    viewModel: MainViewModel
) {
    composable("saldoActualSettings") {
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()
        val principalPerson by viewModel.rememberPrincipalPerson()
        val incluirPresupuestoEnSaldoActual by viewModel.rememberSettingsIncluirPresupuestoEnSaldoActualFlow()
        val incluirDeudasEnSaldoActual by viewModel.rememberSettingsIncluirDeudasEnSaldoActualFlow()
        val coroutineScope = rememberCoroutineScope()

        var saving: Int by remember { mutableStateOf(0) }
        SaldoActualSettings(
            accountAndOwnerWithTransactions.filter { it.owner.id == principalPerson?.id },
            saving = saving,
            incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual,
            incluirDeudasEnSaldoActual = incluirDeudasEnSaldoActual,
            onIncluirPresupuestoEnSaldoActualChanged = viewModel::settingsIncluirPresupuestoEnSaldoActualFlow,
            onIncluirDeudasEnSaldoActualChanged = viewModel::settingsIncluirDeudasEnSaldoActualFlow
        ) { account, nuevoEstado ->
            saving += 1
            coroutineScope.launch {
                viewModel.updateAccount(
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