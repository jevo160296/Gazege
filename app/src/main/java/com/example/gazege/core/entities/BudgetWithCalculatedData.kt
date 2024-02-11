package com.example.gazege.core.entities

import com.example.gazege.core.dao.BudgetDao
import java.time.LocalDate

data class BudgetWithCalculatedData(
    val budget: Budget,
    val expectedTotalFlow: Double,
    val expectedRemainingFlowFromToday: Double,
    val expectedFlowUntilNow: Double,
    val leftToPayFromToday: Double,
    val leftToPayToday: Double
) {
    open class AggregatedBudgetWithCalculatedData(
        val expectedTotalFlow: Double,
        val expectedRemainingFlow: Double,
        val expectedFlowUntilNow: Double,
        val leftToPayFromToday: Double,
        val leftToPayToday: Double
    ) {
        operator fun plus(other: AggregatedBudgetWithCalculatedData) =
            AggregatedBudgetWithCalculatedData(
                expectedTotalFlow = (expectedTotalFlow + other.expectedTotalFlow),
                expectedRemainingFlow = expectedRemainingFlow + other.expectedRemainingFlow,
                expectedFlowUntilNow = expectedFlowUntilNow + other.expectedFlowUntilNow,
                leftToPayFromToday = leftToPayFromToday + other.leftToPayFromToday,
                leftToPayToday = leftToPayToday + other.leftToPayToday
            )

        operator fun plus(other: BudgetWithCalculatedData) = this + from(other)

        companion object {
            fun from(budgetWithCalculatedData: BudgetWithCalculatedData) =
                budgetWithCalculatedData.run {
                    AggregatedBudgetWithCalculatedData(
                        expectedTotalFlow,
                        expectedRemainingFlowFromToday,
                        expectedFlowUntilNow,
                        leftToPayFromToday,
                        leftToPayToday
                    )
                }
        }
    }

    class ZeroAggregatedBudgetWithCalculatedData : AggregatedBudgetWithCalculatedData(
        expectedTotalFlow = 0.0,
        expectedRemainingFlow = 0.0,
        expectedFlowUntilNow = 0.0,
        leftToPayFromToday = 0.0,
        leftToPayToday = 0.0
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
            today: LocalDate,
            endDate: LocalDate
        ): List<BudgetWithCalculatedData> = budget
            .map {
                val coercedCurrentDate = currentDate.coerceIn(startDate..endDate)
                val expectedRemainingFlowTomorrow = BudgetDao.calculateOneBudgetExpectedFlow(
                    it.budget,
                    coercedCurrentDate.plusDays(1L),
                    endDate
                )
                val expectedRemainingFlowToday = BudgetDao.calculateOneBudgetExpectedFlow(
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
                val realTotalFlowToday = BudgetDao.calculateOneBudgetRealFlow(
                    it,
                    today,
                    today
                )
                val leftToPayToday = BudgetDao.calculateLeftToPayToday(
                    it.budget,
                    expectedRemainingFlowTomorrow = expectedRemainingFlowTomorrow,
                    expectedRemainingFlowToday = expectedRemainingFlowToday,
                    realTotalFlowToday = realTotalFlowToday,
                    expectedFlowUntilNow = expectedFlowUntilNow,
                    realTotalFlow = realTotalFlow
                )
                BudgetWithCalculatedData(
                    budget = it.budget,
                    expectedTotalFlow = BudgetDao.calculateOneBudgetExpectedFlow(
                        it.budget,
                        startDate,
                        endDate
                    ),
                    expectedRemainingFlowFromToday = expectedRemainingFlowToday,
                    expectedFlowUntilNow = expectedFlowUntilNow,
                    leftToPayFromToday = BudgetDao.calculateLeftToPayFromToday(
                        it.budget,
                        expectedFlowUntilNow = expectedFlowUntilNow,
                        realTotalFlow = realTotalFlow,
                        expectedRemainingFlowTomorrow = expectedRemainingFlowTomorrow,
                        leftToPayToday = leftToPayToday
                    ),
                    leftToPayToday = leftToPayToday
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