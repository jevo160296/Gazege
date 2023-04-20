package com.example.gazege.core.entities

import com.example.gazege.core.dao.BudgetDao

data class CategoryWithBudgetData(
    val category: Category,
    val budgetExpectedFlowUntilNow: Double,
    val budgetExpectedTotalFlow: Double,
    val budgetRealTotalFlow: Double,
    val completion: Double
) {
    companion object {
        fun from(categories: List<Category>, budget: List<BudgetAndCategoryWithCalculatedData>):
                Map<Category, CategoryWithBudgetData?> {
            val mappedBudget = budget.groupBy { it.category.id }
            return categories.associateWith { currentCategory ->
                mappedBudget[currentCategory.id]?.let { budgetList ->
                    CategoryWithBudgetData(
                        category = currentCategory,
                        budgetExpectedFlowUntilNow = budgetList.sumOf { it.budgetExpectedFlowUntilNow },
                        budgetExpectedTotalFlow = budgetList.sumOf { it.budgetExpectedTotalFlow },
                        budgetRealTotalFlow = budgetList.sumOf { it.budgetRealTotalFlow },
                        completion = BudgetDao.calculateBudgetCompletion(budgetList)
                    )
                }
            }
        }
    }
}