package com.example.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.ui.fragments.EditarCategorias

fun NavGraphBuilder.screenEditarCategorias(
    viewModel: MainViewModel,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Int?) -> Unit,
    onNavigateToAddBudget: (Int?) -> Unit
) {
    composable("editCategories") {
        val categoryWithSubcategoriesAndBudgetWithCalculatedData by viewModel.rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData()

        EditarCategorias(
            categoryWithSubcategoriesAndBudgetWithCalculatedData,
            onAddCategoryRequested = onNavigateToAddCategory,
            onEditCategoryRequested = { onNavigateToEditCategory(it.id) },
            onDeleteCategoryRequested = { viewModel.deleteCategory(it) },
            onSetBudgetRequested = { onNavigateToAddBudget(it.id) }
        )
    }
}

fun NavController.navigateToEditarCategorias() {
    navigate("editCategories")
}