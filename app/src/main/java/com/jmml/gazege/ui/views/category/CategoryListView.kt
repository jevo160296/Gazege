package com.jmml.gazege.ui.views.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.BudgetWithCalculatedData
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.core.entities.plus
import com.jmml.gazege.plot.CategoryPlot
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.fragments.EditarCategoriasShowType
import com.jmml.gazege.ui.templates.ClickableTreeListItemViewHolder
import com.jmml.gazege.ui.templates.SimpleTreeList
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.ButtonField
import com.jmml.gazege.ui.widgets.GProgressIndicator
import com.jmml.gazege.ui.widgets.LargeEmphasis
import com.jmml.gazege.ui.widgets.treeview.NodeId
import com.jmml.gazege.ui.widgets.treeview.TreeScope
import com.jmml.gazege.ui.widgets.treeview.rememberTreeState
import java.time.LocalDate

@Composable
private fun CategoryAndBudgetViewHolder(
    categoryName: String,
    leftToPay: Double,
    expectedTotalFlow: Double,
    availableToday: Double,
    realTotalFlow: Double,
    completion: Double,
    pastForecast: Map<LocalDate, Double>,
    futureForecast: Map<LocalDate, Double>,
    dateRange: ClosedRange<LocalDate>,
    showType: EditarCategoriasShowType
) = Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.DefaultPadding))) {
    val dynamicVisibilityTemplate = @Composable { isVisible: Boolean ->
        @Composable { content: @Composable () -> Unit ->
            AnimatedVisibility(visible = isVisible) {
                content()
            }
        }
    }
    val expandedVisibility =
        dynamicVisibilityTemplate(showType >= EditarCategoriasShowType.EXPANDED)
    val graphicalVisibility =
        dynamicVisibilityTemplate(showType >= EditarCategoriasShowType.GRAPHICAL)

    LargeEmphasis(text = categoryName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = dimensionResource(id = R.dimen.DefaultPadding) * 3),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            expandedVisibility { Text(stringResource(id = R.string.Falta_pagar_recibir)) }
            expandedVisibility { Text(stringResource(id = R.string.Flujo_real)) }
            expandedVisibility { Text(stringResource(id = R.string.Flujo_total)) }
            Text(stringResource(id = R.string.Disponible_hoy))
        }
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        Column {
            expandedVisibility { Text(doubleToMoneyString(leftToPay)) }
            expandedVisibility { Text(doubleToMoneyString(realTotalFlow)) }
            expandedVisibility { Text(doubleToMoneyString(expectedTotalFlow)) }
            Text(doubleToMoneyString(availableToday))
        }
    }
    graphicalVisibility { Text(stringResource(id = R.string.Pronostico)) }
    graphicalVisibility {
        CategoryPlot(
            pastForecast = pastForecast,
            futureForecast = futureForecast,
            dateRange = dateRange
        )
    }
    GProgressIndicator(
        completion,
        stringResource(id = R.string.Progreso),
        color = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
private fun EmptyCategoryAndBudgetViewHolder(
    category: Category,
    onSetBudgetRequested: () -> Unit
) =
    Column(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.DefaultPadding))
            .fillMaxWidth()
    ) {
        LargeEmphasis(text = category.name)
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
        ) {
            ButtonField(onClick = onSetBudgetRequested) {
                Text(stringResource(id = R.string.ConfigurarPresupuesto))
            }
        }
    }

@Composable
fun CategoryListView(
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    editCategory: (category: Category) -> Unit,
    onSetBudgetRequested: (category: Category) -> Unit,
    delCategory: (category: Category) -> Unit,
    showType: EditarCategoriasShowType,
    exportCategory: (category: Category) -> Unit
) {
    val nodes = categoriesWithCalculatedData.map { CategoryWithBudgetNode(it) }
    var menuIdExpanded: NodeId? by remember {
        mutableStateOf(null)
    }
    SimpleTreeList(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
        itemSpacing = dimensionResource(id = R.dimen.DefaultPadding) * 2,
        nodes = nodes,
        state = rememberTreeState()
    ) { node, scope ->
        ClickableTreeListItemViewHolder(
            level = node.level,
            showExpandIcon = node.children.isNotEmpty(),
            isExpanded = scope.isExpanded(node),
            onIsExpandedChanged = { scope.toggleExpanded(node) },
            onItemTapped = { editCategory(node.content.category.category) },
            onItemLongPressed = { menuIdExpanded = node.id() },
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            scope.ClickableCategoriesViewHolder(
                onSetBudgetRequested = onSetBudgetRequested,
                editCategory = editCategory,
                delCategory = delCategory,
                exportCategory = exportCategory,
                menuIdExpanded = menuIdExpanded,
                onMenuIdExpandedChanged = { menuIdExpanded = it },
                node = node,
                showType = showType
            )
        }
    }
}

