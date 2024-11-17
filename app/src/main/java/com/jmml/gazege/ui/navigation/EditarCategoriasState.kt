package com.jmml.gazege.ui.navigation

import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubCategories
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData

sealed interface ICategoriesView

data class LoadedCategoriesWithBudgetDataView(
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    val leftToPay: Double,
    val realTotalFlow: Double,
    val netFlow: Double
) : ICategoriesView {
    companion object {
        fun from(
            categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
        ): LoadedCategoriesWithBudgetDataView {
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

            return LoadedCategoriesWithBudgetDataView(
                categoriesWithCalculatedData = categoriesWithCalculatedData,
                leftToPay = leftToPay,
                netFlow = netFlow,
                realTotalFlow = realTotalFlow
            )
        }
    }
}

data class LoadedCategoriesDataView(
    val categoriesWithSubCategories: List<CategoryWithSubCategories>
) : ICategoriesView {
    companion object {
        fun from(
            categories: List<Category>
        ) = LoadedCategoriesDataView(
            CategoryWithSubCategories.from(categories)
        )
    }
}