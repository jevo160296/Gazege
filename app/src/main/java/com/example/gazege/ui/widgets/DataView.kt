package com.example.gazege.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.doubleToString

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
    saldoActual: Double,
    ingresos: Double,
    egresos: Double,
    onSaldoActualClick: () -> Unit
) {
    val flujo = ingresos - egresos
    val enabledColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
    val disabledColors = CardDefaults.cardColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modifier = Modifier.weight(1f)
            DataView(
                title = "Saldo actual",
                bigTitle = true,
                value = doubleToString(saldoActual),
                modifier = modifier,
                colors = enabledColors,
                onClick = onSaldoActualClick
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modifier = Modifier.weight(1f)
            DataView(
                title = "Ingresos",
                value = doubleToString(ingresos),
                modifier = modifier,
                enabled = false,
                colors = disabledColors
            )
            DataView(
                title = "Egresos",
                value = doubleToString(egresos),
                modifier = modifier,
                enabled = false,
                colors = disabledColors
            )
            DataView(
                title = "Flujo",
                value = doubleToString(flujo),
                modifier = modifier,
                enabled = false,
                colors = disabledColors
            )
        }
    }
}
