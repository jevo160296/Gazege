package com.example.gazege.core.entities

import com.example.gazege.core.dao.BudgetDao
import java.time.LocalDate

data class BudgetWithCalculatedData(
    val budget: Budget,
    val expectedTotalFlow: Double,
    val expectedRemainingFlow: Double,
    val realTotalFlow: Double,
    val expectedFlowUntilNow: Double,
    val compleition: Double,
    val leftToPay: Double
) {
    open class AggregatedBudgetWithCalculatedData(
        val expectedTotalFlow: Double,
        val expectedRemainingFlow: Double,
        val realTotalFlow: Double,
        val expectedFlowUntilNow: Double,
        val compleition: Double,
        val leftToPay: Double
    ) {
        operator fun plus(other: AggregatedBudgetWithCalculatedData) =
            (realTotalFlow + other.realTotalFlow).let { realTotalFlow ->
                (expectedTotalFlow + other.expectedTotalFlow).let { expectedTotalFlow ->
                    AggregatedBudgetWithCalculatedData(
                        expectedTotalFlow = expectedTotalFlow,
                        expectedRemainingFlow = expectedRemainingFlow + other.expectedRemainingFlow,
                        realTotalFlow = realTotalFlow,
                        expectedFlowUntilNow = expectedFlowUntilNow + other.expectedFlowUntilNow,
                        leftToPay = leftToPay + other.leftToPay,
                        compleition = BudgetDao.calculateBudgetCompleition(
                            realTotalFlow = realTotalFlow,
                            expectedTotalFlow = expectedTotalFlow
                        )
                    )
                }
            }

        operator fun plus(other: BudgetWithCalculatedData) = this + from(other)

        companion object {
            fun from(budgetWithCalculatedData: BudgetWithCalculatedData) =
                budgetWithCalculatedData.run {
                    AggregatedBudgetWithCalculatedData(
                        expectedTotalFlow,
                        expectedRemainingFlow,
                        realTotalFlow,
                        expectedFlowUntilNow,
                        compleition,
                        leftToPay
                    )
                }
        }
    }

    class ZeroAggregatedBudgetWithCalculatedData : AggregatedBudgetWithCalculatedData(
        expectedTotalFlow = 0.0,
        expectedRemainingFlow = 0.0,
        realTotalFlow = 0.0,
        expectedFlowUntilNow = 0.0,
        compleition = Double.NaN,
        leftToPay = 0.0
    )

    operator fun plus(other: BudgetWithCalculatedData) =
        AggregatedBudgetWithCalculatedData.from(this) +
                AggregatedBudgetWithCalculatedData.from(other)

    operator fun plus(other: AggregatedBudgetWithCalculatedData) =
        AggregatedBudgetWithCalculatedData.from(this) + other

    companion object {
        fun from(
            budget: List<BudgetAndCategoryWithTransactions>,
            currentDate: LocalDate,
            startDate: LocalDate,
            endDate: LocalDate
        ): List<BudgetWithCalculatedData> = budget
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
                BudgetWithCalculatedData(
                    budget = it.budget,
                    expectedTotalFlow = BudgetDao.calculateOneBudgetExpectedFlow(
                        it.budget,
                        startDate,
                        endDate
                    ),
                    expectedRemainingFlow = expectedRemainingFlow,
                    realTotalFlow = realTotalFlow,
                    compleition = BudgetDao.calculateOneBudgetCompleition(
                        it,
                        coercedCurrentDate,
                        startDate,
                        endDate
                    ),
                    expectedFlowUntilNow = expectedFlowUntilNow,
                    leftToPay = BudgetDao.calculateLeftToPay(
                        it.budget,
                        expectedRemainingFlow = expectedRemainingFlow,
                        expectedFlowUntilNow = expectedFlowUntilNow,
                        realTotalFlow = realTotalFlow
                    )
                )
            }
    }
}

fun List<BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData>.sumOrNull() = this
    .takeIf { it.isNotEmpty() }
    ?.let {
        this.reduceOrNull { acc, new -> acc + new }
            ?: BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData()
    }