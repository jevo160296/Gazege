package com.example.gazege.ui.views.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.BudgetType
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.example.gazege.core.entities.FrequencyType
import com.example.gazege.core.entities.WeekDays
import com.example.gazege.core.entities.recursiveFirstOrNull
import com.example.gazege.core.entities.toList
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.category.CategoryDropDown
import com.example.gazege.ui.widgets.ComboBox
import com.example.gazege.ui.widgets.DatePicker
import com.example.gazege.ui.widgets.Form
import com.example.gazege.ui.widgets.GazegeSegmentedButton
import com.example.gazege.ui.widgets.NumberField
import com.example.gazege.ui.widgets.SegmentedButtonItem
import com.example.gazege.ui.widgets.TextField
import java.lang.Integer.max
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.withSign

@Composable
fun BudgetFormView(
    budget: Budget?,
    categories: List<Category>,
    budgetWithCalculatedDataAndCategory: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    onSaveBudget: (Budget) -> Unit
) {
    var isGasto by rememberSaveable(budget) {
        mutableStateOf(budget?.value?.let { it <= 0 } ?: true)
    }
    var selectedCategoryId by rememberSaveable(budget) { mutableStateOf(budget?.categoryId) }
    var frequencyType: FrequencyType by rememberSaveable(budget) {
        mutableStateOf(budget?.frequencyType ?: FrequencyType.MONTHLY)
    }
    var frequency by rememberSaveable(budget) { mutableIntStateOf(budget?.frequency ?: 1) }
    var startDate by rememberSaveable(budget) {
        mutableStateOf(
            budget?.startDate ?: LocalDate.now()
        )
    }
    var weekDaysDays: Set<DayOfWeek> by rememberSaveable(budget) {
        mutableStateOf((budget?.eachClass as? WeekDays)?.days ?: WeekDays.from(0b1111111).days)
    }
    var budgetType by rememberSaveable(budget) {
        mutableStateOf(
            budget?.budgetType ?: BudgetType.VARIABLE
        )
    }
    var value by rememberSaveable(budget) { mutableDoubleStateOf(abs(budget?.value ?: 0.0)) }
    var descripcion by rememberSaveable(budget) { mutableStateOf(budget?.description ?: "") }

    val selectedCategory =
        budgetWithCalculatedDataAndCategory.recursiveFirstOrNull { it.category.category.id == selectedCategoryId }
    val budgetId = budget?.id
    val weekDays = WeekDays(weekDaysDays)
    val sign = if (isGasto) {
        -1.0
    } else {
        1.0
    }

    val selectedCategoryIdVal = selectedCategoryId

    val newBudget = if (selectedCategoryIdVal != null) {
        when (frequencyType) {
            FrequencyType.DAILY -> Budget.fromDaily(
                id = budgetId,
                categoryId = selectedCategoryIdVal,
                value = value.withSign(sign),
                frequency = frequency,
                startDate = startDate,
                budgetType = budgetType,
                description = descripcion
            )
            FrequencyType.WEEKLY -> Budget.fromWeekly(
                id = budgetId,
                categoryId = selectedCategoryIdVal,
                value = value.withSign(sign),
                each = weekDays,
                frequency = frequency,
                startDate = startDate,
                budgetType = budgetType,
                description = descripcion
            )
            FrequencyType.MONTHLY -> Budget.fromMonthly(
                id = budgetId,
                categoryId = selectedCategoryIdVal,
                value = value.withSign(sign),
                budgetType = budgetType,
                description = descripcion
            )
        }
    } else {
        null
    }

    var frequencyDropDownExpanded by remember { mutableStateOf(false) }

    val frequencyMapper = object {
        val daily = stringResource(id = R.string.Dias)
        val weekly = stringResource(id = R.string.Semanas)
        val monthly = stringResource(id = R.string.Meses)
    }.let {
        { type: FrequencyType ->
            when (type) {
                FrequencyType.DAILY -> it.daily
                FrequencyType.WEEKLY -> it.weekly
                FrequencyType.MONTHLY -> it.monthly
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    Form(
        onSaveClicked = {
            if (newBudget != null) {
                onSaveBudget(newBudget)
            }
        },
        isSavedButtonEnabled = newBudget != null,
        title = stringResource(id = R.string.Presupuesto),
        itemSpacing = dimensionResource(id = R.dimen.DefaultPadding),
        itemsColumnsModifier = Modifier.padding(dimensionResource(id = R.dimen.DefaultPadding))
    ) {
        GazegeSegmentedButton(
            modifier = Modifier.fillMaxWidth(),
            selectedIndex = if (isGasto) {
                0
            } else {
                1
            },
            items = listOf(
                Pair(stringResource(R.string.Gasto), painterResource(R.drawable.gasto_icon)),
                Pair(stringResource(R.string.Ingreso), painterResource(R.drawable.ingreso_icon))
            )
                .map { (text, painter) -> SegmentedButtonItem.from(text, painter) },
            onItemClicked = { isGasto = it == 0 }
        )
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
        ) {
            Text(stringResource(R.string.Cada))
            TextField(
                modifier = Modifier
                    .width(70.dp)
                    .focusRequester(focusRequester),
                value = frequency.toString(),
                onValueChange = { newText ->
                    if (newText.isBlank()) {
                        frequency = 1
                    } else {
                        newText.toIntOrNull()?.let { frequency = max(1, it) }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            ComboBox(
                dropDownExpanded = frequencyDropDownExpanded,
                onExpandedChange = { frequencyDropDownExpanded = it },
                options = FrequencyType.values().toList(),
                selectedItem = frequencyType,
                itemToString = { item -> item?.let { frequencyMapper(it) } ?: "" },
                onItemClick = { frequencyType = it }
            )
        }
        WeekDaysPicker(frequencyType, weekDaysDays) { weekDaysDays = it }
        CategoryDropDown(
            categoryList = categories,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            selectedCategory = selectedCategory,
            label = { Text(stringResource(R.string.Categoria)) },
            onItemClick = { selectedCategoryId = it?.id }
        )
        NumberField(
            value = value,
            onValueChange = { value = it },
            label = { Text(stringResource(id = R.string.Valor)) }
        )
        BudgetTypeSelector(budgetType) { budgetType = it }
        TextField(value = descripcion, onValueChange = { descripcion = it }, label = {
            Text(
                stringResource(id = R.string.descripcion)
            )
        })
        if (frequencyType != FrequencyType.MONTHLY) {
            DatePicker(value = startDate, onValueChange = { startDate = it })
        }
    }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun budgetTypeMapper(budgetType: BudgetType) = when (budgetType) {
    BudgetType.FIXED -> stringResource(id = R.string.Fijo)
    BudgetType.VARIABLE -> stringResource(id = R.string.Variable)
}

@Composable
private fun BudgetTypeSelector(
    budgetType: BudgetType,
    onBudgetTypeChanged: (newType: BudgetType) -> Unit
) {
    Column {
        Text(text = stringResource(id = R.string.Tipo))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = budgetType == BudgetType.VARIABLE,
                onClick = { onBudgetTypeChanged(BudgetType.VARIABLE) })
            Text(budgetTypeMapper(BudgetType.VARIABLE))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = budgetType == BudgetType.FIXED,
                onClick = { onBudgetTypeChanged(BudgetType.FIXED) })
            Text(budgetTypeMapper(BudgetType.FIXED))
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WeekDaysPicker(
    frequencyType: FrequencyType,
    weekDaysDays: Set<DayOfWeek>,
    onWeekDaysDaysChanged: (Set<DayOfWeek>) -> Unit
) {
    when (frequencyType) {
        FrequencyType.DAILY -> {}
        FrequencyType.WEEKLY -> {
            Column {
                Text(stringResource(R.string.Dias_semana))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    (DayOfWeek.MONDAY..DayOfWeek.SUNDAY).toList().forEach { day ->
                        val selected = weekDaysDays.contains(day)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val newWeekDaysDays = if (selected) {
                                    weekDaysDays
                                        .toMutableList()
                                        .apply { removeIf { it == day } }
                                        .toSet()
                                } else {
                                    weekDaysDays
                                        .toMutableList()
                                        .apply { add(day) }
                                        .toSet()
                                }
                                onWeekDaysDaysChanged(newWeekDaysDays)
                            },
                            label = {
                                val text = when (day) {
                                    DayOfWeek.MONDAY -> stringResource(R.string.Lunes).firstOrNull()
                                    DayOfWeek.TUESDAY -> stringResource(R.string.Martes).firstOrNull()
                                    DayOfWeek.WEDNESDAY -> stringResource(R.string.Miercoles).firstOrNull()
                                    DayOfWeek.THURSDAY -> stringResource(R.string.Jueves).firstOrNull()
                                    DayOfWeek.FRIDAY -> stringResource(R.string.Viernes).firstOrNull()
                                    DayOfWeek.SATURDAY -> stringResource(R.string.Sabado).firstOrNull()
                                    DayOfWeek.SUNDAY -> stringResource(R.string.Domingo).firstOrNull()
                                }?.toString() ?: ""
                                Text(text)
                            }
                        )
                    }
                }
            }
        }
        FrequencyType.MONTHLY -> {}
    }
}

@Preview
@Composable
fun BudgetPreview2() {
    GazegeTheme {
        Box(Modifier.fillMaxSize()) {
            BudgetFormView(
                budget = null,
                categories = (1..10).map {
                    Category(it, "Cat$it", BudgetType.FIXED, null)
                },
                budgetWithCalculatedDataAndCategory = emptyList(),
                onSaveBudget = {}
            )
        }
    }
}