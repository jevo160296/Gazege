package com.example.gazege.ui.views.category

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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.BudgetWithCalculatedData
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.example.gazege.core.entities.plus
import com.example.gazege.plot.CategoryPlot
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.templates.ClickableTreeListItemViewHolder
import com.example.gazege.ui.templates.SimpleTreeList
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.GProgressIndicator
import com.example.gazege.ui.widgets.LargeEmphasis
import com.example.gazege.ui.widgets.treeview.NodeId
import com.example.gazege.ui.widgets.treeview.TreeScope
import com.example.gazege.ui.widgets.treeview.rememberTreeState
import java.time.LocalDate

@Composable
private fun CategoryAndBudgetViewHolder(
    categoryName: String,
    leftToPay: Double,
    expectedTotalFlow: Double,
    realTotalFlow: Double,
    completion: Double,
    pastForecast: Map<LocalDate, Double>,
    futureForecast: Map<LocalDate, Double>,
    dateRange: ClosedRange<LocalDate>,
    showPlot: Boolean
) = Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.DefaultPadding))) {
    LargeEmphasis(text = categoryName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = dimensionResource(id = R.dimen.DefaultPadding) * 3),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(stringResource(id = R.string.Falta_pagar_recibir))
            Text(stringResource(id = R.string.Flujo_real))
            Text(stringResource(id = R.string.Flujo_total))

        }
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        Column {
            Text(doubleToMoneyString(leftToPay))
            Text(doubleToMoneyString(realTotalFlow))
            Text(doubleToMoneyString(expectedTotalFlow))

        }
    }
    if (showPlot) {
        Text(stringResource(id = R.string.Pronostico))
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
    categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    editCategory: (category: Category) -> Unit,
    onSetBudgetRequested: (category: Category) -> Unit,
    delCategory: (category: Category) -> Unit,
    showPlot: Boolean,
    exportCategory: (category: Category) -> Unit
) {
    val nodes = categoriesWithCalculatedData.map { CategoryWithBudgetNode(it) }
    var menuIdExpanded: NodeId? by remember {
        mutableStateOf(null)
    }
    SimpleTreeList(
        contentPadding = PaddingValues(
            bottom = dimensionResource(id = R.dimen.FABDefaultSpace)
        ),
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
                showPlot = showPlot
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
    showPlot: Boolean,
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
                completion = completion,
                pastForecast = pastForecast,
                futureForecast = futureForecast,
                dateRange = dateRange,
                showPlot = showPlot
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
                    categoriesWithCalculatedData = categoryWithSubcategoriesAndBudgetWithCalculatedDataSample,
                    editCategory = {},
                    delCategory = {},
                    onSetBudgetRequested = {},
                    exportCategory = {},
                    showPlot = true
                )
            }
        }
    }
}
