package com.example.gazege.core.entities

data class BudgetWithCalculatedDataAndCategory(
    val budget: BudgetWithCalculatedData,
    val category: Category
) {
    fun toBudgetWithCalculatedData(): BudgetWithCalculatedData = this.budget

    val budgetId get() = budget.budget.id

    val budgetValue get() = budget.budget.value

    val budgetFrequency get() = budget.budget.frequency

    val budgetFrequencyType get() = budget.budget.frequencyType

    val budgetType get() = budget.budget.budgetType

    val categoryName get() = category.name

    val budgetExpectedFlowUntilNow get() = budget.expectedFlowUntilToday

    val budgetExpectedTotalFlow get() = budget.expectedTotalFlow

    val budgetExpectedFlowFromToday get() = budget.expectedTotalFlow - budget.expectedFlowUntilToday

    val budgetDescription get() = budget.budget.description

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
                                    budget = it,
                                    category = notNullCategory
                                )
                            }
                        }
                }
    }
}