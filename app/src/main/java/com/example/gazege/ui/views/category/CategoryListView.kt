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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.templates.ClickableTreeListItemViewHolder
import com.example.gazege.ui.templates.SimpleTreeList
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.GazegeProgressIndicator
import com.example.gazege.ui.widgets.LargeEmphasis
import com.example.gazege.ui.widgets.treeview.rememberTreeState

@Composable
private fun CategoryAndBudgetViewHolder(
    categoryName: String,
    leftToPay: Double,
    expectedFlowUntilNow: Double,
    expectedTotalFlow: Double,
    realTotalFlow: Double,
    completion: Double
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
            Text(stringResource(id = R.string.Flujo_estimado_hasta_hoy))
            Text(stringResource(id = R.string.Flujo_total))
            Text(stringResource(id = R.string.Flujo_real))
        }
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        Column {
            Text(doubleToMoneyString(leftToPay))
            Text(doubleToMoneyString(expectedFlowUntilNow))
            Text(doubleToMoneyString(expectedTotalFlow))
            Text(doubleToMoneyString(realTotalFlow))
        }
    }
    GazegeProgressIndicator(
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
                .align(Alignment.End)
                .width(140.dp)
        ) {
            ButtonField(onClick = onSetBudgetRequested) {
                Text(stringResource(id = R.string.ConfigurarPresupuesto))
            }
        }
    }

@Composable
fun CategoryListView(
    categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    onItemClick: (category: Category) -> Unit,
    onSetBudgetRequested: (category: Category) -> Unit,
    onItemLongClick: (category: Category) -> Unit
) {
    val nodes = categoriesWithCalculatedData.map { CategoryWithBudgetNode(it) }
    SimpleTreeList(
        contentPadding = PaddingValues(
            bottom = dimensionResource(id = R.dimen.FABDefaultSpace),
            start = dimensionResource(id = R.dimen.DefaultPadding),
            end = dimensionResource(id = R.dimen.DefaultPadding)
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
            onItemTapped = { onItemClick(node.content.category.category) },
            onItemLongPressed = { onItemLongClick(node.content.category.category) },
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            val categoryWithCalculatedData = node.content
            val category = node.content.category
            val aggregatedBudget = node.content.aggregatedBudget
            val isExpanded = scope.isExpanded(node)
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

            val expectedFlowUntilNow = categoryWithCalculatedData.expectedFlowUntilNow +
                    if (isExpanded) {
                        0.0
                    } else {
                        categoryWithCalculatedData.childrenExpectedFlowUntilNow
                    }

            val expectedTotalFlow = categoryWithCalculatedData.expectedTotalFlow +
                    if (isExpanded) {
                        0.0
                    } else {
                        categoryWithCalculatedData.childrenExpectedTotalFlow
                    }

            if (aggregatedBudget != null || childrenBudget != null) {
                CategoryAndBudgetViewHolder(
                    categoryName = category.name,
                    leftToPay = leftToPay,
                    expectedFlowUntilNow = expectedFlowUntilNow,
                    expectedTotalFlow = expectedTotalFlow,
                    realTotalFlow = realFlow,
                    completion = completion
                )
            } else {
                EmptyCategoryAndBudgetViewHolder(
                    node.content.category.category,
                    onSetBudgetRequested = { onSetBudgetRequested(category.category) })
            }
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
                    onItemClick = {},
                    onItemLongClick = {},
                    onSetBudgetRequested = {}
                )
            }
        }
    }
}
