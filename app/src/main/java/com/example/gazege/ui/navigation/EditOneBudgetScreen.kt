package com.example.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.ui.views.budget.BudgetFormView

fun NavGraphBuilder.screenEditOneBudget(
    viewModel: MainViewModel.ViewModelEditOneBudget,
    onNavigateUp: () -> Unit
) {
    composable(
        "editOneBudget/{budgetId}",
        arguments = listOf(
            navArgument("budgetId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val budgetId = navStack.arguments?.getInt("budgetId")
        val budgets by viewModel.rememberBudget()
        val categories by viewModel.rememberCategories()
        val budgetWithCalculatedDataAndCategory by viewModel.rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData()
        BudgetFormView(
            budget = budgets.firstOrNull { it.id == budgetId },
            categories = categories,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            onSaveBudget = {
                viewModel.updateBudget(
                    it,
                    onCompleitionAction = { onNavigateUp() },
                    onErrorAction = {})
            }
        )
    }
}

fun NavController.navigateToEditOneBudget(budgetId: Int) {
    navigate("editOneBudget/$budgetId")
}