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
            val incomeOutcome = categoriesWithCalculatedData
                .map { actual ->
                    (actual.aggregatedBudget?.expectedTotalFlow ?: 0.0) +
                            (actual.childrenAggregatedBudget?.expectedTotalFlow ?: 0.0)
                }
                .fold(Pair(0.0, 0.0)) { accum, current ->
                    when (current > 0) {
                        true -> accum.copy(first = accum.first + current)
                        false -> accum.copy(second = accum.second + current)
                    }
                }
            val income = incomeOutcome.first
            val outcome = incomeOutcome.second
            return LoadedEditarCategoriasState(
                categoriesWithCalculatedData,
                income,
                outcome,
                income + outcome
            )
        }
    }
}