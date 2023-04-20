package com.example.gazege.core.dao

import androidx.room.*
import com.example.gazege.core.dateBetween
import com.example.gazege.core.entities.*
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

@Dao
interface BudgetDao {
    @Query(
        """
        SELECT *
        FROM Budget
    """
    )
    fun getAll(): Flow<List<Budget>>

    @Insert
    suspend fun insertAll(vararg budget: Budget): List<Long>

    @Delete
    suspend fun deleteAll(vararg budget: Budget): Int

    @Update
    suspend fun update(budget: Budget)

    companion object {
        fun calculateOneBudgetRealFlow(
            budget: BudgetAndCategoryWithTransactions,
            startDate: LocalDate,
            endDate: LocalDate
        ) = budget.let {
            it.inTransactions
                .filter { trx -> dateBetween(trx.date, startDate, endDate) }
                .sumOf { trx -> trx.amount } -
                    it.outTransactions
                        .filter { trx -> dateBetween(trx.date, startDate, endDate) }
                        .sumOf { trx -> trx.amount }
        }

        fun calculateCantRepetitions(
            budget: Budget,
            startDate: LocalDate,
            endDate: LocalDate
        ): Int = endDate.plusDays(1L)
            .let { endDate1 ->
                when (budget.frequencyType) {
                    FrequencyType.DAILY -> {
                        val cantDaysTotal = ceil(
                            ChronoUnit.DAYS.between(budget.startDate, endDate1)
                                .div(budget.frequency.toDouble())
                        ).toInt()
                        val cantDaysOutInterval = ceil(
                            ChronoUnit.DAYS.between(budget.startDate, startDate)
                                .div(budget.frequency.toDouble())
                        ).toInt()
                        cantDaysTotal - cantDaysOutInterval
                    }
                    FrequencyType.WEEKLY -> {
                        val daysIncluded = (budget.eachClass as WeekDays).days
                        val countRepetitions = { start: LocalDate, end: LocalDate ->
                            val wholeWeeks = ChronoUnit.WEEKS.between(start, end)
                            val wholeDays = ChronoUnit.DAYS.between(start, end)
                            val daysLeft = wholeDays - wholeWeeks * 7
                            val startWeekDay = start.dayOfWeek
                            val weekDaysLeft =
                                (startWeekDay.value until startWeekDay.value + daysLeft)
                                    .map { DayOfWeek.of((it - 1).mod(7) + 1) }
                                    .toSet()
                            val cantWholeWeeks =
                                ceil(wholeWeeks.div(budget.frequency.toDouble())).toInt()
                            val cantWholeRepetitions = cantWholeWeeks * daysIncluded.size
                            val cantDaysLeft = if (wholeWeeks.mod(budget.frequency) == 0) {
                                weekDaysLeft.intersect(daysIncluded).size
                            } else {
                                0
                            }
                            cantDaysLeft + cantWholeRepetitions
                        }

                        val cantRepetitionsTotal = countRepetitions(budget.startDate, endDate1)
                        val cantRepetitionsOutInterval =
                            countRepetitions(budget.startDate, startDate)
                        cantRepetitionsTotal - cantRepetitionsOutInterval
                    }
                    FrequencyType.MONTHLY -> {
                        val countRepetitions = { start: LocalDate, end: LocalDate ->
                            if (start < end) {
                                val wholeMonths = ChronoUnit.MONTHS.between(start, end).toInt()
                                wholeMonths
                            } else {
                                0
                            }
                        }
                        val startMonth = if (startDate.dayOfMonth == 1) {
                            1
                        } else {
                            0
                        }
                        val endMonth =
                            if (endDate.dayOfMonth >= 1 && endDate.monthValue > startDate.monthValue) {
                                1
                            } else {
                                0
                            }

                        val adjustedStartDate = startDate.plusMonths(1).withDayOfMonth(1)
                        val adjustedEndDate = endDate.withDayOfMonth(1)
                        val wholeRepetitions = countRepetitions(adjustedStartDate, adjustedEndDate)
                        wholeRepetitions + startMonth + endMonth
                    }
                }
            }

        fun calculateOneBudgetExpectedFlow(
            budget: Budget,
            startDate: LocalDate,
            endDate: LocalDate
        ) = budget.let {
            val cantRepetitions = if (startDate <= endDate) {
                calculateCantRepetitions(it, startDate, endDate)
            } else {
                0
            }
            it.value * cantRepetitions
        }

        fun calculateOneBudgetCompleition(
            budget: BudgetAndCategoryWithTransactions,
            currentDate: LocalDate,
            startDate: LocalDate,
            endDate: LocalDate
        ): Double {
            val budgetExpectedTotalFlow = calculateOneBudgetExpectedFlow(
                budget.budget,
                startDate,
                endDate
            )
            val realTotalFlow = calculateOneBudgetRealFlow(
                budget,
                startDate,
                currentDate
            )
            val budgetCompleition =
                realTotalFlow.div(budgetExpectedTotalFlow).takeIf { !it.isNaN() }
            return (budgetCompleition ?: 0.0).coerceIn(0.0..1.0)
        }

        fun calculateBudgetCompletion(
            budget: List<BudgetAndCategoryWithCalculatedData>
        ): Double {
            val realTotalFlow = budget.sumOf { it.budgetRealTotalFlow }
            val budgetExpectedTotalFlow = budget.sumOf { it.budgetExpectedTotalFlow }
            val budgetCompletion = realTotalFlow.div(budgetExpectedTotalFlow).takeIf { !it.isNaN() }
            return (budgetCompletion ?: 0.0).coerceIn(0.0..1.0)
        }

        /**
         * Calculates the amount left to pay for a budget, based on the expected remaining flow and the real total flow.
         *
         * @param budget The budget to calculate the amount left to pay for.
         * @param expectedRemainingFlow The expected remaining flow for the budget.
         * @param expectedFlowUntilNow The expected flow until now for the budget.
         * @param realTotalFlow The real total flow for the budget.
         *
         * @return The amount left to pay for the budget.
         */
        fun calculateLeftToPay(
            budget: Budget,
            expectedRemainingFlow: Double,
            expectedFlowUntilNow: Double,
            realTotalFlow: Double
        ): Double = when (budget.budgetType) {
            BudgetType.FIXED -> (expectedFlowUntilNow - realTotalFlow).let { difference ->
                if (expectedFlowUntilNow > 0) {
                    difference.coerceAtLeast(0.0)
                } else {
                    difference.coerceAtMost(0.0)
                }
            }

            BudgetType.VARIABLE -> expectedRemainingFlow
        }
    }
}