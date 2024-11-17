package com.jmml.gazege.ui.navigation

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.ui.views.category.CategoryForm
import com.jmml.gazege.ui.views.category.LoadingCategoryForm
import com.jmml.zoo.clases.Result
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenEditCategory(
    viewModelEditCategory: MainViewModel.ViewModelEditCategory,
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
        val categories by viewModelEditCategory.rememberCategories()
        val budgetWithCalculatedDataAndCategory =
            viewModelEditCategory.rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData().value
        val budgetAndCategoryWithCalculatedData by viewModelEditCategory.rememberBudgetAndCategoryWithCalculatedData()
        val budgetAndCategoryWithTransactions =
            viewModelEditCategory.rememberBudgetAndCategoryWithTransactions().value

        val coroutineScope = rememberCoroutineScope()

        val categoryId = navStack.arguments?.getInt("categoryId")
        val category = categories.firstOrNull { it.id == categoryId }

        val categoryBudgetWithCalculatedData =
            budgetAndCategoryWithCalculatedData?.filter { it.category.id == categoryId }
        when (budgetWithCalculatedDataAndCategory) {
            is Result.Error -> Text(text = "Error ${budgetWithCalculatedDataAndCategory.exception}")
            Result.Loading -> LoadingCategoryForm()
            is Result.Success -> when (budgetAndCategoryWithTransactions) {
                is Result.Error -> Text(text = "Error ${budgetAndCategoryWithTransactions.exception}")
                Result.Loading -> LoadingCategoryForm()
                is Result.Success -> CategoryForm(
                    category?.let {
                        Triple(
                            category,
                            budgetAndCategoryWithTransactions.data.filter { it.category.id == categoryId },
                            categoryBudgetWithCalculatedData
                        )
                    },
                    categories,
                    budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory.data,
                    onCategorySave = { newCategory, state ->
                        viewModelEditCategory.updateCategory(
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
                    onBudgetDeleteRequested = { viewModelEditCategory.deleteBudget(it) },
                    onBudgetDetailRequested = {},
                    onBudgetEditRequested = { budget ->
                        budget.id?.let {
                            onNavigateToEditOneBudgetRequested(
                                it
                            )
                        }
                    },
                    onBudgetAddRequested = onNavigateToAddOneBudgetRequested
                )
            }
        }
    }
}

fun NavController.navigateToEditCategory(categoryId: Int?) {
    navigate("editCategory/$categoryId")
}