package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gazege.R
import com.example.gazege.core.entities.BudgetAndCategoryWithTransactions
import com.example.gazege.ui.views.budget.BudgetRecyclerView
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.fab.FAB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetFragment(
    budget: List<BudgetAndCategoryWithTransactions>,
    onNavigateToAddOneBudget: () -> Unit,
    onNavigateToOneBudgetDetail: (budgetId: Int) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FAB(onClick = onNavigateToAddOneBudget) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_add_24),
                    contentDescription = "Add"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        topBar = {
            TopAppBar(
                title = { MediumHeadline(text = stringResource(id = R.string.Presupuesto)) }
            )
        }
    ) { padding ->
        val layoutDirection = LocalLayoutDirection.current
        val itemHolderPaddingValues = PaddingValues(
            start = padding.calculateStartPadding(layoutDirection) + dimensionResource(id = R.dimen.DefaultPadding),
            end = padding.calculateEndPadding(layoutDirection) + dimensionResource(id = R.dimen.DefaultPadding),
            top = padding.calculateTopPadding(),
            bottom = dimensionResource(id = R.dimen.FABDefaultSpace)
        )
        BudgetRecyclerView(
            modifier = Modifier.systemBarsPadding(),
            itemHolderPaddingValues = itemHolderPaddingValues,
            budget = budget,
            onItemClick = { it.budgetId?.let { id -> onNavigateToOneBudgetDetail(id) } },
            onItemLongClick = {}
        )
    }
}