package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.ui.views.account.AccountPage
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.treeview.rememberTreeState
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenSaldoActualSettings(
    viewModel: MainViewModel
) {
    composable("saldoActualSettings") {
        val accountAndOwnerWithTransactions by viewModel.accountAndOwnerWithTransactions.observeAsState(
            emptyList()
        )
        val principalPerson by viewModel.principalPerson.observeAsState()
        val coroutineScope = rememberCoroutineScope()

        var saving: Int by remember {
            mutableStateOf(0)
        }
        SaldoActualSettings(
            accountAndOwnerWithTransactions.filter { it.owner.id == principalPerson?.id },
            saving = saving
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaldoActualSettings(
    accountList: List<AccountAndOwnerWithTransactions>,
    saving: Int,
    onUpdateSeleccion: (account: Account, nuevoEstado: Boolean) -> Unit
) {
    val accountState = rememberTreeState()
    Column {
        TopAppBar(
            title = {
                MediumHeadline(text = "Saldo actual settings")
            }
        )
        Box(Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))) {
            LargeBody(text = "A continuación seleccione las cuentas incluídas en el cálculo del saldo actual")
        }
        if (saving > 0) {
            LinearProgressIndicator(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth()
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
        Box(Modifier.navigationBarsPadding()) {
            AccountPage(
                accountList = accountList,
                itemHolderPaddingValues = PaddingValues(4.dp),
                treeState = accountState,
                delAccount = null,
                editAccount = null,
                startDate = null,
                endDate = null,
                colorSelector = {
                    if (it.account.includedInTotal) {
                        CardDefaults.cardColors()
                    } else {
                        CardDefaults.elevatedCardColors()

                    }
                },
                detailAccount = { account ->
                    val id = account.id
                    if (id != null) {
                        onUpdateSeleccion(account, !account.includedInTotal)
                    }
                }
            ) {}
        }
    }
}