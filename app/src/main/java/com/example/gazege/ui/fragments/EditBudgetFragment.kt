package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gazege.R
import com.example.gazege.core.entities.BudgetWithCalculatedDataAndCategory
import com.example.gazege.ui.views.budget.BudgetRecyclerView
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.ModalSheetLayout
import com.example.gazege.ui.widgets.fab.FAB
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetFragment(
    budget: List<BudgetWithCalculatedDataAndCategory>,
    onAddOneBudgetRequested: () -> Unit,
    onGetBudgetDetailRequested: (budgetId: Int) -> Unit,
    onEditBudgetRequested: (budgetId: Int) -> Unit,
    onDeleteBudgetRequested: (budgetId: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val defaultAction: () -> Unit = { scope.launch { sheetState.hide() } }
    var action: (() -> Unit)? by remember { mutableStateOf(null) }
    ModalSheetLayout(
        modalSheetMsg = stringResource(id = R.string.confirma_la_eliminacion_de).format(
            stringResource(id = R.string.Presupuesto)
        ),
        onModalSheetMsgChanged = {},
        action = action ?: defaultAction,
        onActionChanged = { action = it },
        sheetState = sheetState
    ) {
        Scaffold(
            floatingActionButton = {
                FAB(onClick = onAddOneBudgetRequested) {
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
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                onBudgetDetailRequested = { it.budgetId?.let { id -> onGetBudgetDetailRequested(id) } },
                onBudgetDeleteRequested = {
                    it.budgetId?.let { id ->
                        action = {
                            onDeleteBudgetRequested(id)
                            scope.launch { sheetState.hide() }
                        }
                        scope.launch { sheetState.show() }
                    }
                },
                onBudgetEditRequested = { it.budgetId?.let { id -> onEditBudgetRequested(id) } }
            )
        }
    }
}