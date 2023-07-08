package com.example.gazege.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.ui.fragments.EmptyEditarCategorias
import com.example.gazege.ui.fragments.LoadedEditarCategorias

fun NavGraphBuilder.screenEditarCategorias(
    viewModel: MainViewModel,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Int?) -> Unit,
    onNavigateToAddBudget: (Int?) -> Unit
) {
    composable("editCategories") {

        when (val editarCategoriasState = viewModel.rememberEditarCategoriasState().value) {
            is LoadedEditarCategoriasState -> LoadedEditarCategorias(
                editarCategoriasState,
                onAddCategoryRequested = onNavigateToAddCategory,
                onEditCategoryRequested = { onNavigateToEditCategory(it.id) },
                onDeleteCategoryRequested = { viewModel.deleteCategory(it) },
                onSetBudgetRequested = { onNavigateToAddBudget(it.id) }
            )

            is EmptyEditarCategoriasState -> EmptyEditarCategorias(editarCategoriasState)
        }
    }
}

fun NavController.navigateToEditarCategorias() {
    navigate("editCategories")
}