package com.jmml.gazege.ui.navigation

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.ui.views.category.CategoryForm
import com.jmml.gazege.ui.views.category.LoadingCategoryForm
import com.jmml.zoo.clases.Result
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenAddCategory(
    viewModelAddCategory: MainViewModel.ViewModelAddCategory,
    onNavigateUp: () -> Unit
) {
    composable("addCategory") {
        val categories by viewModelAddCategory.rememberCategories()
        val budgetWithCalculatedDataAndCategory =
            viewModelAddCategory.rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData().value

        val coroutineScope = rememberCoroutineScope()

        when (budgetWithCalculatedDataAndCategory) {
            is Result.Error -> Text("Error: ${budgetWithCalculatedDataAndCategory.exception}")
            Result.Loading -> LoadingCategoryForm()
            is Result.Success -> CategoryForm(
                null,
                categories,
                budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory.data,
                onCategorySave = { category, snackbar ->
                    viewModelAddCategory.insertCategory(
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
}

fun NavController.navigateToAddCategory() {
    navigate("addCategory")
}