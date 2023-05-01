package com.example.gazege.core.entities

data class BudgetWithCalculatedDataAndCategory(
    val budget: Budget,
    val category: Category,
    val budgetExpectedTotalFlow: Double,
    val budgetExpectedRemainingFlow: Double,
    val budgetRealTotalFlow: Double,
    val budgetExpectedFlowUntilNow: Double,
    val budgetCompleition: Double,
    val budgetLeftToPay: Double
) {
    fun toBudgetWithCalculatedData(): BudgetWithCalculatedData = BudgetWithCalculatedData(
        budget = this.budget,
        expectedTotalFlow = budgetExpectedTotalFlow,
        expectedRemainingFlow = budgetExpectedRemainingFlow,
        realTotalFlow = budgetRealTotalFlow,
        expectedFlowUntilNow = budgetExpectedFlowUntilNow,
        compleition = budgetCompleition,
        leftToPay = budgetLeftToPay
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
                                    budgetExpectedRemainingFlow = it.expectedRemainingFlow,
                                    budgetRealTotalFlow = it.realTotalFlow,
                                    budgetCompleition = it.compleition,
                                    budgetExpectedFlowUntilNow = it.expectedFlowUntilNow,
                                    budgetLeftToPay = it.leftToPay
                                )
                            }
                        }
                }

    }
}