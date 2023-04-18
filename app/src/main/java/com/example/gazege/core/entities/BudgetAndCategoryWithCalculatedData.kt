package com.example.gazege.core.entities

import com.example.gazege.core.dao.BudgetDao
import java.time.LocalDate

data class BudgetAndCategoryWithCalculatedData(
    val budget: Budget,
    val category: Category,
    val budgetExpectedTotalFlow: Double,
    val budgetExpectedRemainingFlow: Double,
    val budgetRealTotalFlow: Double,
    val budgetExpectedFlowUntilNow: Double,
    val budgetCompleition: Double,
    val budgetLeftToPay: Double
) {
    val budgetId get() = budget.id
    val budgetFrequency get() = budget.frequency
    val budgetFrequencyType get() = budget.frequencyType
    val budgetEachClass get() = budget.eachClass
    val budgetType get() = budget.budgetType
    val budgetValue get() = budget.value
    val categoryName get() = category.name

    companion object {
        fun from(
            budget: List<BudgetAndCategoryWithTransactions>,
            currentDate: LocalDate,
            startDate: LocalDate,
            endDate: LocalDate
        ): List<BudgetAndCategoryWithCalculatedData> =
            budget
                .map {
                    val coercedCurrentDate = currentDate.coerceIn(startDate..endDate)
                    val expectedRemainingFlow = BudgetDao.calculateOneBudgetExpectedFlow(
                        it.budget,
                        coercedCurrentDate,
                        endDate
                    )
                    val expectedFlowUntilNow = BudgetDao.calculateOneBudgetExpectedFlow(
                        it.budget,
                        startDate,
                        coercedCurrentDate
                    )
                    val realTotalFlow = BudgetDao.calculateOneBudgetRealFlow(
                        it,
                        startDate,
                        endDate
                    )
                    BudgetAndCategoryWithCalculatedData(
                        budget = it.budget,
                        category = it.category,
                        budgetExpectedTotalFlow = BudgetDao.calculateOneBudgetExpectedFlow(
                            it.budget,
                            startDate,
                            endDate
                        ),
                        budgetExpectedRemainingFlow = expectedRemainingFlow,
                        budgetRealTotalFlow = realTotalFlow,
                        budgetCompleition = BudgetDao.calculateOneBudgetCompleition(
                            it,
                            coercedCurrentDate,
                            startDate,
                            endDate
                        ),
                        budgetExpectedFlowUntilNow = expectedFlowUntilNow,
                        budgetLeftToPay = BudgetDao.calculateLeftToPay(
                            it.budget,
                            expectedRemainingFlow = expectedRemainingFlow,
                            expectedFlowUntilNow = expectedFlowUntilNow,
                            realTotalFlow = realTotalFlow
                        )
                    )
                }
    }
}