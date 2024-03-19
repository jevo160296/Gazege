package com.jmml.gazege.ui.navigation

import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData

interface EditarCategoriasState {
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
    val leftToPay: Double
    val realTotalFlow: Double
    val netFlow: Double
}

fun nullCategoriasState(): EditarCategoriasState = EmptyEditarCategoriasState

object EmptyEditarCategoriasState : EditarCategoriasState {
    override val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
        get() = emptyList()
    override val leftToPay: Double
        get() = 0.0
    override val realTotalFlow: Double
        get() = 0.0
    override val netFlow: Double
        get() = 0.0

}

data class LoadedEditarCategoriasState(
    override val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    override val leftToPay: Double,
    override val realTotalFlow: Double,
    override val netFlow: Double
) : EditarCategoriasState {
    companion object {
        fun from(
            categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
        ): EditarCategoriasState {
            val (leftToPay, realTotalFlow, netFlow) = categoriesWithCalculatedData
                .map {
                    Pair(
                        it.leftToPay + it.childrenLeftToPay,
                        it.realTotalFlow + it.childrenRealTotalFlow
                    ).let { value ->
                        Triple(
                            value.first,
                            value.second,
                            value.first + value.second
                        )
                    }
                }.fold(Triple(0.0, 0.0, 0.0)) { cum, current ->
                    Triple(
                        cum.first + current.first,
                        cum.second + current.second,
                        cum.third + current.third
                    )
                }

            return LoadedEditarCategoriasState(
                categoriesWithCalculatedData = categoriesWithCalculatedData,
                leftToPay = leftToPay,
                netFlow = netFlow,
                realTotalFlow = realTotalFlow
            )
        }
    }
}