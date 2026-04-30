package com.jmml.gazege.ui.widgets

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.zoo.ui.state.ZProgressIndicator

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
            ) {
                MediumHeadline(text = title)
                MediumHeadline(text = value)
            }
        }
    }
}

@Composable
fun LoadingDataView(
    modifier: Modifier = Modifier,
    title: String? = null,
    bigTitle: Boolean = false,
    value: String? = null,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(),
    isLoading: Boolean = true,
    contentPaddingValues: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition("Infinite transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1.0F,
        targetValue = 0.2F,
        animationSpec = infiniteRepeatable(
            animation = tween(
                1000, easing = CubicBezierEasing(
                    0.22F, 1.0F, 0.36F, 1.0F
                )
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha animation"
    )
    Card(
        modifier = modifier
            .height(42.dp)
            .apply {
                if (isLoading) {
                    alpha(alpha)
                }
            },
        colors = colors,
        enabled = enabled,
        onClick = onClick
    ) {
        if (!bigTitle) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPaddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(value ?: "")
                SmallEmphasis(title ?: "")
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPaddingValues),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediumHeadline(text = title ?: "")
                MediumHeadline(text = value ?: "")
            }
        }
    }
}

@Composable
fun DataViewWithTrailingComposable(
    modifier: Modifier = Modifier,
    title: String,
    bigTitle: Boolean = false,
    value: String,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(),
    trailingComposable: @Composable () -> Unit = @Composable {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = colors,
        enabled = enabled,
        onClick = onClick
    ) {
        if (!bigTitle) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(value)
                SmallEmphasis(text = title)
                trailingComposable()
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MediumHeadline(text = title)
                    MediumHeadline(text = value)
                }
                trailingComposable()
            }
        }
    }
}

@Composable
fun DataViewProgressBar(
    progress: Double
) {
    ZProgressIndicator(
        compleition = progress,
        color = MaterialTheme.colorScheme.tertiary,
        compact = true
    )
}

@Composable
fun LoadedPersonMonthSummaryView(
    modifier: Modifier,
    saldoActual: Double,
    disponibleHoy: Double,
    ingresos: Double,
    egresos: Double,
    flujo: Double,
    loading: Boolean,
    onSaldoActualClick: () -> Unit
) {
    val enabledColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary
    )
    val disabledColors = CardDefaults.cardColors()
    val animatedSaldoActual by animateFloatAsState(saldoActual.toFloat(), label = "")
    val animatedDisponibleHoy by animateFloatAsState(disponibleHoy.toFloat(), label = "")
    val animatedIngresos by animateFloatAsState(ingresos.toFloat(), label = "")
    val animatedEgresos by animateFloatAsState(egresos.toFloat(), label = "")
    val animatedFlujo by animateFloatAsState(flujo.toFloat(), label = "")
    var selectedItem by remember { mutableStateOf("saldo_actual") }
    Column(
        modifier = modifier
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
            HorizontalSlider(
                modifier = Modifier.fillMaxWidth(),
                items = mapOf(
                    "saldo_actual" to @Composable {
                        LoadingDataView(
                            title = stringResource(R.string.Saldo_actual),
                            bigTitle = true,
                            value = doubleToMoneyString(animatedSaldoActual.toDouble()),
                            modifier = Modifier.fillMaxWidth(),
                            contentPaddingValues = it,
                            colors = enabledColors,
                            isLoading = loading,
                            onClick = onSaldoActualClick
                        )
                    },
                    "disponible_hoy" to @Composable {
                        LoadingDataView(
                            title = stringResource(R.string.Disponible_hoy),
                            bigTitle = true,
                            value = doubleToMoneyString(animatedDisponibleHoy.toDouble()),
                            modifier = Modifier.fillMaxWidth(),
                            contentPaddingValues = it,
                            colors = enabledColors,
                            isLoading = loading,
                            onClick = onSaldoActualClick
                        )
                    }
                ),
                selectedItem = selectedItem,
                onItemClicked = { onSaldoActualClick() },
                onItemChanged = { selectedItem = it }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoadingDataView(
                title = stringResource(R.string.Ingresos),
                value = doubleToMoneyString(animatedIngresos.toDouble()),
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = disabledColors,
                isLoading = loading
            )
            LoadingDataView(
                title = stringResource(id = R.string.Gastos),
                value = doubleToMoneyString(animatedEgresos.toDouble()),
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = disabledColors,
                isLoading = loading
            )
            LoadingDataView(
                title = stringResource(R.string.Flujo),
                value = doubleToMoneyString(animatedFlujo.toDouble()),
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = disabledColors,
                isLoading = loading
            )
        }
    }
}

@Composable
fun EmptyPersonMonthSummaryView(
    modifier: Modifier,
    saldoActual: Double? = null,
    ingresos: Double? = null,
    egresos: Double? = null,
    flujo: Double? = null,
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
            LoadingDataView(
                title = saldoActual?.let { stringResource(R.string.Saldo_actual) },
                bigTitle = true,
                value = saldoActual?.let { doubleToMoneyString(saldoActual) },
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
            LoadingDataView(
                title = ingresos?.let { stringResource(R.string.Ingresos) },
                modifier = Modifier.weight(1f),
                value = ingresos?.let { doubleToMoneyString(ingresos) },
                enabled = false,
                colors = disabledColors
            )
            LoadingDataView(
                title = egresos?.let { stringResource(id = R.string.Gastos) },
                value = egresos?.let { doubleToMoneyString(egresos) },
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = disabledColors
            )
            LoadingDataView(
                title = flujo?.let { stringResource(R.string.Flujo) },
                value = flujo?.let { doubleToMoneyString(flujo) },
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = disabledColors
            )
        }
    }
}

@Preview
@Composable
fun DataViewWithProgressBarPreview() {
    DataViewWithTrailingComposable(
        title = "Titulo",
        value = "Valor",
        trailingComposable = {
            DataViewProgressBar(progress = 0.45)
        }
    )
}

@Preview
@Composable
fun LoadingDataPreview() {
    LoadingDataView()
}

@Preview(apiLevel = 34)
@Composable
fun SummaryPreview() {
    LoadedPersonMonthSummaryView(
        modifier = Modifier,
        saldoActual = 200.0,
        disponibleHoy = 250.0,
        ingresos = 100.0,
        egresos = 100.0,
        flujo = 0.0,
        loading = false,
        onSaldoActualClick = {}
    )
}