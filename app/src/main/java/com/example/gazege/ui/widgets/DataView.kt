package com.example.gazege.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.ui.doubleToMoneyString

@Composable
fun DataView(
    modifier: Modifier = Modifier,
    title: String,
    bigTitle: Boolean = false,
    value: String,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(),
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(42.dp),
        colors = colors,
        enabled = enabled,
        onClick = onClick
    ) {
        if(!bigTitle){
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(value)
                SmallEmphasis(text = title)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ){
                MediumHeadline(text = title)
                MediumHeadline(text = value)
            }
        }
    }
}

@Composable
fun PersonMonthSummaryView(
    modifier: Modifier,
    saldoActual: Double,
    ingresos: Double,
    egresos: Double,
    flujo: Double,
    onSaldoActualClick: () -> Unit
) {
    val enabledColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary
    )
    val disabledColors = CardDefaults.cardColors()
    Column(
        modifier = modifier
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DataView(
                title = stringResource(R.string.Saldo_actual),
                bigTitle = true,
                value = doubleToMoneyString(saldoActual),
                modifier = Modifier.weight(1f),
                colors = enabledColors,
                onClick = onSaldoActualClick
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DataView(
                title = stringResource(R.string.Ingresos),
                value = doubleToMoneyString(ingresos),
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = disabledColors
            )
            DataView(
                title = stringResource(id = R.string.Gastos),
                value = doubleToMoneyString(egresos),
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = disabledColors
            )
            DataView(
                title = stringResource(R.string.Flujo),
                value = doubleToMoneyString(flujo),
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = disabledColors
            )
        }
    }
}
