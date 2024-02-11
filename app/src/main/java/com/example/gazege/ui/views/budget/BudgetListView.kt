package com.example.gazege.ui.views.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.R
import com.example.gazege.core.entities.BudgetWithCalculatedDataAndCategory
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.templates.ClickableListItemViewHolder
import com.example.gazege.ui.templates.SimpleLazyList
import com.example.gazege.ui.theme.GazegeTheme

@Composable
private fun BudgetViewHolder(
    budget: BudgetWithCalculatedDataAndCategory
) {
    val overlineText =
        " ${doubleToMoneyString(budget.budgetValue)} each ${budget.budgetFrequency}, period ${budget.budgetFrequencyType}, type ${budget.budgetType}\n"
    val headlineText = "${stringResource(id = R.string.Presupuesto)}: ${budget.categoryName}"
    val leftToPayString = stringResource(id = R.string.Falta_pagar_recibir)
    val expectedFlowUntilNowString = stringResource(id = R.string.Flujo_estimado_hasta_hoy)
    val expectedTotalFlowString = stringResource(id = R.string.Flujo_total)
    val expectedRemainingFlow = stringResource(id = R.string.Flujo_estimado_desde_manana)
    val descripcionText = stringResource(id = R.string.descripcion)
    val supportingView = @Composable {
        Column(Modifier.fillMaxWidth()) {
            Text(text = "$leftToPayString ${doubleToMoneyString(budget.budgetLeftToPayFromToday)}")
            Text(text = "$expectedFlowUntilNowString ${doubleToMoneyString(budget.budgetExpectedFlowUntilNow)}")
            Text(text = "$expectedTotalFlowString ${doubleToMoneyString(budget.budgetExpectedTotalFlow)}")
            Text(text = "$expectedRemainingFlow ${doubleToMoneyString(budget.budgetExpectedRemainingFlow)}")
            Text(
                text = "$descripcionText ${budget.budgetDescription}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    ListItem(
        overlineContent = { Text(text = overlineText) },
        headlineContent = { Text(text = headlineText) },
        supportingContent = { supportingView() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun BudgetRecyclerView(
    modifier: Modifier,
    itemHolderPaddingValues: PaddingValues,
    budget: List<BudgetWithCalculatedDataAndCategory>,
    onBudgetDetailRequested: (budget: BudgetWithCalculatedDataAndCategory) -> Unit,
    onBudgetDeleteRequested: (budget: BudgetWithCalculatedDataAndCategory) -> Unit,
    onBudgetEditRequested: (budget: BudgetWithCalculatedDataAndCategory) -> Unit
) {
    val state = rememberLazyListState()
    var menuIdExpanded: Int? by remember {
        mutableStateOf(null)
    }
    SimpleLazyList(
        modifier = modifier,
        state = state,
        contentPadding = itemHolderPaddingValues,
        items = budget
    ) {
        ClickableListItemViewHolder(
            onItemTapped = { onBudgetDetailRequested(it) },
            onItemLongPressed = { menuIdExpanded = it.budgetId }
        ) {
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
    }
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
                    HorizontalDivider()
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