package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.ui.views.account.AccountSelectionPage
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.treeview.rememberTreeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaldoActualSettings(
    accountList: List<AccountAndOwnerWithTransactions>,
    saving: Int,
    incluirPresupuestoEnSaldoActual: Boolean,
    incluirDeudasEnSaldoActual: Boolean,
    onIncluirPresupuestoEnSaldoActualChanged: (Boolean) -> Unit,
    onIncluirDeudasEnSaldoActualChanged: (Boolean) -> Unit,
    onUpdateSeleccion: (account: Account, nuevoEstado: Boolean) -> Unit
) {
    val accountState = rememberTreeState()
    Column {
        TopAppBar(
            title = {
                MediumHeadline(text = stringResource(R.string.Ajustes_saldo_actual))
            }
        )
        Box(Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))) {
            LargeBody(text = stringResource(R.string.Ajustes_saldo_actual_desc))
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
        Column(
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
            ) {
                Switch(
                    checked = incluirPresupuestoEnSaldoActual,
                    onCheckedChange = onIncluirPresupuestoEnSaldoActualChanged
                )
                Text(text = stringResource(id = R.string.Incluir_presupuesto))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
            ) {
                Switch(
                    checked = incluirDeudasEnSaldoActual,
                    onCheckedChange = onIncluirDeudasEnSaldoActualChanged
                )
                Text(text = stringResource(R.string.Incluir_deudas))
            }
        }
        AccountSelectionPage(
            modifier = Modifier.navigationBarsPadding(),
            accountList = accountList,
            itemHolderPaddingValues = PaddingValues(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
            treeState = accountState,
            onAccountStateChanged = { account, nuevoEstado ->
                onUpdateSeleccion(account, nuevoEstado)
            },
            startDate = null,
            endDate = null,
        )
    }
}