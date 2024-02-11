package com.example.gazege.core.entities

data class BudgetWithCalculatedDataAndCategory(
    val budget: Budget,
    val category: Category,
    val budgetExpectedTotalFlow: Double,
    val budgetExpectedRemainingFlow: Double,
    val budgetExpectedFlowUntilNow: Double,
    val budgetLeftToPayFromToday: Double,
    val budgetLeftToPayToday: Double
) {
    fun toBudgetWithCalculatedData(): BudgetWithCalculatedData = BudgetWithCalculatedData(
        budget = this.budget,
        expectedTotalFlow = budgetExpectedTotalFlow,
        expectedRemainingFlowFromToday = budgetExpectedRemainingFlow,
        expectedFlowUntilNow = budgetExpectedFlowUntilNow,
        leftToPayFromToday = budgetLeftToPayFromToday,
        leftToPayToday = budgetLeftToPayToday
    )

    val budgetId get() = budget.id
    val budgetFrequency get() = budget.frequency
    val budgetFrequencyType get() = budget.frequencyType
    val budgetType get() = budget.budgetType
    val budgetValue get() = budget.value
    val budgetDescription get() = budget.description
    val categoryName get() = category.name

    companion object {
        fun from(
            budget: List<BudgetWithCalculatedData>,
            categories: List<Category>
        ): List<BudgetWithCalculatedDataAndCategory> =
            categories.associateBy { it.id }
                .let { groupedCategories ->
                    budget
                        .mapNotNull {
                            groupedCategories[it.budget.categoryId]?.let { notNullCategory ->
                                BudgetWithCalculatedDataAndCategory(
                                    budget = it.budget,
                                    category = notNullCategory,
                                    budgetExpectedTotalFlow = it.expectedTotalFlow,
                                    budgetExpectedRemainingFlow = it.expectedRemainingFlowFromToday,
                                    budgetExpectedFlowUntilNow = it.expectedFlowUntilNow,
                                    budgetLeftToPayFromToday = it.leftToPayFromToday,
                                    budgetLeftToPayToday = it.leftToPayToday
                                )
                            }
                        }
                }
    }
}