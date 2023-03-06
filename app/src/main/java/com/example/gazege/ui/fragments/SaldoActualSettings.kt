package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.ui.views.AccountPage
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.MediumHeadline

@Composable
fun SaldoActualSettings(
    accountList: List<AccountAndOwnerWithTransactions>,
    saving: Int,
    onUpdateSeleccion: (account: Account, nuevoEstado: Boolean) -> Unit
) {
    val accountState = rememberLazyListState()
    Column {
        MediumHeadline(text = "Saldo actual settings")
        LargeBody(text = "A continuación seleccione las cuentas incluídas en el cálculo del saldo actual")
        if (saving > 0) {
            LinearProgressIndicator(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth()
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
        AccountPage(
            accountList = accountList,
            itemHolderPaddingValues = PaddingValues(4.dp),
            state = accountState,
            delAccount = { },
            editAccount = { account ->
                val id = account.id
                if (id != null) {
                    onUpdateSeleccion(account, !account.includedInTotal)
                }
            },
            startDate = null,
            endDate = null,
            colorSelector = {
                if (it.account.includedInTotal) {
                    CardDefaults.cardColors()
                } else {
                    CardDefaults.elevatedCardColors()

                }
            }
        ) {}
    }
}