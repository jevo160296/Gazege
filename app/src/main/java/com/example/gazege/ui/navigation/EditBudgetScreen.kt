package com.example.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.ui.fragments.EditBudgetFragment

fun NavGraphBuilder.screenEditBudget(
    viewModel: MainViewModel,
    onNavigateToAddOneBudget: () -> Unit,
    onNavigateToOneBudgetDetail: (budgetId: Int) -> Unit,
    onNavigateToOneBudgetEdit: (budgetId: Int) -> Unit
) {
    composable("editarBudget") {
        val budget by viewModel.rememberBudgetAndCategoryWithCalculatedData()
        EditBudgetFragment(
            budget = budget,
            onAddOneBudgetRequested = onNavigateToAddOneBudget,
            onGetBudgetDetailRequested = onNavigateToOneBudgetDetail,
            onDeleteBudgetRequested = { id ->
                budget
                    .firstOrNull { it.budgetId == id }
                    ?.let { viewModel.deleteBudget(it.budget) }
            },
            onEditBudgetRequested = onNavigateToOneBudgetEdit
        )
    }
}

fun NavController.navigateToEditBudget() {
    navigate("editarBudget")
}