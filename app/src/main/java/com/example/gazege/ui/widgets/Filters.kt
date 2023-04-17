package com.example.gazege.ui.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Filter(
    modifier: Modifier = Modifier,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onRangeChanged: (newStartDate: LocalDate?, newEndDate: LocalDate?) -> Unit,
    personFilterVisible: Boolean = false,
    personFilterValue: Boolean = false,
    onPersonFilterValueChanged: (newValue: Boolean) -> Unit = {}
) {
    val isFiltered = startDate != null || endDate != null || personFilterValue
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = personFilterVisible,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            PersonFilter(personFilterValue, onValueChanged = onPersonFilterValueChanged)
        }
        DateFilterItems(startDate = startDate, endDate = endDate, onRangeChanged = onRangeChanged)
        IconButton(
            onClick = {
                onRangeChanged(null, null)
                onPersonFilterValueChanged(false)
            },
            enabled = isFiltered
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.round_filter_list_off_24
                ),
                contentDescription = "Clear filters",
                tint = MaterialTheme.colorScheme.onBackground
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
            contentDescription = "Left",
            tint = MaterialTheme.colorScheme.onBackground
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
            contentDescription = "Right",
            tint = MaterialTheme.colorScheme.onBackground
        )
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