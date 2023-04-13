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
        private fun calculateOneBudgetRealFlow(
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

        private fun calculateOneBudgetExpectedFlow(
            budget: Budget,
            startDate: LocalDate,
            endDate: LocalDate
        ) = budget.let {
            val cantRepetitions = calculateCantRepetitions(it, startDate, endDate)
            it.value * cantRepetitions
        }

        fun calculateOneBudgetCompleition(
            budget: BudgetAndCategoryWithTransactions,
            currentDate: LocalDate,
            startDate: LocalDate,
            endDate: LocalDate
        ): Double {
            val expectedFLowEnd = calculateOneBudgetExpectedFlow(
                budget.budget,
                startDate,
                endDate
            )
            val realFlowUntilNow = calculateOneBudgetRealFlow(
                budget,
                startDate,
                currentDate
            )
            return realFlowUntilNow.div(expectedFLowEnd)
        }
    }
}