package com.example.gazege.core.entities

import com.example.gazege.core.dao.BudgetDao
import java.time.LocalDate

data class BudgetWithCalculatedData(
    val budget: Budget,
    val dateRange: ClosedRange<LocalDate>,
    val currentDate: LocalDate,
    val expectedTotalFlow: Double,
    val expectedFlowUntilToday: Double,
    val expectedFlowFromToday: Double,
    val expectedFlowFromTomorrow: Double,
    val expectedFlowUntilTodaySeries: Map<LocalDate, Double>,
    val expectedFlowFromTodaySeries: Map<LocalDate, Double>,
    val expectedFlowFromTomorrowSeries: Map<LocalDate, Double>,
    val expectedFlowTodaySeries: Map<LocalDate, Double>
) {
    open class AggregatedBudgetWithCalculatedData(
        val dateRange: ClosedRange<LocalDate>?,
        val currentDate: LocalDate?,
        val expectedTotalFlow: Double,
        val expectedFlowUntilToday: Double,
        val expectedFlowFromToday: Double,
        val expectedFlowFromTomorrow: Double,
        val expectedFlowUntilTodaySeries: Map<LocalDate, Double>,
        val expectedFlowFromTodaySeries: Map<LocalDate, Double>,
        val expectedFlowFromTomorrowSeries: Map<LocalDate, Double>,
        val expectedFlowTodaySeries: Map<LocalDate, Double>
    ) {
        operator fun plus(other: AggregatedBudgetWithCalculatedData) =
            AggregatedBudgetWithCalculatedData(
                dateRange = dateRange + other.dateRange,
                expectedTotalFlow = (expectedTotalFlow + other.expectedTotalFlow),
                expectedFlowUntilToday = expectedFlowUntilToday + other.expectedFlowUntilToday,
                expectedFlowFromToday = expectedFlowFromToday + other.expectedFlowFromToday,
                expectedFlowFromTomorrow = expectedFlowFromTomorrow + other.expectedFlowFromTomorrow,
                expectedFlowUntilTodaySeries = expectedFlowUntilTodaySeries.merge(other.expectedFlowUntilTodaySeries) { first, second ->
                    (first ?: 0.0) + (second ?: 0.0)
                },
                expectedFlowFromTodaySeries = expectedFlowFromTodaySeries.merge(other.expectedFlowFromTodaySeries) { first, second ->
                    (first ?: 0.0) + (second ?: 0.0)
                },
                expectedFlowFromTomorrowSeries = expectedFlowFromTomorrowSeries.merge(other.expectedFlowFromTomorrowSeries) { first, second ->
                    (first ?: 0.0) + (second ?: 0.0)
                },
                currentDate = if (currentDate != null && other.currentDate != null) {
                    currentDate.takeIf { currentDate == other.currentDate }
                } else {
                    currentDate ?: other.currentDate
                },
                expectedFlowTodaySeries = expectedFlowTodaySeries + other.expectedFlowTodaySeries
            )

        operator fun plus(other: BudgetWithCalculatedData) = this + from(other)

        companion object {
            fun from(budgetWithCalculatedData: BudgetWithCalculatedData) =
                budgetWithCalculatedData.run {
                    AggregatedBudgetWithCalculatedData(
                        dateRange = dateRange,
                        expectedTotalFlow = expectedTotalFlow,
                        expectedFlowUntilToday = expectedFlowUntilToday,
                        expectedFlowFromToday = expectedFlowFromToday,
                        expectedFlowFromTomorrow = expectedFlowFromTomorrow,
                        expectedFlowUntilTodaySeries = expectedFlowUntilTodaySeries,
                        expectedFlowFromTodaySeries = expectedFlowFromTodaySeries,
                        expectedFlowFromTomorrowSeries = expectedFlowFromTomorrowSeries,
                        currentDate = currentDate,
                        expectedFlowTodaySeries = expectedFlowTodaySeries
                    )
                }
        }
    }

    class ZeroAggregatedBudgetWithCalculatedData : AggregatedBudgetWithCalculatedData(
        dateRange = null,
        expectedTotalFlow = 0.0,
        expectedFlowUntilToday = 0.0,
        expectedFlowFromToday = 0.0,
        expectedFlowFromTomorrow = 0.0,
        expectedFlowUntilTodaySeries = emptyMap(),
        expectedFlowFromTodaySeries = emptyMap(),
        expectedFlowFromTomorrowSeries = emptyMap(),
        currentDate = null,
        expectedFlowTodaySeries = emptyMap()
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

                val dateSeries = (startDate..endDate).let { dateRange ->
                    generateSequence(startDate) { testDate ->
                        if (dateRange.contains(testDate)) {
                            testDate.plusDays(1)
                        } else {
                            null
                        }
                    }
                }
                val expectedFlowUntilNowSeries = dateSeries.map { testDate ->
                    Pair(
                        testDate, BudgetDao.calculateOneBudgetExpectedFlow(
                            it.budget,
                            startDate,
                            testDate
                        )
                    )
                }.toMap()
                val expectedFlowFromTomorrowSeries = dateSeries.map { testDate ->
                    Pair(
                        testDate, BudgetDao.calculateOneBudgetExpectedFlow(
                            it.budget,
                            testDate.plusDays(1L).coerceIn(startDate, endDate),
                            endDate
                        )
                    )
                }.toMap()
                val expectedFlowFromTodaySeries = dateSeries.map { testDate ->
                    Pair(
                        testDate, BudgetDao.calculateOneBudgetExpectedFlow(
                            it.budget,
                            testDate,
                            endDate
                        )
                    )
                }.toMap()
                val expectedFlowTodaySeries = dateSeries.map { testDate ->
                    testDate to BudgetDao.calculateOneBudgetExpectedFlow(
                        it.budget,
                        testDate,
                        testDate
                    )
                }.toMap()

                val expectedFlowUntilNow = expectedFlowUntilNowSeries[coercedToday] ?: 0.0
                val expectedFlowFromTomorrow = expectedFlowFromTomorrowSeries[coercedToday] ?: 0.0
                val expectedFlowFromToday = expectedFlowFromTodaySeries[coercedToday] ?: 0.0
                val expectedTotalFlow = expectedFlowUntilNowSeries[endDate] ?: 0.0

                BudgetWithCalculatedData(
                    budget = it.budget,
                    expectedTotalFlow = expectedTotalFlow,
                    expectedFlowUntilToday = expectedFlowUntilNow,
                    expectedFlowFromTomorrow = expectedFlowFromTomorrow,
                    expectedFlowFromToday = expectedFlowFromToday,
                    expectedFlowUntilTodaySeries = expectedFlowUntilNowSeries,
                    expectedFlowFromTomorrowSeries = expectedFlowFromTomorrowSeries,
                    expectedFlowFromTodaySeries = expectedFlowFromTodaySeries,
                    dateRange = startDate..endDate,
                    currentDate = currentDate,
                    expectedFlowTodaySeries = expectedFlowTodaySeries
                )
            }
    }
}