@Composable
fun TreeScope<CategoryWithSubcategoriesAndBudgetWithCalculatedData, CategoryWithBudgetNode>.ClickableCategoriesViewHolder(
    onSetBudgetRequested: (category: Category) -> Unit,
    editCategory: (category: Category) -> Unit,
    delCategory: (category: Category) -> Unit,
    exportCategory: (category: Category) -> Unit,
    menuIdExpanded: NodeId?,
    onMenuIdExpandedChanged: (NodeId?) -> Unit,
    showType: EditarCategoriasShowType,
    node: CategoryWithBudgetNode
) {
    val categoryWithCalculatedData = node.content
    val category = node.content.category
    val aggregatedBudget = node.content.aggregatedBudget
    val isExpanded = this.isExpanded(node)
    val childrenBudget =
        if (isExpanded) {
            null
        } else {
            node.content.childrenAggregatedBudget
        }
    val realFlow = categoryWithCalculatedData.realTotalFlow +
            if (isExpanded) {
                0.0
            } else {
                categoryWithCalculatedData.childrenRealTotalFlow
            }
    val completion =
        if (isExpanded) {
            categoryWithCalculatedData.completion
        } else {
            categoryWithCalculatedData.completionWithChildren
        }

    val leftToPay = categoryWithCalculatedData.leftToPay +
            if (isExpanded) {
                0.0
            } else {
                categoryWithCalculatedData.childrenLeftToPay
            }

    val availableToday = categoryWithCalculatedData.leftToPayToday

    val pastForecast = categoryWithCalculatedData.pastForecast +
            (categoryWithCalculatedData.childrenPastForecast.takeUnless { isExpanded }
                ?: emptyMap())

    val futureForecast = categoryWithCalculatedData.futureForecast +
            (categoryWithCalculatedData.childrenFutureForecast.takeUnless { isExpanded }
                ?: emptyMap())

    val dateRange = categoryWithCalculatedData.dateRange +
            (categoryWithCalculatedData.childrenDateRange.takeUnless { isExpanded })

    Box {
        if (dateRange != null &&
            (aggregatedBudget !is BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData ||
                    childrenBudget != null)
        ) {
            CategoryAndBudgetViewHolder(
                categoryName = category.name,
                leftToPay = leftToPay,
                expectedTotalFlow = realFlow + leftToPay,
                realTotalFlow = realFlow,
                availableToday = availableToday,
                completion = completion,
                pastForecast = pastForecast,
                futureForecast = futureForecast,
                dateRange = dateRange,
                showType = showType
            )
        } else {
            EmptyCategoryAndBudgetViewHolder(
                node.content.category.category,
                onSetBudgetRequested = { onSetBudgetRequested(category.category) })
        }
        DropdownMenu(
            expanded = menuIdExpanded == node.id(),
            onDismissRequest = { onMenuIdExpandedChanged(null) }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.Editar)) },
                onClick = {
                    onMenuIdExpandedChanged(null)
                    editCategory(node.content.category.category)
                })
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.Eliminar)) },
                onClick = {
                    onMenuIdExpandedChanged(null)
                    delCategory(node.content.category.category)
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.Exportar)) },
                onClick = {
                    onMenuIdExpandedChanged(null)
                    exportCategory(node.content.category.category)
                })
        }
    }
}

@Preview(widthDp = 600, heightDp = 720)
@Composable
private fun CategoryListPreview() {
    DatabaseSample {
        GazegeTheme {
            Box(Modifier.background(MaterialTheme.colorScheme.background)) {
                CategoryListView(
                    paddingValues = PaddingValues(),
                    categoriesWithCalculatedData = categoryWithSubcategoriesAndBudgetWithCalculatedDataSample,
                    editCategory = {},
                    delCategory = {},
                    onSetBudgetRequested = {},
                    exportCategory = {},
                    showType = EditarCategoriasShowType.EXPANDED,
                    nestedScrollConnection = object : NestedScrollConnection {}
                )
            }
        }
    }
}
