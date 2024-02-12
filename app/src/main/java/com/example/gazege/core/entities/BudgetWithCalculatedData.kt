package com.example.gazege.core.entities

import com.example.gazege.core.dao.BudgetDao
import java.time.LocalDate

data class BudgetWithCalculatedData(
    val budget: Budget,
    val expectedTotalFlow: Double,
    val expectedFlowUntilToday: Double,
    val expectedFlowUntilTomorrow: Double,
    val expectedFlowFromToday: Double,
    val expectedFlowFromTomorrow: Double
) {
    open class AggregatedBudgetWithCalculatedData(
        val expectedTotalFlow: Double,
        val expectedFlowUntilToday: Double,
        val expectedFlowUntilTomorrow: Double,
        val expectedFlowFromToday: Double,
        val expectedFlowFromTomorrow: Double
    ) {
        operator fun plus(other: AggregatedBudgetWithCalculatedData) =
            AggregatedBudgetWithCalculatedData(
                expectedTotalFlow = (expectedTotalFlow + other.expectedTotalFlow),
                expectedFlowUntilToday = expectedFlowUntilToday + other.expectedFlowUntilToday,
                expectedFlowUntilTomorrow = expectedFlowUntilTomorrow + other.expectedFlowUntilTomorrow,
                expectedFlowFromToday = expectedFlowFromToday + other.expectedFlowFromToday,
                expectedFlowFromTomorrow = expectedFlowFromTomorrow + other.expectedFlowFromTomorrow
            )

        operator fun plus(other: BudgetWithCalculatedData) = this + from(other)

        companion object {
            fun from(budgetWithCalculatedData: BudgetWithCalculatedData) =
                budgetWithCalculatedData.run {
                    AggregatedBudgetWithCalculatedData(
                        expectedTotalFlow,
                        expectedFlowUntilToday,
                        expectedFlowUntilTomorrow,
                        expectedFlowFromToday,
                        expectedFlowFromTomorrow
                    )
                }
        }
    }

    class ZeroAggregatedBudgetWithCalculatedData : AggregatedBudgetWithCalculatedData(
        expectedTotalFlow = 0.0,
        expectedFlowUntilToday = 0.0,
        expectedFlowUntilTomorrow = 0.0,
        expectedFlowFromToday = 0.0,
        expectedFlowFromTomorrow = 0.0
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
                val coercedToday = currentDate.coerceIn(startDate..endDate)
                val coercedTomorrow = coercedToday.plusDays(1L).coerceIn(startDate..endDate)
                val expectedFlowUntilNow = BudgetDao.calculateOneBudgetExpectedFlow(
                    it.budget,
                    startDate,
                    coercedToday
                )
                val expectedFlowUntilTomorrow = BudgetDao.calculateOneBudgetExpectedFlow(
                    it.budget,
                    startDate,
                    coercedTomorrow
                )
                val expectedFlowFromTomorrow = BudgetDao.calculateOneBudgetExpectedFlow(
                    it.budget,
                    coercedTomorrow,
                    endDate
                )
                val expectedFlowFromToday = BudgetDao.calculateOneBudgetExpectedFlow(
                    it.budget,
                    coercedToday,
                    endDate
                )
                BudgetWithCalculatedData(
                    budget = it.budget,
                    expectedTotalFlow = BudgetDao.calculateOneBudgetExpectedFlow(
                        it.budget,
                        startDate,
                        endDate
                    ),
                    expectedFlowUntilToday = expectedFlowUntilNow,
                    expectedFlowUntilTomorrow = expectedFlowUntilTomorrow,
                    expectedFlowFromTomorrow = expectedFlowFromTomorrow,
                    expectedFlowFromToday = expectedFlowFromToday
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