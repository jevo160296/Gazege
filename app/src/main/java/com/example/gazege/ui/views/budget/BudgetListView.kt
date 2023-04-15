package com.example.gazege.ui.views.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.GazegeProgressIndicator
import com.example.gazege.ui.widgets.RecyclerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetViewHolder(
    budget: BudgetAndCategoryWithCalculatedData
) {
    val overlineText =
        " ${doubleToMoneyString(budget.budgetValue)} each ${budget.budgetFrequency}, period ${budget.budgetFrequencyType}, type ${budget.budgetType}\n"
    val headlineText = "${stringResource(id = R.string.Presupuesto)}: ${budget.categoryName}"
    val supportingView = @Composable {
        Column(Modifier.fillMaxWidth()) {
            GazegeProgressIndicator(
                budget.budgetCompleition,
                stringResource(id = R.string.Progreso)
            )
            Text(text = "leftToPay ${doubleToMoneyString(budget.budgetLeftToPay)}")
            Text(text = "expectedTotalFlow ${doubleToMoneyString(budget.budgetExpectedTotalFlow)}")
            Text(text = "expectedRemainingFlow ${doubleToMoneyString(budget.budgetExpectedRemainingFlow)}")
            Text(text = "realTotalFlow ${doubleToMoneyString(budget.budgetRealTotalFlow)}")
            Text(text = "expectedFlowUntilNow ${doubleToMoneyString(budget.budgetExpectedFlowUntilNow)}")
        }
    }

    ListItem(
        overlineText = { Text(text = overlineText) },
        headlineText = { Text(text = headlineText) },
        supportingText = { supportingView() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun BudgetRecyclerView(
    modifier: Modifier,
    itemHolderPaddingValues: PaddingValues,
    budget: List<BudgetAndCategoryWithCalculatedData>,
    onBudgetDetailRequested: (budget: BudgetAndCategoryWithCalculatedData) -> Unit,
    onBudgetDeleteRequested: (budget: BudgetAndCategoryWithCalculatedData) -> Unit,
    onBudgetEditRequested: (budget: BudgetAndCategoryWithCalculatedData) -> Unit
) {
    val state = rememberLazyListState()
    var menuIdExpanded: Int? by remember {
        mutableStateOf(null)
    }
    RecyclerView(
        elements = budget,
        modifier = modifier,
        onItemTapped = onBudgetDetailRequested,
        onItemLongPressed = { menuIdExpanded = it.budgetId },
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state,
        viewHolder = {
            Box {
                BudgetViewHolder(
                    budget = it
                )
                DropdownMenu(
                    expanded = menuIdExpanded == it.budgetId,
                    onDismissRequest = { menuIdExpanded = null }) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.Editar)) },
                        onClick = {
                            menuIdExpanded = null
                            onBudgetEditRequested(it)
                        })
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.Eliminar)) },
                        onClick = {
                            menuIdExpanded = null
                            onBudgetDeleteRequested(it)
                        }
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun BudgetPreview() {
    DatabaseSample {
        GazegeTheme {
            Surface(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .systemBarsPadding()
            ) {
                Column {
                    Text("$currentDateSample")
                    Divider()
                    Text("$startDateSample")
                    Text("$endDateSample")
                    BudgetRecyclerView(
                        modifier = Modifier,
                        itemHolderPaddingValues = PaddingValues(),
                        budget = budgetAndCategoryWithCalculatedDataSample,
                        onBudgetDetailRequested = {},
                        onBudgetDeleteRequested = {},
                        onBudgetEditRequested = {}
                    )
                }
            }
        }
    }
}