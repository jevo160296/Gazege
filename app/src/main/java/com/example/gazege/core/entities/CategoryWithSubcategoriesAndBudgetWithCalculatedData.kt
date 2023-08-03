package com.example.gazege.core.entities

import com.example.gazege.core.dao.CategoryDao


data class CategoryWithSubcategoriesAndBudgetWithCalculatedData(
    val category: CategoryWithCalculatedData,
    val budget: BudgetWithCalculatedData?,
    val subCategories: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    val completion: Double
) {
    val aggregatedBudget
        get(): BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData? =
            budget?.let { BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData.from(it) }
    val childrenAggregatedBudget
        get(): BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData? =
            subCategories
                .takeIf { it.isNotEmpty() }
                ?.mapNotNull { parent ->
                    val childrenTotal =
                        parent.subCategories.mapNotNull { it.childrenAggregatedBudget }.sumOrNull()
                    val parentBudget = parent.budget
                    if (parentBudget != null || childrenTotal != null) {
                        (parent.budget?.let { it + BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData() }
                            ?: BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData()) +
                                (childrenTotal
                                    ?: BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData())
                    } else {
                        null
                    }
                }
                ?.sumOrNull()

    val realTotalFlow get() = category.realTotalFlow

    companion object {
        fun from(
            budgetWithCalculatedDataAndCategory: List<BudgetWithCalculatedDataAndCategory>,
            categoriesWithSubcategories: List<CategoryWithSubCategories>,
            categoriesWithCalculatedData: Map<Int, CategoryWithCalculatedData>
        ): List<CategoryWithSubcategoriesAndBudgetWithCalculatedData> =
            budgetWithCalculatedDataAndCategory
                .associateBy { it.budget.categoryId }
                .let { mappedBudget ->
                    categoriesWithSubcategories.mapNotNull {
                        val categoryWithCalculatedData =
                            categoriesWithCalculatedData[it.category.id]
                        if (categoryWithCalculatedData != null) {
                            val completion = CategoryDao.calculateCategoryCompleition(
                                realTotalFlow = categoryWithCalculatedData.realTotalFlow,
                                expectedTotalFlow = mappedBudget[it.category.id]?.budgetExpectedTotalFlow
                                    ?: 0.0
                            )
                            CategoryWithSubcategoriesAndBudgetWithCalculatedData(
                                category = categoryWithCalculatedData,
                                budget = mappedBudget[it.category.id]?.toBudgetWithCalculatedData(),
                                subCategories = from(
                                    budgetWithCalculatedDataAndCategory,
                                    it.subCategories,
                                    categoriesWithCalculatedData
                                ),
                                completion = completion
                            )
                        } else {
                            null
                        }
                    }
                }
    }
}
