package com.example.gazege.ui.navigation

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.Category
import com.example.gazege.ui.views.category.CategoryForm
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenEditCategory(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToEditOneBudgetRequested: (Int) -> Unit,
    onNavigateToAddOneBudgetRequested: (Category) -> Unit
) {
    composable(
        "editCategory/{categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val categories by viewModel.rememberCategories()
        val budgetWithCalculatedDataAndCategory by viewModel.rememberBudgetAndCategoryWithCalculatedDataMap()
        val budgetAndCategoryWithCalculatedData by viewModel.rememberBudgetAndCategoryWithCalculatedData()

        val coroutineScope = rememberCoroutineScope()

        val categoryId = navStack.arguments?.getInt("categoryId")
        val category = categories.firstOrNull { it.id == categoryId }

        val categoryBudget =
            budgetAndCategoryWithCalculatedData.filter { it.category.id == categoryId }
        CategoryForm(
            category?.let { Pair(category, categoryBudget) },
            categories,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            onCategorySave = { newCategory, state ->
                viewModel.updateCategory(
                    newCategory,
                    onCompleitionAction = onNavigateUp
                ) { error ->
                    val msg = when (error) {
                        is SQLiteConstraintException -> if (newCategory.name in categories.map { it.name }) {
                            "${newCategory.name} ya existe."
                        } else {
                            "CONSTRAINT ERROR"
                        }

                        else -> error.toString()
                    }
                    coroutineScope.launch {
                        state.showSnackbar("Error agregando ${newCategory.name}: \n$msg")
                    }
                }
            },
            onBudgetDeleteRequested = { viewModel.deleteBudget(it.budget) },
            onBudgetDetailRequested = {},
            onBudgetEditRequested = { budget ->
                budget.budget.id?.let {
                    onNavigateToEditOneBudgetRequested(
                        it
                    )
                }
            },
            onBudgetAddRequested = onNavigateToAddOneBudgetRequested
        )
    }
}

fun NavController.navigateToEditCategory(categoryId: Int?) {
    navigate("editCategory/$categoryId")
}