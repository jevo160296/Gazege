package com.example.gazege.ui.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
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

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class
)
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
    categoriesFilter: BooleanFilters<Int, Pair<String, Int>>,
    onCategoriesFilterChanged: (newFilters: BooleanFilters<Int, Pair<String, Int>>) -> Unit,
    transactionFilters: BooleanFilters<String, Nothing>,
    onTransactionFiltersChanged: (newFilters: BooleanFilters<String, Nothing>) -> Unit
) {
    val transactionFilterCheckboxState by remember(transactionFilters) {
        mutableStateOf(
            if (transactionFilters.allFiltered()) {
                ToggleableState.Off
            } else if (transactionFilters.anyFiltered()) {
                ToggleableState.Indeterminate
            } else {
                ToggleableState.On
            }
        )
    }
    val categoryFilterCheckboxState by remember(categoriesFilter) {
        mutableStateOf(
            if (categoriesFilter.allFiltered()) {
                ToggleableState.Off
            } else if (categoriesFilter.anyFiltered()) {
                ToggleableState.Indeterminate
            } else {
                ToggleableState.On
            }
        )
    }
    var menuExpanded by remember { mutableStateOf(false) }
    val isFiltered =
        startDate != null ||
                endDate != null ||
                personFilterValue ||
                transactionFilters.anyFiltered() ||
                categoriesFilter.anyFiltered()
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
                label = { Text(text = stringResource(id = R.string.Filters)) },
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
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                            shape = MaterialTheme.shapes.small
                        )
                        .widthIn(min = 200.dp, max = 300.dp)
                        .heightIn(max = 400.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (dateFilterVisible) {
                            DateFilterItems(
                                Modifier.weight(1f, fill = true),
                                startDate = startDate,
                                endDate = endDate,
                                onRangeChanged = onRangeChanged
                            )
                        } else {
                            Spacer(modifier = Modifier)
                        }
                        IconButton(
                            onClick = {
                                onRangeChanged(null, null)
                                onPersonFilterValueChanged(false)
                                onTransactionFiltersChanged(transactionFilters.resetValues())
                                onCategoriesFilterChanged(categoriesFilter.resetValues())
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
                    if (transactionsFilterVisible || personFilterVisible) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                                )
                                .verticalScroll(rememberScrollState())
                                .padding(dimensionResource(id = R.dimen.DefaultPadding))
                        ) {
                            if (transactionsFilterVisible) {
                                Row(
                                    modifier = Modifier.align(Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TriStateCheckbox(
                                        state = transactionFilterCheckboxState,
                                        onClick = {
                                            onTransactionFiltersChanged(
                                                when (transactionFilterCheckboxState) {
                                                    ToggleableState.On -> transactionFilters.setValues(
                                                        transactionFilters.defaultValue.not()
                                                    )

                                                    ToggleableState.Off -> transactionFilters.resetValues()
                                                    ToggleableState.Indeterminate -> transactionFilters.resetValues()
                                                }
                                            )
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                                    )
                                    Text(stringResource(id = R.string.transacciones))
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(
                                        dimensionResource(
                                            id = R.dimen.DefaultPadding
                                        )
                                    )
                                ) {
                                    FilterChip(
                                        selected = transactionFilters[INCOME_FILTER],
                                        onClick = {
                                            onTransactionFiltersChanged(
                                                transactionFilters.switchOrDefault(
                                                    INCOME_FILTER
                                                )
                                            )
                                        },
                                        label = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ingreso_icon),
                                                contentDescription = "Filter income"
                                            )
                                        }
                                    )
                                    FilterChip(
                                        selected = transactionFilters[TRANSFER_FILTER],
                                        onClick = {
                                            onTransactionFiltersChanged(
                                                transactionFilters.switchOrDefault(
                                                    TRANSFER_FILTER
                                                )
                                            )
                                        },
                                        label = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.transfer_icon),
                                                contentDescription = "Filter transfer"
                                            )
                                        }
                                    )
                                    FilterChip(
                                        selected = transactionFilters[OUTCOME_FILTER],
                                        onClick = {
                                            onTransactionFiltersChanged(
                                                transactionFilters.switchOrDefault(
                                                    OUTCOME_FILTER
                                                )
                                            )
                                        },
                                        label = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.gasto_icon),
                                                contentDescription = "Filter gasto"
                                            )
                                        }
                                    )
                                }
                                Row(
                                    modifier = Modifier.align(Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TriStateCheckbox(
                                        state = categoryFilterCheckboxState,
                                        onClick = {
                                            onCategoriesFilterChanged(
                                                when (categoryFilterCheckboxState) {
                                                    ToggleableState.On -> categoriesFilter.setValues(
                                                        categoriesFilter.defaultValue.not()
                                                    )

                                                    ToggleableState.Off -> categoriesFilter.resetValues()
                                                    ToggleableState.Indeterminate -> categoriesFilter.resetValues()
                                                }
                                            )
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                    Text(stringResource(id = R.string.Categorias))
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(
                                        dimensionResource(
                                            id = R.dimen.DefaultPadding
                                        )
                                    )
                                ) {
                                    categoriesFilter.values.forEach { (filterId, value) ->
                                        FilterChip(
                                            selected = value,
                                            onClick = {
                                                onCategoriesFilterChanged(
                                                    categoriesFilter.switchOrDefault(
                                                        filterId
                                                    )
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = categoriesFilter.metadata[filterId]?.first
                                                        ?: "FilterId: $filterId"
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme
                                                    .colorScheme
                                                    .background
                                                    .copy(
                                                        alpha =
                                                        if ((categoriesFilter.metadata[filterId]?.second
                                                                ?: 0) > 0
                                                        ) {
                                                            0.6F
                                                        } else {
                                                            0.0F
                                                        }
                                                    )
                                                    .compositeOver(MaterialTheme.colorScheme.secondaryContainer)
                                            )
                                        )
                                    }
                                }
                            }
                            if (personFilterVisible) {
                                Text("Persons")
                                PersonFilter(
                                    personFilterValue,
                                    onValueChanged = onPersonFilterValueChanged
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateFilterItems(
    modifier: Modifier,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onRangeChanged: (newStartDate: LocalDate, newEndDate: LocalDate) -> Unit
) {
    val dateString = rangeToString(startDate, endDate)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
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

data class BooleanFilters<U, T>(
    val values: Map<U, Boolean>,
    val defaultValue: Boolean,
    val metadata: Map<U, T>
) {
    private fun isFiltered(value: Boolean) = value != defaultValue
    fun allFiltered() = values.values.all { isFiltered(it) }

    fun anyFiltered() = values.values.any { isFiltered(it) }

    fun switchOrDefault(valueName: U) =
        copy(values = this.values
            .toMutableMap()
            .also {
                it.merge(valueName, !defaultValue) { oldValue, _ -> !oldValue }
            }
            .toMap()
        )

    fun setValues(newValue: Boolean) = copy(
        values = values.mapValues { newValue }
    )

    fun resetValues() = setValues(defaultValue)

    fun update(items: List<U>) = copy(
        values = items.associateWith { this.values.getOrDefault(it, defaultValue) }
    )

    fun updateWithMetadata(items: List<Pair<U, T>>) = copy(
        values = items.associate { it.first to this.values.getOrDefault(it.first, defaultValue) },
        metadata = items.toMap()
    )

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other is BooleanFilters<*, *>) && (this.toString() == other.toString())
    }

    operator fun get(valueName: U): Boolean = values.getValue(valueName)
}

fun <U, T> booleanFilterOf(
    filterNames: List<U>,
    defaultValue: Boolean = false,
    metadata: Map<U, T> = emptyMap()
) = BooleanFilters(
    filterNames.associateWith { defaultValue },
    defaultValue,
    metadata
)

@Preview
@Composable
private fun FilterPreview() {
    var startDate: LocalDate? by remember { mutableStateOf(null) }
    var endDate: LocalDate? by remember { mutableStateOf(null) }
    var personFilterValue by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(booleanFilterOf<String, Nothing>(emptyList(), true)) }
    var categoriesFilter by remember {
        mutableStateOf(
            booleanFilterOf<Int, Pair<String, Int>>(
                (1..50).toList(),
                true
            )
        )
    }
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
                transactionFilters = filters,
                onTransactionFiltersChanged = { filters = it },
                categoriesFilter = categoriesFilter,
                onCategoriesFilterChanged = { categoriesFilter = it }
            )
        }
    }
}