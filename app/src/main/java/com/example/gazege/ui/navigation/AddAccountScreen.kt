package com.example.gazege.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.ui.fragments.AccountFormFragment
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenAddAccount(
    viewModel: MainViewModel,
    onNavigateToAddPerson: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable("addAccount") {
        val allPerson by viewModel.rememberAllPerson()
        val allAccount by viewModel.rememberAllAccount()
        val incomeAccount by viewModel.rememberIncomeAccount()
        val outcomeAccount by viewModel.rememberOutcomeAccount()
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()

        val coroutineScope = rememberCoroutineScope()
        AccountFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            personList = allPerson,
            onPersonAddRequested = onNavigateToAddPerson,
            onAccountAndOwnerAdd = { account, newBalance, snackBarHostState, incomeAccountId, outcomeAccountId ->
                val accountOwnerIdList = allAccount.map {
                    Pair(it.name, it.ownerId)
                }
                val accountOwnerId = Pair(account.name, account.ownerId)
                val sePuedeAgregar = accountOwnerId !in accountOwnerIdList
                if (sePuedeAgregar) {
                    viewModel.insertAccount(
                        account,
                        onErrorAction = {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Error añadiento cuenta $it")
                            }
                        },
                        onCompleitionAction = { addedId ->
                            if (incomeAccountId != null && outcomeAccountId != null) {
                                val valorAjuste = newBalance
                                viewModel.realizarAjuste(
                                    accountId = addedId.toInt(),
                                    amount = valorAjuste,
                                    incomeAccountId = incomeAccountId,
                                    outcomeAccountId = outcomeAccountId
                                )
                            }
                        }).invokeOnCompletion {
                        if (it == null) {
                            onNavigateUp()
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("Las personas no pueden tener cuentas con nombres repetidos")
                    }
                }
            },
            currentBalance = 0.0,
            incomeAccount = incomeAccount,
            outcomeAccount = outcomeAccount,
            onSetIncomeOutcomeAccount = onNavigateToSettings,
            accountAndOwnerList = accountAndOwnerWithTransactions.map {
                AccountAndOwner(it.account, it.owner)
            }
        )
    }
}

fun NavController.navigateToAddAccount() {
    navigate("addAccount")
}