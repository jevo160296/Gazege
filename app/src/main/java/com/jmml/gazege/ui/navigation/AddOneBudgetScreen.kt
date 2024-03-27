package com.jmml.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.core.entities.Budget
import com.jmml.gazege.ui.views.budget.BudgetFormView

fun NavGraphBuilder.screenAddOneBudget(
    viewModelAddOneBudget: MainViewModel.ViewModelAddOneBudget,
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
        val categories by viewModelAddOneBudget.rememberCategories()
        val budgetWithCalculatedDataAndCategory by viewModelAddOneBudget.rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData()
        val fixedCategory = categories.firstOrNull { it.id == categoryId }
        if (fixedCategory == null) {
            BudgetFormView(
                budget = null,
                categories = categories,
                budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
                onSaveBudget = {
                    viewModelAddOneBudget.insertBudget(
                        it,
                        onCompleitionAction = { onNavigateUp() },
                        onErrorAction = {})
                }
            )
        } else {
            BudgetFormView(
                budget = Budget.fromMonthly(
                    categoryId = fixedCategory.id!!,
                    value = 0.0
                ),
                categories = categories,
                budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
                onSaveBudget = {
                    viewModelAddOneBudget.insertBudget(
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