package com.jmml.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.ui.views.budget.BudgetDetailView
import com.jmml.gazege.ui.views.budget.EmptyBudgetDetailView

fun NavGraphBuilder.screenOneBudgetDetail(
    viewModelOneBudgetDetail: MainViewModel.ViewModelOneBudgetDetail
) {
    composable(
        "oneBudgetDetail/{budgetId}",
        arguments = listOf(
            navArgument("budgetId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val budgetId = navStack.arguments?.getInt("budgetId")
        val budgetAndCategoryWithTransactions by viewModelOneBudgetDetail.rememberBudgetAndCategoryWithTransactions()
        val selectedBudget = budgetAndCategoryWithTransactions
            .firstOrNull { it.budgetId == budgetId }
        if (selectedBudget != null) {
            BudgetDetailView(selectedBudget)
        } else {
            EmptyBudgetDetailView()
        }
    }
}

fun NavController.navigateToOneBudgetDetail(budgetId: Int) {
    //TODO Aún no se tiene lista la página de detalles para los presupupestos.
    //navigate("oneBudgetDetail/$budgetId")
}