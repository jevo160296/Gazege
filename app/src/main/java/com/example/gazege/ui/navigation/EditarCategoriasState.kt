package com.example.gazege.ui.navigation

import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData

interface EditarCategoriasState {
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
    val expectedTotalIncome: Double
    val expectedTotalOutcome: Double
    val expectedNetValue: Double
}

fun nullCategoriasState(): EditarCategoriasState = EmptyEditarCategoriasState

object EmptyEditarCategoriasState : EditarCategoriasState {
    override val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
        get() = emptyList()
    override val expectedTotalIncome: Double
        get() = 0.0
    override val expectedTotalOutcome: Double
        get() = 0.0
    override val expectedNetValue: Double
        get() = 0.0

}

data class LoadedEditarCategoriasState(
    override val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    override val expectedTotalIncome: Double,
    override val expectedTotalOutcome: Double,
    override val expectedNetValue: Double
) : EditarCategoriasState {
    companion object {
        fun from(
            categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
        ): EditarCategoriasState {
            val expectedIncomeOutcome = categoriesWithCalculatedData
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
            val expectedIncome = expectedIncomeOutcome.first
            val expectedOutcome = expectedIncomeOutcome.second
            return LoadedEditarCategoriasState(
                categoriesWithCalculatedData,
                expectedIncome,
                expectedOutcome,
                expectedIncome + expectedOutcome
            )
        }
    }
}