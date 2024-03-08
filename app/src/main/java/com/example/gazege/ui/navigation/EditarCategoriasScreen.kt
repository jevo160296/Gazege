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
    viewModelCategoryList: MainViewModel.ViewModelCategoryList,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Int?) -> Unit,
    onNavigateToAddBudget: (Int?) -> Unit,
    onExportCategoryRequested: (Category) -> Unit
) {
    composable("editCategories") {
        val showPlot by viewModelCategoryList.rememberShowPlot()

        when (val editarCategoriasState =
            viewModelCategoryList.rememberEditarCategoriasState().value) {
            is LoadedEditarCategoriasState -> LoadedEditarCategorias(
                editarCategoriasState,
                onAddCategoryRequested = onNavigateToAddCategory,
                onEditCategoryRequested = { onNavigateToEditCategory(it.id) },
                onDeleteCategoryRequested = { viewModelCategoryList.deleteCategory(it) },
                onSetBudgetRequested = { onNavigateToAddBudget(it.id) },
                onExportCategoryRequested = onExportCategoryRequested,
                showPlot = showPlot,
                onShowPlotChanged = viewModelCategoryList::updateShowPlot
            )

            is EmptyEditarCategoriasState -> EmptyEditarCategorias(editarCategoriasState)
        }
    }
}

fun NavController.navigateToEditarCategorias() {
    navigate("editCategories")
}