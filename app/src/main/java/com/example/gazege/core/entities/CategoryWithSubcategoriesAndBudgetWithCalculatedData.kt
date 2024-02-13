package com.example.gazege.core.entities

import com.example.gazege.core.dao.BudgetDao
import com.example.gazege.core.dao.CategoryDao


data class CategoryWithSubcategoriesAndBudgetWithCalculatedData(
    val category: CategoryWithCalculatedData,
    val budget: List<BudgetWithCalculatedData>,
    val subCategories: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
) {
    val aggregatedBudget
        get(): BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData =
            budget
                .map { BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData.from(it) }
                .sumOrNull()
                ?: BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData()
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

    val expectedTotalFlow get() = aggregatedBudget.expectedTotalFlow

    val childrenExpectedTotalFlow
        get(): Double =
            subCategories.sumOf { it.expectedTotalFlow + it.childrenExpectedTotalFlow }

    val realTotalFlow get() = category.realTotalFlow

    val childrenRealTotalFlow: Double
        get() = subCategories
            .sumOf { it.realTotalFlow + it.childrenRealTotalFlow }

    val completion = CategoryDao.calculateCategoryCompleition(
        realTotalFlow = realTotalFlow,
        expectedTotalFlow = expectedTotalFlow
    )

    val completionWithChildren = CategoryDao.calculateCategoryCompleition(
        realTotalFlow = realTotalFlow + childrenRealTotalFlow,
        expectedTotalFlow = expectedTotalFlow + childrenExpectedTotalFlow
    )

    val expectedFlowUntilToday
        get() = aggregatedBudget.expectedFlowUntilToday

    val childrenExpectedFlowUntilToday: Double
        get() = subCategories
            .sumOf { it.expectedFlowUntilToday + it.childrenExpectedFlowUntilToday }

    val leftToPayToday: Double
        get() = BudgetDao.calculateLeftToPayToday(
            budgetType = category.category.budgetType,
            expectedRemainingFlowTomorrow = aggregatedBudget.expectedFlowFromTomorrow,
            expectedRemainingFlowToday = aggregatedBudget.expectedFlowFromToday,
            expectedFlowUntilNow = aggregatedBudget.expectedFlowUntilToday,
            realTotalFlowToday = category.realTotalFlowToday,
            realTotalFlow = category.realTotalFlow
        )

    val leftToPay: Double
        get() = BudgetDao.calculateLeftToPayFromToday(
            budgetType = category.category.budgetType,
            expectedRemainingFlowTomorrow = aggregatedBudget.expectedFlowFromTomorrow,
            leftToPayToday = leftToPayToday,
            expectedTotalFlow = aggregatedBudget.expectedTotalFlow,
            realTotalFlow = category.realTotalFlow
        )

    val childrenLeftToPay: Double
        get() = subCategories
            .sumOf { it.leftToPay + it.childrenLeftToPay }

    companion object {
        fun from(
            budgetWithCalculatedDataAndCategory: List<BudgetWithCalculatedDataAndCategory>,
            categoriesWithSubcategories: List<CategoryWithSubCategories>,
            categoriesWithCalculatedData: Map<Int, CategoryWithCalculatedData>
        ): List<CategoryWithSubcategoriesAndBudgetWithCalculatedData> =
            budgetWithCalculatedDataAndCategory
                .groupBy { it.category.id }
                .let { mappedBudget ->
                    categoriesWithSubcategories.mapNotNull {
                        val budgetList = mappedBudget[it.category.id]
                        val categoryWithCalculatedData =
                            categoriesWithCalculatedData[it.category.id]
                        if (categoryWithCalculatedData != null) {
                            val subCategories = from(
                                budgetWithCalculatedDataAndCategory,
                                it.subCategories,
                                categoriesWithCalculatedData
                            )
                            CategoryWithSubcategoriesAndBudgetWithCalculatedData(
                                category = categoryWithCalculatedData,
                                budget = budgetList
                                    ?.map { budget -> budget.toBudgetWithCalculatedData() }
                                    ?: emptyList(),
                                subCategories = subCategories
                            )
                        } else {
                            null
                        }
                    }
                }
    }
}

fun List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>.recursiveFirstOrNull(predicate: (CategoryWithSubcategoriesAndBudgetWithCalculatedData) -> Boolean):
        CategoryWithSubcategoriesAndBudgetWithCalculatedData? =
    this.firstOrNull(predicate) ?: this.firstNotNullOfOrNull {
        it.subCategories.recursiveFirstOrNull(predicate)
    }