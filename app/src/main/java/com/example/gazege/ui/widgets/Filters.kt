package com.example.gazege.ui.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.R
import com.example.gazege.core.firstDayOfMonth
import com.example.gazege.core.lastDayOfMonth
import com.example.gazege.core.stableMinusMonths
import com.example.gazege.core.stablePlusMonths
import com.example.gazege.ui.DateFormat
import com.example.gazege.ui.localDateToString
import com.example.gazege.ui.theme.GazegeTheme
import java.time.LocalDate

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Filter(
    modifier: Modifier = Modifier,
    dateFilterVisible: Boolean = true,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onRangeChanged: (newStartDate: LocalDate?, newEndDate: LocalDate?) -> Unit,
    personFilterVisible: Boolean = false,
    personFilterValue: Boolean = false,
    onPersonFilterValueChanged: (newValue: Boolean) -> Unit = {},
    transactionsFilterVisible: Boolean = true,
    incomeFilterValue: Boolean = true,
    onIncomeFilterValueChanged: (newValue: Boolean) -> Unit = {},
    outcomeFilterValue: Boolean = true,
    onOutcomeFilterValueChanged: (newValue: Boolean) -> Unit = {},
    transferFilterValue: Boolean = true,
    onTransferFilterValueChanged: (newValue: Boolean) -> Unit = {}
) {
    val animatedVisibility: @Composable (visible: Boolean, content: @Composable () -> Unit) -> Unit =
        @Composable { visible, content ->
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn() + fadeIn() + expandIn(expandFrom = Alignment.Center),
                exit = scaleOut() + fadeOut() + shrinkOut(shrinkTowards = Alignment.Center),
                content = { content() }
            )
        }
    val isFiltered =
        startDate != null ||
                endDate != null ||
                personFilterValue ||
                !incomeFilterValue ||
                !outcomeFilterValue ||
                !transferFilterValue
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState(), reverseScrolling = true)
            .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        animatedVisibility(
            visible = transactionsFilterVisible
        ) {
            FilterChip(
                selected = incomeFilterValue,
                onClick = { onIncomeFilterValueChanged(!incomeFilterValue) },
                label = {
                    Icon(
                        painter = painterResource(id = R.drawable.ingreso_icon),
                        contentDescription = "Filter income"
                    )
                }
            )
        }
        animatedVisibility(transactionsFilterVisible) {
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        }
        animatedVisibility(
            visible = transactionsFilterVisible
        ) {
            FilterChip(
                selected = transferFilterValue,
                onClick = { onTransferFilterValueChanged(!transferFilterValue) },
                label = {
                    Icon(
                        painter = painterResource(id = R.drawable.transfer_icon),
                        contentDescription = "Filter transfer"
                    )
                }
            )
        }
        animatedVisibility(transactionsFilterVisible) {
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        }
        animatedVisibility(
            visible = transactionsFilterVisible
        ) {
            FilterChip(
                selected = outcomeFilterValue,
                onClick = { onOutcomeFilterValueChanged(!outcomeFilterValue) },
                label = {
                    Icon(
                        painter = painterResource(id = R.drawable.gasto_icon),
                        contentDescription = "Filter gasto"
                    )
                }
            )
        }
        animatedVisibility(transactionsFilterVisible) {
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        }
        animatedVisibility(
            visible = personFilterVisible
        ) {
            PersonFilter(personFilterValue, onValueChanged = onPersonFilterValueChanged)
        }
        animatedVisibility(personFilterVisible) {
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        }
        animatedVisibility(dateFilterVisible) {
            DateFilterItems(
                startDate = startDate,
                endDate = endDate,
                onRangeChanged = onRangeChanged
            )
        }
        animatedVisibility(dateFilterVisible) {
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        }
        IconButton(
            onClick = {
                onRangeChanged(null, null)
                onPersonFilterValueChanged(false)
                onIncomeFilterValueChanged(true)
                onOutcomeFilterValueChanged(true)
                onTransferFilterValueChanged(true)
            },
            enabled = isFiltered
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.round_filter_list_off_24
                ),
                contentDescription = "Clear filters"
            )
        }
    }
}

@Composable
fun DateFilterItems(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onRangeChanged: (newStartDate: LocalDate, newEndDate: LocalDate) -> Unit
) {
    val dateString = rangeToString(startDate, endDate)
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {
                if (startDate != null && endDate != null) {
                    val newRange = Pair(
                        stableMinusMonths(startDate, 1L),
                        stableMinusMonths(endDate, 1L)
                    )
                    onRangeChanged(newRange.first, newRange.second)
                }
            },
            enabled = startDate != null && endDate != null
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.round_arrow_left_24
                ),
                contentDescription = "Left"
            )
        }
        Text(
            dateString,
            modifier = Modifier.clickable {
                val newRange = LocalDate.now().let {
                    Pair(
                        firstDayOfMonth(it),
                        lastDayOfMonth(it)
                    )
                }
                onRangeChanged(newRange.first, newRange.second)
            },
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(
            onClick = {
                if (startDate != null && endDate != null) {
                    val newRange = Pair(
                        stablePlusMonths(startDate, 1L),
                        stablePlusMonths(endDate, 1L)
                    )
                    onRangeChanged(newRange.first, newRange.second)
                }
            },
            enabled = startDate != null && endDate != null
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.round_arrow_right_24
                ),
                contentDescription = "Right"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonFilter(
    value: Boolean,
    onValueChanged: (newValue: Boolean) -> Unit
) {
    FilterChip(
        selected = value,
        onClick = { onValueChanged(!value) },
        label = { Text(text = stringResource(id = R.string.Deuda_diferente_a_cero)) }
    )
}

@Composable
fun rangeToString(startDate: LocalDate?, endDate: LocalDate?) =
    if (startDate == null && endDate == null) stringResource(R.string.Todo)
    else if (startDate != null && endDate != null)
        localDateToString(startDate, DateFormat.YEARMONTHNAME)
    else "?"

@Preview
@Composable
private fun FilterPreview() {
    var startDate: LocalDate? by remember { mutableStateOf(null) }
    var endDate: LocalDate? by remember { mutableStateOf(null) }
    var personFilterValue by remember { mutableStateOf(false) }
    GazegeTheme(darkTheme = true) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            Filter(
                startDate = LocalDate.of(2022, 1, 1),
                endDate = LocalDate.of(2022, 1, 31),
                onRangeChanged = { newStart, newEnd ->
                    startDate = newStart
                    endDate = newEnd
                },
                personFilterVisible = true,
                personFilterValue = personFilterValue,
                onPersonFilterValueChanged = { personFilterValue = it }
            )
        }
    }
}