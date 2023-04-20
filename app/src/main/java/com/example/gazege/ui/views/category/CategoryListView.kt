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
import com.example.gazege.core.entities.CategoryWithBudgetData
import com.example.gazege.core.entities.CategoryWithSubCategories
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
    category: CategoryWithBudgetData
) = Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.DefaultPadding))) {
    LargeEmphasis(text = category.category.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = dimensionResource(id = R.dimen.DefaultPadding) * 3),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(stringResource(id = R.string.flujo_hasta_hoy))
            Text(stringResource(id = R.string.Flujo_total))
            Text(stringResource(id = R.string.Flujo_real))
        }
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        Column {
            Text(doubleToMoneyString(category.budgetExpectedFlowUntilNow))
            Text(doubleToMoneyString(category.budgetExpectedTotalFlow))
            Text(doubleToMoneyString(category.budgetRealTotalFlow))
        }
    }
    GazegeProgressIndicator(
        category.completion,
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
    categories: List<CategoryWithSubCategories>,
    categoriesWithCalculatedData: Map<Category, CategoryWithBudgetData?>,
    onItemClick: (category: CategoryWithSubCategories) -> Unit,
    onSetBudgetRequested: (category: CategoryWithSubCategories) -> Unit,
    onItemLongClick: (category: CategoryWithSubCategories) -> Unit
) {
    val nodes = categories.map { CategoryNode(it) }
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
            onItemTapped = { onItemClick(node.content) },
            onItemLongPressed = { onItemLongClick(node.content) },
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            val categoryWithCalculatedData = categoriesWithCalculatedData[node.content.category]
            if (categoryWithCalculatedData != null) {
                CategoryAndBudgetViewHolder(category = categoryWithCalculatedData)
            } else {
                EmptyCategoryAndBudgetViewHolder(
                    node.content.category,
                    onSetBudgetRequested = { onSetBudgetRequested(node.content) })
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
                    categories = categoryWithSubcategoriesSample,
                    categoriesWithCalculatedData = categoryWithCalculatedData,
                    onItemClick = {},
                    onItemLongClick = {},
                    onSetBudgetRequested = {}
                )
            }
        }
    }
}
