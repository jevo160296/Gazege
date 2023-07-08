package com.example.gazege.ui.navigation

import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData

interface EditarCategoriasState {
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
    val totalIncome: Double
    val totalOutcome: Double
    val netValue: Double
}

fun nullCategoriasState(): EditarCategoriasState = EmptyEditarCategoriasState

object EmptyEditarCategoriasState : EditarCategoriasState {
    override val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
        get() = emptyList()
    override val totalIncome: Double
        get() = 0.0
    override val totalOutcome: Double
        get() = 0.0
    override val netValue: Double
        get() = 0.0

}

data class LoadedEditarCategoriasState(
    override val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    override val totalIncome: Double,
    override val totalOutcome: Double,
    override val netValue: Double
) : EditarCategoriasState {
    companion object {
        fun from(
            categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
        ): EditarCategoriasState {
            return LoadedEditarCategoriasState(
                categoriesWithCalculatedData,
                0.0,
                0.0,
                0.0
            )
        }
    }
}