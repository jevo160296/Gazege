package com.jmml.gazege.ui.navigation

import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.ui.views.budget.BudgetFormView
import com.jmml.gazege.ui.views.budget.LoadingBudgetFormView
import com.jmml.zoo.clases.Result

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
        val budgetWithCalculatedDataAndCategory =
            viewModel.rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData().value
        when (budgetWithCalculatedDataAndCategory) {
            is Result.Error -> Text(text = "Error ${budgetWithCalculatedDataAndCategory.exception}")
            Result.Loading -> LoadingBudgetFormView()
            is Result.Success -> BudgetFormView(
                budget = budgets.firstOrNull { it.id == budgetId },
                categories = categories,
                budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory.data,
                onSaveBudget = {
                    viewModel.updateBudget(
                        it,
                        onCompleitionAction = { onNavigateUp() },
                        onErrorAction = {})
                }
            )
        }
    }
}

fun NavController.navigateToEditOneBudget(budgetId: Int) {
    navigate("editOneBudget/$budgetId")
}