package com.jmml.gazege.ui.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.extensions.localdate.endOfMonth
import com.jmml.gazege.extensions.localdate.stableMinusMonths
import com.jmml.gazege.extensions.localdate.stablePlusMonths
import com.jmml.gazege.extensions.localdate.startOfMonth
import com.jmml.gazege.ui.DateFormat
import com.jmml.gazege.ui.localDateToString
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.menu.DropdownMenu
import com.jmml.gazege.ui.widgets.sliders.GRangeSlider
import java.time.LocalDate
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.min

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
    categoriesFilter: BooleanFilters<Int?, Pair<String, Int>>,
    onCategoriesFilterChanged: (newFilters: BooleanFilters<Int?, Pair<String, Int>>) -> Unit,
    transactionFilters: BooleanFilters<String, Nothing>,
    onTransactionFiltersChanged: (newFilters: BooleanFilters<String, Nothing>) -> Unit,
    valueFilterState: DoubleFilter,
    onValueFilterStateChanged: (DoubleFilter) -> Unit,
    descriptionFilterState: TextFilter,
    onDescriptionFilterStateChanged: (TextFilter) -> Unit
) {
    val (valueFilterUI, onValueFilterUIChanged) = remember(valueFilterState) {
        mutableStateOf(
            valueFilterState.copy()
        )
    }
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
    var searchBarActive by remember { mutableStateOf(false) }
    val searchBarTonalElevation by animateDpAsState(
        targetValue = if (searchBarActive) {
            2.dp
        } else {
            8.dp
        },
        label = "Tonal elevation"
    )
    val isFiltered =
        startDate != null ||
                endDate != null ||
                personFilterValue ||
                transactionFilters.anyFiltered() ||
                categoriesFilter.anyFiltered() ||
                valueFilterState.anyFiltered() ||
                descriptionFilterState.anyFiltered()
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
        horizontalArrangement = Arrangement
            .spacedBy(dimensionResource(id = R.dimen.DefaultPadding), Alignment.End),
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
                    if (transactionsFilterVisible) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
                                .padding(bottom = dimensionResource(id = R.dimen.DefaultPadding)),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SearchBar(
                                query = descriptionFilterState.value ?: "",
                                onQueryChange = {
                                    onDescriptionFilterStateChanged(
                                        descriptionFilterState.copy(
                                            value = it.substring(
                                                0,
                                                min(30, it.length)
                                            )
                                        )
                                    )
                                },
                                onSearch = { searchBarActive = false },
                                active = false,
                                onActiveChange = { searchBarActive = it },
                                placeholder = { Text(text = stringResource(id = R.string.descripcion)) },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.round_search_24),
                                        contentDescription = "Search"
                                    )
                                },
                                tonalElevation = searchBarTonalElevation
                            ) {}
                        }
                    }
                    if (transactionsFilterVisible || personFilterVisible || dateFilterVisible) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                                )
                                .verticalScroll(rememberScrollState())
                                .padding(dimensionResource(id = R.dimen.DefaultPadding))
                        ) {
                            if (dateFilterVisible) {
                                Box(
                                    Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        enabled = startDate != null || endDate != null,
                                        onClick = { onRangeChanged(null, null) },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.round_restart_alt_24),
                                            contentDescription = "Restart"
                                        )
                                    }
                                    DateFilterItems(
                                        modifier = Modifier.align(Alignment.Center),
                                        startDate = startDate,
                                        endDate = endDate,
                                        onRangeChanged = onRangeChanged
                                    )
                                }
                            }
                            if (transactionsFilterVisible) {
                                Row(
                                    modifier = Modifier.align(Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GTriStateCheckbox(
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
                                        }
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
                                    IconButton(
                                        enabled = valueFilterState.anyFiltered(),
                                        onClick = {
                                            onValueFilterStateChanged(valueFilterState.resetValues())
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.round_restart_alt_24),
                                            contentDescription = "Restart"
                                        )
                                    }
                                    Text(stringResource(id = R.string.Valor))
                                }
                                GRangeSlider(
                                    value = valueFilterUI.value ?: valueFilterUI.range,
                                    onValueChange = {
                                        onValueFilterUIChanged(
                                            valueFilterUI.copy(
                                                value = it.start..it.endInclusive
                                            )
                                        )
                                    },
                                    valueRange = valueFilterUI.range,
                                    onValueChangeFinished = {
                                        onValueFilterStateChanged(
                                            valueFilterUI
                                        )
                                    }
                                )
                                Row(
                                    modifier = Modifier.align(Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GTriStateCheckbox(
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
                                        }
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
                                                    text = (categoriesFilter.metadata[filterId]?.first
                                                        ?: "FilterId: $filterId").ifEmpty {
                                                        stringResource(
                                                            id = R.string.No_category
                                                        )
                                                    }
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
                                Text(stringResource(id = R.string.personas))
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
        IconButton(
            onClick = {
                onRangeChanged(null, null)
                onPersonFilterValueChanged(false)
                onTransactionFiltersChanged(transactionFilters.resetValues())
                onCategoriesFilterChanged(categoriesFilter.resetValues())
                onValueFilterStateChanged(valueFilterState.resetValues())
                onDescriptionFilterStateChanged(descriptionFilterState.resetValues())
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
                        startDate.stableMinusMonths(1L),
                        endDate.stableMinusMonths(1L)
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
                    Pair(it.startOfMonth(), it.endOfMonth())
                }
                onRangeChanged(newRange.first, newRange.second)
            },
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(
            onClick = {
                if (startDate != null && endDate != null) {
                    val newRange = Pair(
                        startDate.stablePlusMonths(1L),
                        endDate.stablePlusMonths(1L)
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

interface Filter<T : Filter<T>> {
    fun allFiltered(): Boolean
    fun anyFiltered(): Boolean
    fun resetValues(): T
}

data class BooleanFilters<U, T>(
    val values: Map<U, Boolean>,
    val defaultValue: Boolean,
    val metadata: Map<U, T>
) : Filter<BooleanFilters<U, T>> {
    private fun isFiltered(value: Boolean) = value != defaultValue
    override fun allFiltered() = values.values.all { isFiltered(it) }

    override fun anyFiltered() = values.values.any { isFiltered(it) }

    fun switchOrDefault(valueName: U) =
        copy(
            values = this.values
                .toMutableMap()
                .also {
                    it.merge(valueName, !defaultValue) { oldValue, _ -> !oldValue }
            }
            .toMap()
        )

    fun setValue(valueName: U, value: Boolean) =
        copy(values = this.values
            .toMutableMap()
            .also {
                it[valueName] = value
            }
            .toMap()
        )

    fun setValues(newValue: Boolean) = copy(
        values = values.mapValues { newValue }
    )


    override fun resetValues() = setValues(defaultValue)

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

data class DoubleFilter(
    val value: ClosedFloatingPointRange<Float>?,
    val range: ClosedFloatingPointRange<Float>
) : Filter<DoubleFilter> {
    override fun allFiltered(): Boolean = value?.let {
        it.endInclusive < range.start ||
                it.start > range.endInclusive
    } ?: false

    override fun anyFiltered(): Boolean = value?.let {
        it.endInclusive >= range.start ||
                it.start <= range.endInclusive
    } ?: false

    override fun resetValues(): DoubleFilter = DoubleFilter(
        value = null,
        range = range
    )

}

data class TextFilter(
    val value: String?
) : Filter<TextFilter> {
    override fun allFiltered(): Boolean = false

    override fun anyFiltered(): Boolean = value != null

    override fun resetValues(): TextFilter = copy(value = null)
}

@Preview
@Composable
private fun FilterPreview() {
    var startDate: LocalDate? by remember { mutableStateOf(null) }
    var endDate: LocalDate? by remember { mutableStateOf(null) }
    var personFilterValue by remember { mutableStateOf(false) }
    var descriptionFilter by remember { mutableStateOf(TextFilter(null)) }
    var filters by remember {
        mutableStateOf(
            booleanFilterOf<String, Nothing>(
                listOf(
                    INCOME_FILTER,
                    OUTCOME_FILTER,
                    TRANSFER_FILTER
                ), true
            )
        )
    }
    var categoriesFilter by remember {
        mutableStateOf(
            booleanFilterOf<Int?, Pair<String, Int>>(
                (1..50).toList(),
                true
            )
        )
    }
    var valueFilterState by remember { mutableStateOf(DoubleFilter(0f..100f, 0f..100f)) }
    GazegeTheme {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
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
                onCategoriesFilterChanged = { categoriesFilter = it },
                valueFilterState = valueFilterState,
                onValueFilterStateChanged = { valueFilterState = it },
                descriptionFilterState = descriptionFilter,
                onDescriptionFilterStateChanged = { descriptionFilter = it }
            )
            Text("UI: ${valueFilterState.value}")
        }
    }
}