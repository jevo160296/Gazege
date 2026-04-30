package com.jmml.gazege.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.ui.DateFormat
import com.jmml.gazege.ui.localDateToString
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.sliders.GRangeSlider
import com.jmml.zoo.extensions.localdate.endOfMonth
import com.jmml.zoo.extensions.localdate.stableMinusMonths
import com.jmml.zoo.extensions.localdate.stablePlusMonths
import com.jmml.zoo.extensions.localdate.startOfMonth
import com.jmml.zoo.ui.menu.DropdownMenu
import java.time.LocalDate
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.max
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
        AnimatedVisibility(dateFilterVisible) {
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
        }
        AnimatedVisibility(dateFilterVisible) {
            DateFilterItems(
                startDate = startDate,
                endDate = endDate,
                onRangeChanged = onRangeChanged
            )
        }
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
                                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                                )
                                .verticalScroll(rememberScrollState())
                                .padding(dimensionResource(id = R.dimen.DefaultPadding))
                        ) {
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
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        space = dimensionResource(
                                            id = R.dimen.DefaultPadding
                                        ),
                                        alignment = Alignment.CenterHorizontally
                                    )
                                ) {
                                    GFilterChip(
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
                                    GFilterChip(
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
                                    GFilterChip(
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
                                    GFilterChip(
                                        selected = transactionFilters[PROMISSORY_NOTE_FILTER],
                                        onClick = {
                                            onTransactionFiltersChanged(
                                                transactionFilters.switchOrDefault(
                                                    PROMISSORY_NOTE_FILTER
                                                )
                                            )
                                        },
                                        label = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.promissory_note),
                                                contentDescription = "Filter promissory notes"
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
                                        val level = categoriesFilter.metadata[filterId]?.second
                                        GFilterChip(
                                            label = {
                                                Text(
                                                    text = (categoriesFilter.metadata[filterId]?.first
                                                        ?: "FilterId: $filterId").ifEmpty {
                                                        stringResource(
                                                            id = R.string.No_category
                                                        )
                                                    },
                                                    fontWeight = if ((level
                                                            ?: 0) > 0
                                                    ) FontWeight.Normal else FontWeight.Bold
                                                )
                                            },
                                            onClick = {
                                                onCategoriesFilterChanged(
                                                    categoriesFilter.switchOrDefault(
                                                        filterId
                                                    )
                                                )
                                            },
                                            selected = value,
                                            level = if ((level
                                                    ?: 0) > 0
                                            ) GFilterChipLevel.Secondary else GFilterChipLevel.Primary
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

@Stable
internal fun IconButtonColors.containerColor(enabled: Boolean): Color =
    if (enabled) containerColor else disabledContainerColor

@Stable
internal fun IconButtonColors.contentColor(enabled: Boolean): Color =
    if (enabled) contentColor else disabledContentColor

@Composable
private fun GIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(40.dp)
            .clip(MaterialTheme.shapes.small)
            .background(color = colors.containerColor(enabled)),
        contentAlignment = Alignment.Center
    ) {
        val contentColor = colors.contentColor(enabled)
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

@Composable
fun DateFilterItems(
    modifier: Modifier = Modifier,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onRangeChanged: (newStartDate: LocalDate, newEndDate: LocalDate) -> Unit
) {
    var backSize by rememberSaveable { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    val dateString = rangeToString(startDate, endDate)
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple()
    val onClickCenter = {
        val newRange = LocalDate
            .now()
            .let {
                Pair(it.startOfMonth(), it.endOfMonth())
            }
        onRangeChanged(newRange.first, newRange.second)
    }
    val onClickBack = {
        if (startDate != null && endDate != null) {
            val newRange = Pair(
                startDate.stableMinusMonths(1L),
                endDate.stableMinusMonths(1L)
            )
            onRangeChanged(newRange.first, newRange.second)
        } else {
            onClickCenter()
        }
    }
    val onClickNext = {
        if (startDate != null && endDate != null) {
            val newRange = Pair(
                startDate.stablePlusMonths(1L),
                endDate.stablePlusMonths(1L)
            )
            onRangeChanged(newRange.first, newRange.second)
        } else {
            onClickCenter()
        }
    }
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.extraLarge)
            .indication(interactionSource, indication)
            .hoverable(interactionSource)
            .pointerInput(startDate, endDate) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount >= 10.0) onClickBack()
                    if (dragAmount <= -10.0) onClickNext()
                }
            }
            .pointerInput(startDate, endDate) {
                val width = size.width.toDp()
                val start = backSize.dp
                val end = width - start
                detectTapGestures(
                    onPress = {
                        val pressInteraction = PressInteraction.Press(it)
                        interactionSource.emit(pressInteraction)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(pressInteraction))
                    }
                ) { offset ->
                    val x = offset.x.toDp()
                    when {
                        x <= start -> onClickBack()
                        x in start..end -> onClickCenter()
                        x >= end -> onClickNext()
                    }
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        GIcon(
            modifier = Modifier.onSizeChanged {
                backSize = with(density) { it.width.toDp().value }
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
        Box(
            Modifier
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.animateContentSize(),
                text = dateString,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        GIcon(enabled = startDate != null && endDate != null) {
            Icon(
                painter = painterResource(
                    id = R.drawable.round_arrow_right_24
                ),
                contentDescription = "Right"
            )
        }
    }
}

@Composable
fun HorizontalSlider(
    modifier: Modifier = Modifier,
    items: Map<String, @Composable (paddingValues: PaddingValues) -> Unit>,
    selectedItem: String,
    onItemClicked: (itemClicked: String) -> Unit,
    onItemChanged: (newItemSelected: String) -> Unit
) {
    var backSize by rememberSaveable { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple()
    val onClickCenter = {
        onItemClicked(selectedItem)
    }
    val onClickBack = {
        if (selectedItem != items.keys.first()) {
            val newSelectedItemIndex = items.keys.indexOf(selectedItem) - 1
            val newSelectedItem = items.keys.elementAt(max(newSelectedItemIndex, 0))
            onItemChanged(newSelectedItem)
        }
    }
    val onClickNext = {
        if (selectedItem != items.keys.last()) {
            val newSelectedItemIndex = items.keys.indexOf(selectedItem) + 1
            val newSelectedItem =
                items.keys.elementAt(min(newSelectedItemIndex, items.keys.size - 1))
            onItemChanged(newSelectedItem)
        }
    }
    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .width(IntrinsicSize.Max)
            .indication(interactionSource, indication)
            .hoverable(interactionSource)
            .pointerInput(selectedItem) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount >= 10.0) onClickBack()
                    if (dragAmount <= -10.0) onClickNext()
                }
            }
            .pointerInput(selectedItem) {
                val width = size.width.toDp()
                val start = backSize.dp
                val end = width - start
                detectTapGestures(
                    onPress = {
                        val pressInteraction = PressInteraction.Press(it)
                        interactionSource.emit(pressInteraction)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(pressInteraction))
                    }
                ) { offset ->
                    val x = offset.x.toDp()
                    when {
                        x <= start -> onClickBack()
                        x in start..end -> onClickCenter()
                        x >= end -> onClickNext()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        items[selectedItem]?.invoke(
            PaddingValues(horizontal = with(density) { (2 * backSize).toDp() })
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GIcon(
                modifier = Modifier.onSizeChanged {
                    backSize = with(density) { it.width.toDp().value }
                },
                enabled = selectedItem != items.keys.first()
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.round_arrow_left_24
                    ),
                    contentDescription = "Left"
                )
            }
            GIcon(enabled = selectedItem != items.keys.last()) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.round_arrow_right_24
                    ),
                    contentDescription = "Right"
                )
            }
        }
    }
}

