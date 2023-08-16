package com.example.gazege.ui.navigation

import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData

interface EditarCategoriasState {
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
    val expectedTotalIncome: Double
    val expectedTotalOutcome: Double
    val expectedNetValue: Double

    val realTotalIncome: Double
    val realTotalOutcome: Double
    val realNetValue: Double

    val totalIncomeProgress: Double
        get() = if (expectedTotalIncome != 0.0) {
            realTotalIncome / expectedTotalIncome
        } else {
            1.0
        }
    val totalOutcomeProgress: Double
        get() = if (expectedTotalOutcome != 0.0) {
            realTotalOutcome / expectedTotalOutcome
        } else {
            1.0
        }
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
    override val realTotalIncome: Double
        get() = 0.0
    override val realTotalOutcome: Double
        get() = 0.0
    override val realNetValue: Double
        get() = 0.0

}

data class LoadedEditarCategoriasState(
    override val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    override val expectedTotalIncome: Double,
    override val expectedTotalOutcome: Double,
    override val expectedNetValue: Double,
    override val realTotalIncome: Double,
    override val realTotalOutcome: Double,
    override val realNetValue: Double
) : EditarCategoriasState {
    companion object {
        fun from(
            categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
        ): EditarCategoriasState {
            val expectedIncomeOutcome = categoriesWithCalculatedData
                .map { actual ->
                    actual.expectedTotalFlow + actual.childrenExpectedTotalFlow
                }
                .fold(Pair(0.0, 0.0)) { accum, current ->
                    when (current > 0) {
                        true -> accum.copy(first = accum.first + current)
                        false -> accum.copy(second = accum.second + current)
                    }
                }
            val expectedIncome = expectedIncomeOutcome.first
            val expectedOutcome = expectedIncomeOutcome.second

            val realIncomeOutcome = categoriesWithCalculatedData
                .map { actual ->
                    actual.realTotalFlow + actual.childrenRealTotalFlow
                }
                .fold(Pair(0.0, 0.0)) { accum, current ->
                    when (current > 0) {
                        true -> accum.copy(first = accum.first + current)
                        false -> accum.copy(second = accum.second + current)
                    }
                }
            val realIncome = realIncomeOutcome.first
            val realOutcome = realIncomeOutcome.second
            return LoadedEditarCategoriasState(
                categoriesWithCalculatedData,
                expectedIncome,
                expectedOutcome,
                expectedIncome + expectedOutcome,
                realIncome,
                realOutcome,
                realIncome + realOutcome
            )
        }
    }
}