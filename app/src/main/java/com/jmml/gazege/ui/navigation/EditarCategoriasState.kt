package com.jmml.gazege.ui.navigation

import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubCategories
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData

sealed interface ICategoriesView

sealed interface ICategoriesWithBudgetDataView : ICategoriesView {
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
    val leftToPay: Double
    val realTotalFlow: Double
    val netFlow: Double
}

sealed interface ICategoriesDataView : ICategoriesView {
    val categoriesWithSubCategories: List<CategoryWithSubCategories>
}

data class ReloadingCategoriesWithBudgetDataView(
    override val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    override val leftToPay: Double,
    override val realTotalFlow: Double,
    override val netFlow: Double
) : ICategoriesWithBudgetDataView {
    companion object {
        fun from(LoadedCategoriesWithBudgetDataView: LoadedCategoriesWithBudgetDataView) =
            LoadedCategoriesWithBudgetDataView.run {
                ReloadingCategoriesWithBudgetDataView(
                    categoriesWithCalculatedData = categoriesWithCalculatedData,
                    leftToPay = leftToPay,
                    realTotalFlow = realTotalFlow,
                    netFlow = netFlow
                )
            }

    }
}

data class LoadedCategoriesWithBudgetDataView(
    override val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    override val leftToPay: Double,
    override val realTotalFlow: Double,
    override val netFlow: Double
) : ICategoriesWithBudgetDataView {
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

data class ReloadingCategoriesDataView(
    override val categoriesWithSubCategories: List<CategoryWithSubCategories>
) : ICategoriesDataView {
    companion object {
        fun from(loadedCategoriesDataView: LoadedCategoriesDataView) =
            loadedCategoriesDataView.run {
                ReloadingCategoriesDataView(
                    categoriesWithSubCategories = categoriesWithSubCategories
                )
            }
    }
}

data class LoadedCategoriesDataView(
    override val categoriesWithSubCategories: List<CategoryWithSubCategories>
) : ICategoriesDataView {
    companion object {
        fun from(
            categories: List<Category>
        ) = LoadedCategoriesDataView(
            CategoryWithSubCategories.from(categories)
        )
    }
}