@Composable
fun PersonFilter(
    value: Boolean,
    onValueChanged: (newValue: Boolean) -> Unit
) {
    GFilterChip(
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
const val PROMISSORY_NOTE_FILTER = "PROMISSORYNOTE"

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

    operator fun get(valueName: U): Boolean = values.getOrDefault(valueName, defaultValue)
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

@Preview(apiLevel = 33)
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
                Modifier.fillMaxWidth(),
                startDate = startDate,
                endDate = endDate,
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

@Preview()
@Composable
private fun HorizontalSliderPreview() {
    GazegeTheme {
        var selectedItem: String by remember { mutableStateOf("first") }
        var lastClickedItem: String by remember { mutableStateOf("") }
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)) {
            Text(selectedItem)
            Text(lastClickedItem)
            HorizontalSlider(
                items = mapOf(
                    "first" to @Composable {
                        Box(
                            Modifier
                                .padding(it)
                                .background(MaterialTheme.colorScheme.primary))
                        {
                            Text(
                                "Firstiary text",
                                Modifier.background(MaterialTheme.colorScheme.primary),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    "second" to @Composable {
                        Box(
                            Modifier
                                .padding(it)
                                .background(MaterialTheme.colorScheme.primary))
                        {
                            Text(
                                "Secondary text",
                                Modifier.background(MaterialTheme.colorScheme.primary),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    "third" to @Composable {
                        Box(
                            Modifier
                                .padding(it)
                                .background(MaterialTheme.colorScheme.primary))
                        {
                            Text(
                                "Third",
                                Modifier.background(MaterialTheme.colorScheme.primary),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                ),
                selectedItem = selectedItem,
                onItemClicked = { lastClickedItem = it },
                onItemChanged = { selectedItem = it }
            )
        }
    }
}