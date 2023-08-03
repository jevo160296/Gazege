package com.example.gazege.core.entities

import com.example.gazege.core.dao.CategoryDao


data class CategoryWithSubcategoriesAndBudgetWithCalculatedData(
    val category: CategoryWithCalculatedData,
    val budget: List<BudgetWithCalculatedData>,
    val subCategories: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    val completion: Double
) {
    val aggregatedBudget
        get(): BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData? =
            budget
                .map { BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData.from(it) }
                .sumOrNull()
    val childrenAggregatedBudget
        get(): BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData? =
            subCategories
                .takeIf { it.isNotEmpty() }
                ?.mapNotNull { parent ->
                    val childrenTotal =
                        parent.subCategories.mapNotNull { it.childrenAggregatedBudget }.sumOrNull()
                    val parentBudget = parent
                        .budget
                        .map { BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData.from(it) }
                        .sumOrNull()
                    if (parentBudget != null || childrenTotal != null) {
                        (parentBudget
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
                .groupBy { it.budget.categoryId }
                .let { mappedBudget ->
                    categoriesWithSubcategories.mapNotNull {
                        val budgetList = mappedBudget[it.category.id]
                        val aggregatedBudget = budgetList
                            ?.map { budget ->
                                BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData.from(
                                    budget.toBudgetWithCalculatedData()
                                )
                            }
                            ?.sumOrNull()
                        val categoryWithCalculatedData =
                            categoriesWithCalculatedData[it.category.id]
                        if (categoryWithCalculatedData != null) {
                            val completion = CategoryDao.calculateCategoryCompleition(
                                realTotalFlow = categoryWithCalculatedData.realTotalFlow,
                                expectedTotalFlow = aggregatedBudget?.expectedTotalFlow
                                    ?: 0.0
                            )
                            CategoryWithSubcategoriesAndBudgetWithCalculatedData(
                                category = categoryWithCalculatedData,
                                budget = budgetList
                                    ?.map { budget -> budget.toBudgetWithCalculatedData() }
                                    ?: emptyList(),
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