fun <K, U, V, W> Map<K, U>.merge(
    other: Map<K, V>,
    merger: (first: U?, second: V?) -> W
): Map<K, W> =
    (this.keys + other.keys).associateWith { merger(this[it], other[it]) }

operator fun <K> Map<K, Double>.plus(other: Map<K, Double>) = merge(other) { first, second ->
    (first ?: 0.0) + (second ?: 0.0)
}

fun <E> List<E>.mapSumOf(function: (E) -> Map<LocalDate, Double>): Map<LocalDate, Double> =
    fold(emptyMap()) { acc, e -> acc + function(e) }

operator fun <T : Comparable<T>> ClosedRange<T>?.plus(other: ClosedRange<T>?): ClosedRange<T>? =
    if (this != null && other != null) {
        minOf(start, other.start)..maxOf(endInclusive, other.endInclusive)
    } else {
        this ?: other
    }

fun <T : Comparable<T>> ClosedRange<T>.toSequence(next: (T) -> T): Sequence<T> =
    generateSequence(start) { next(it).takeIf { current -> contains(current) } }

fun List<BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData>.sumOrNull() = this
    .takeIf { it.isNotEmpty() }
    ?.let {
        this.reduceOrNull { acc, new -> acc + new }
            ?: BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData()
    }