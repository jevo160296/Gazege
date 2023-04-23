package com.example.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.BudgetType
import com.example.gazege.ui.views.budget.BudgetFormView

fun NavGraphBuilder.screenAddOneBudget(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable("addOneBudget?categoryId={categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    ) { navStack ->
        val categoryId = navStack.arguments?.getInt("categoryId")
        val categories by viewModel.rememberCategories()
        val fixedCategory = categories.firstOrNull { it.id == categoryId }
        if (fixedCategory == null) {
            BudgetFormView(
                budget = null,
                categories = categories,
                onSaveBudget = {
                    viewModel.insertBudget(
                        it,
                        onCompleitionAction = { onNavigateUp() },
                        onErrorAction = {})
                }
            )
        } else {
            BudgetFormView(
                budget = Budget.fromMonthly(
                    categoryId = fixedCategory.id!!,
                    value = 0.0,
                    budgetType = BudgetType.VARIABLE
                ),
                categories = categories,
                onSaveBudget = {
                    viewModel.insertBudget(
                        it,
                        onCompleitionAction = { onNavigateUp() },
                        onErrorAction = {})
                }
            )
        }
    }
}

fun NavController.navigateToAddOneBudget() {
    navigate("addOneBudget")
}

fun NavController.navigateToAddOneBudget(categoryId: Int) {
    navigate("addOneBudget?categoryId=$categoryId")
}