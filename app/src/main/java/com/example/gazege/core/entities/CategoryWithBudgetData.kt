package com.example.gazege.core.entities

data class CategoryWithBudgetData(
    val category: Category,
    val budgetExpectedFlowUntilNow: Double,
    val budgetExpectedTotalFlow: Double,
) {
    companion object {
        fun from(categories: List<Category>, budget: List<BudgetWithCalculatedData>):
                Map<Category, CategoryWithBudgetData?> {
            val mappedBudget = budget.groupBy { it.budget.categoryId }
            return categories.associateWith { category: Category ->
                val currentBudgets = mappedBudget[category.id]
                val aggregatedBudgets =
                    currentBudgets?.fold(BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData() as BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData) { a, b -> a + b }
                aggregatedBudgets?.let {
                    CategoryWithBudgetData(
                        category = category,
                        budgetExpectedFlowUntilNow = aggregatedBudgets.expectedFlowUntilNow,
                        budgetExpectedTotalFlow = aggregatedBudgets.expectedTotalFlow
                    )
                }
            }
        }
    }
}
