package com.example.gazege.ui.navigation

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.ui.views.category.CategoryForm
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenAddCategory(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable("addCategory") {
        val categories by viewModel.rememberCategories()
        val budgetWithCalculatedDataAndCategory by viewModel.rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData()

        val coroutineScope = rememberCoroutineScope()

        CategoryForm(
            null,
            categories,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            onCategorySave = { category, snackbar ->
                viewModel.insertCategory(
                    category,
                    onCompleitionAction = { onNavigateUp() }
                ) { error ->
                    val msg = when (error) {
                        is SQLiteConstraintException -> if (category.name in categories.map { it.name }) {
                            "${category.name} ya existe."
                        } else {
                            "CONSTRAINT ERROR"
                        }

                        else -> error.toString()
                    }
                    coroutineScope.launch {
                        snackbar.showSnackbar("Error agregando ${category.name}: \n$msg")
                    }
                }
            },
            onBudgetDeleteRequested = {},
            onBudgetDetailRequested = {},
            onBudgetEditRequested = {},
            onBudgetAddRequested = null
        )
    }
}

fun NavController.navigateToAddCategory() {
    navigate("addCategory")
}