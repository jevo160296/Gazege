package com.example.gazege.ui.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.firstDayOfMonth
import com.example.gazege.core.lastDayOfMonth
import com.example.gazege.core.stableMinusMonths
import com.example.gazege.core.stablePlusMonths
import com.example.gazege.ui.DateFormat
import com.example.gazege.ui.localDateToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.menu.DropdownMenu
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
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
    filters: BooleanFilters,
    onFiltersChanged: (newFilters: BooleanFilters) -> Unit
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
    var menuExpanded by remember { mutableStateOf(false) }
    val isFiltered =
        startDate != null ||
                endDate != null ||
                personFilterValue ||
                filters.anyFiltered()
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AssistChip(
                onClick = { menuExpanded = !menuExpanded },
                label = { Text(text = "Filtros") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_arrow_drop_down_24),
                        contentDescription = "Filtros"
                    )
                }
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                Column(
                    Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(dimensionResource(id = R.dimen.DefaultPadding))
                        .widthIn(min = 200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (transactionsFilterVisible) {
                        Text("Transacciones")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
                        ) {
                            FilterChip(
                                selected = filters[INCOME_FILTER],
                                onClick = { onFiltersChanged(filters.switchOrDefault(INCOME_FILTER)) },
                                label = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ingreso_icon),
                                        contentDescription = "Filter income"
                                    )
                                }
                            )
                            FilterChip(
                                selected = filters[TRANSFER_FILTER],
                                onClick = { onFiltersChanged(filters.switchOrDefault(TRANSFER_FILTER)) },
                                label = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.transfer_icon),
                                        contentDescription = "Filter transfer"
                                    )
                                }
                            )
                            FilterChip(
                                selected = filters[OUTCOME_FILTER],
                                onClick = { onFiltersChanged(filters.switchOrDefault(OUTCOME_FILTER)) },
                                label = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.gasto_icon),
                                        contentDescription = "Filter gasto"
                                    )
                                }
                            )
                        }
                    }
                    if (personFilterVisible) {
                        Text("Persons")
                        PersonFilter(personFilterValue, onValueChanged = onPersonFilterValueChanged)
                    }
                }
            }
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
                onFiltersChanged(filters.resetValues())
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

const val INCOME_FILTER = "INCOME"
const val OUTCOME_FILTER = "OUTCOME"
const val TRANSFER_FILTER = "TRANSFER"

data class BooleanFilters(
    val values: Map<String, Boolean>,
    val defaultValue: Boolean
) {
    private fun isFiltered(value: Boolean) = value != defaultValue
    fun allFiltered() = values.values.all { isFiltered(it) }

    fun anyFiltered() = values.values.any { isFiltered(it) }

    fun switchOrDefault(valueName: String) =
        copy(values = this.values
            .toMutableMap()
            .also {
                it.merge(valueName, !defaultValue) { oldValue, _ -> !oldValue }
            }
            .toMap()
        )

    fun resetValues() = copy(values = emptyMap())

    operator fun get(valueName: String): Boolean = values.getOrDefault(valueName, defaultValue)
}

fun booleanFilterOf(defaultValue: Boolean = false) = BooleanFilters(
    mapOf(),
    defaultValue
)

@Preview
@Composable
private fun FilterPreview() {
    var startDate: LocalDate? by remember { mutableStateOf(null) }
    var endDate: LocalDate? by remember { mutableStateOf(null) }
    var personFilterValue by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(booleanFilterOf(true)) }
    GazegeTheme(darkTheme = true) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Filter(
                startDate = LocalDate.of(2022, 1, 1),
                endDate = LocalDate.of(2022, 1, 31),
                onRangeChanged = { newStart, newEnd ->
                    startDate = newStart
                    endDate = newEnd
                },
                personFilterVisible = true,
                personFilterValue = personFilterValue,
                onPersonFilterValueChanged = { personFilterValue = it },
                filters = filters,
                onFiltersChanged = { filters = it }
            )
        }
    }
}