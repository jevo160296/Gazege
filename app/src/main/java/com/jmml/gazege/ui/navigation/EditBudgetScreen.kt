package com.jmml.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.ui.fragments.EditBudgetFragment

fun NavGraphBuilder.screenEditBudget(
    viewModelEditBudget: MainViewModel.ViewModelEditBudget,
    onNavigateToAddOneBudget: () -> Unit,
    onNavigateToOneBudgetDetail: (budgetId: Int) -> Unit,
    onNavigateToOneBudgetEdit: (budgetId: Int) -> Unit
) {
    composable("editarBudget") {
        val budget by viewModelEditBudget.rememberBudgetAndCategoryWithCalculatedData()
        EditBudgetFragment(
            budget = budget,
            onAddOneBudgetRequested = onNavigateToAddOneBudget,
            onGetBudgetDetailRequested = onNavigateToOneBudgetDetail,
            onDeleteBudgetRequested = { id ->
                budget
                    .firstOrNull { it.budgetId == id }
                    ?.let { viewModelEditBudget.deleteBudget(it.budget.budget) }
            },
            onEditBudgetRequested = onNavigateToOneBudgetEdit
        )
    }
}

fun NavController.navigateToEditBudget() {
    navigate("editarBudget")
}