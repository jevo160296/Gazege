package com.example.gazege.ui.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.Category
import com.example.gazege.ui.fragments.EmptyEditarCategorias
import com.example.gazege.ui.fragments.LoadedEditarCategorias

fun NavGraphBuilder.screenEditarCategorias(
    viewModel: MainViewModel,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Int?) -> Unit,
    onNavigateToAddBudget: (Int?) -> Unit,
    onExportCategoryRequested: (Category) -> Unit
) {
    composable("editCategories") {
        val showPlot by viewModel.categoryListStates.rememberShowPlot()

        when (val editarCategoriasState = viewModel.rememberEditarCategoriasState().value) {
            is LoadedEditarCategoriasState -> LoadedEditarCategorias(
                editarCategoriasState,
                onAddCategoryRequested = onNavigateToAddCategory,
                onEditCategoryRequested = { onNavigateToEditCategory(it.id) },
                onDeleteCategoryRequested = { viewModel.deleteCategory(it) },
                onSetBudgetRequested = { onNavigateToAddBudget(it.id) },
                onExportCategoryRequested = onExportCategoryRequested,
                showPlot = showPlot,
                onShowPlotChanged = viewModel.categoryListStates::updateShowPlot
            )

            is EmptyEditarCategoriasState -> EmptyEditarCategorias(editarCategoriasState)
        }
    }
}

fun NavController.navigateToEditarCategorias() {
    navigate("editCategories")
}