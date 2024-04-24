package com.jmml.gazege.core.entities

import com.jmml.gazege.core.dao.CategoryDao
import com.jmml.zoo.extensions.closedrange.plus
import com.jmml.zoo.extensions.closedrange.toSequence
import com.jmml.zoo.extensions.list.mapSumOf
import com.jmml.zoo.extensions.map.plus
import java.time.LocalDate
import java.util.SortedMap
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.withSign


data class CategoryWithSubcategoriesAndBudgetWithCalculatedData(
    val category: CategoryWithCalculatedData,
    val budget: List<BudgetWithCalculatedData>,
    val subCategories: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    val dateRange: ClosedRange<LocalDate>?
) {
    val aggregatedBudget: BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData =
        budget
            .map { BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData.from(it) }
            .sumOrNull()
            ?: BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData()
    val childrenAggregatedBudget: BudgetWithCalculatedData.AggregatedBudgetWithCalculatedData =
        subCategories
            .map { it.aggregatedBudget + it.childrenAggregatedBudget }
            .sumOrNull()
            ?: BudgetWithCalculatedData.ZeroAggregatedBudgetWithCalculatedData()

    val currentDate = aggregatedBudget.currentDate

    val expectedFlowTodaySeries: Map<LocalDate, Double> = aggregatedBudget
        .expectedFlowTodaySeries

    val realTotalFlowTodaySeries: Map<LocalDate, Double> = category
        .realTotalFlowTodaySeries

    val accumulatedExpectedFlowTodaySeries: Map<LocalDate, Double> = expectedFlowTodaySeries
        .toSortedMap()
        .runningReduce { acc, value -> acc + value }

    val accumulatedRealTotalFlowTodaySeries: Map<LocalDate, Double> = realTotalFlowTodaySeries
        .toSortedMap()
        .runningReduce { acc, value -> acc + value }

    val forecastedTransactionsTimeSeries: Map<LocalDate, Double> =
        dateRange
            ?.let { dateRange ->
                var acc = 0.0
                dateRange
                    .toSequence { it.plusDays(1) }
                    .associateWith { day ->
                        val sign = aggregatedBudget.expectedFlowUntilTodaySeries[day]?.sign ?: 0.0
                        val dVTS = (expectedFlowTodaySeries[day] ?: 0.0) * sign
                        val tTS = (realTotalFlowTodaySeries[day] ?: 0.0) * sign
                        val cDVTS = (accumulatedExpectedFlowTodaySeries[day] ?: 0.0) * sign
                        val cTTS = (accumulatedRealTotalFlowTodaySeries[day] ?: 0.0) * sign

                        val value = when (category.category.budgetType) {
                            BudgetType.VARIABLE ->
                                if (day < currentDate) {
                                    0.0
                                } else if (day > currentDate) {
                                    dVTS
                                } else {
                                    max(0.0, dVTS - tTS)
                                }

                            BudgetType.FIXED ->
                                if (day < currentDate) {
                                    0.0
                                } else {
                                    max(0.0, cDVTS - (cTTS + acc))
                                }
                        }
                        acc += value
                        value
                    }
                    .mapValues { (day, value) ->
                        value.withSign(aggregatedBudget.expectedFlowUntilTodaySeries[day] ?: 0.0)
                    }
            }
            ?: emptyMap()

    val realTotalFlow = category.realTotalFlow

    val childrenRealTotalFlow: Double = subCategories
        .sumOf { it.realTotalFlow + it.childrenRealTotalFlow }

    val leftToPayTodaySeries: Map<LocalDate, Double> get() = forecastedTransactionsTimeSeries

    val leftToPaySeries: Map<LocalDate, Double> = forecastedTransactionsTimeSeries
        .toSortedMap { date1, date2 -> date1.compareTo(date2) * -1 }
        .runningReduce { acc, value -> acc + value }
        .toSortedMap()
        .toMap()

    val pastForecast: Map<LocalDate, Double> = if (dateRange != null && currentDate != null) {
        accumulatedRealTotalFlowTodaySeries
            .filterKeys { (dateRange.start..currentDate).contains(it) }
    } else {
        emptyMap()
    }

    val childrenPastForecast: Map<LocalDate, Double> = subCategories
        .mapSumOf { it.pastForecast + it.childrenPastForecast }

    val futureForecast: Map<LocalDate, Double> = if (dateRange != null && currentDate != null) {
        val lastPastForecast = pastForecast[currentDate] ?: 0.0
        mapOf(currentDate to lastPastForecast).plus(
            forecastedTransactionsTimeSeries
                .toSortedMap()
                .runningReduce { acc, value -> acc + value }
                .filterKeys { it > currentDate }
                .mapValues { (_, value) -> value + lastPastForecast }
        )
    } else {
        emptyMap()
    }

    val childrenFutureForecast: Map<LocalDate, Double> = subCategories
        .mapSumOf { it.futureForecast + it.childrenFutureForecast }

    val childrenDateRange: ClosedRange<LocalDate>? =
        subCategories
            .map {
                it.dateRange + it.childrenDateRange
            }
            .reduceOrNull { acc, closedRange ->
                acc + closedRange
            }

    val expectedTotalFlow = futureForecast[dateRange?.endInclusive] ?: 0.0

    val childrenExpectedTotalFlow: Double =
        subCategories.sumOf { it.expectedTotalFlow + it.childrenExpectedTotalFlow }

    val completion = CategoryDao.calculateCategoryCompleition(
        realTotalFlow = realTotalFlow,
        expectedTotalFlow = aggregatedBudget.expectedTotalFlow
    )

    val completionWithChildren = CategoryDao.calculateCategoryCompleition(
        realTotalFlow = realTotalFlow + childrenRealTotalFlow,
        expectedTotalFlow = aggregatedBudget.expectedTotalFlow + childrenAggregatedBudget.expectedTotalFlow
    )

    val leftToPayToday: Double = forecastedTransactionsTimeSeries[currentDate] ?: 0.0

    val leftToPay: Double = forecastedTransactionsTimeSeries.values.sum()

    val childrenLeftToPay: Double = subCategories
        .sumOf { it.leftToPay + it.childrenLeftToPay }

    fun <K, V> SortedMap<K, V>.runningReduce(operation: (acc: V, value: V) -> V): Map<K, V> =
        values
            .runningReduce(operation)
            .zip(keys)
            .associate { it.second to it.first }

    companion object {
        fun from(
            budgetWithCalculatedDataAndCategory: List<BudgetWithCalculatedDataAndCategory>,
            categoriesWithSubcategories: List<CategoryWithSubCategories>,
            categoriesWithCalculatedData: Map<Int, CategoryWithCalculatedData>
        ): List<CategoryWithSubcategoriesAndBudgetWithCalculatedData> =
            budgetWithCalculatedDataAndCategory
                .groupBy { it.category.id }
                .let { mappedBudget ->
                    categoriesWithSubcategories.mapNotNull {
                        val budgetList = mappedBudget[it.category.id]
                            ?.map { budget -> budget.toBudgetWithCalculatedData() }
                            ?: emptyList()
                        val categoryWithCalculatedData =
                            categoriesWithCalculatedData[it.category.id]
                        if (categoryWithCalculatedData != null) {
                            val subCategories = from(
                                budgetWithCalculatedDataAndCategory,
                                it.subCategories,
                                categoriesWithCalculatedData
                            )
                            CategoryWithSubcategoriesAndBudgetWithCalculatedData(
                                category = categoryWithCalculatedData,
                                budget = budgetList,
                                subCategories = subCategories,
                                dateRange = budgetList.fold(null) { acc: ClosedRange<LocalDate>?, budgetWithCalculatedData ->
                                    if (acc != null) {
                                        acc + budgetWithCalculatedData.dateRange
                                    } else {
                                        budgetWithCalculatedData.dateRange
                                    }
                                }
                            )
                        } else {
                            null
                        }
                    }
                }
    }
}

fun List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>.recursiveFirstOrNull(predicate: (CategoryWithSubcategoriesAndBudgetWithCalculatedData) -> Boolean):
        CategoryWithSubcategoriesAndBudgetWithCalculatedData? =
    this.firstOrNull(predicate) ?: this.firstNotNullOfOrNull {
        it.subCategories.recursiveFirstOrNull(predicate)
    }

fun CategoryWithSubcategoriesAndBudgetWithCalculatedData.recursiveFirstOrNull(predicate: (CategoryWithSubcategoriesAndBudgetWithCalculatedData) -> Boolean): CategoryWithSubcategoriesAndBudgetWithCalculatedData? =
    if (predicate(this)) {
        this
    } else {
        this.subCategories.recursiveFirstOrNull(predicate)
    }