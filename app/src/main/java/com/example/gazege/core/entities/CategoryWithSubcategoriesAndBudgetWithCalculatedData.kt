package com.example.gazege.core.entities

import com.example.gazege.core.dao.BudgetDao
import com.example.gazege.core.dao.CategoryDao
import java.time.LocalDate


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

    val expectedTotalFlow = aggregatedBudget.expectedTotalFlow

    val childrenExpectedTotalFlow: Double =
        subCategories.sumOf { it.expectedTotalFlow + it.childrenExpectedTotalFlow }

    val realTotalFlow = category.realTotalFlow

    val childrenRealTotalFlow: Double = subCategories
        .sumOf { it.realTotalFlow + it.childrenRealTotalFlow }

    val completion = CategoryDao.calculateCategoryCompleition(
        realTotalFlow = realTotalFlow,
        expectedTotalFlow = expectedTotalFlow
    )

    val completionWithChildren = CategoryDao.calculateCategoryCompleition(
        realTotalFlow = realTotalFlow + childrenRealTotalFlow,
        expectedTotalFlow = expectedTotalFlow + childrenExpectedTotalFlow
    )

    val expectedFlowUntilToday = aggregatedBudget.expectedFlowUntilToday

    val childrenExpectedFlowUntilToday: Double = subCategories
        .sumOf { it.expectedFlowUntilToday + it.childrenExpectedFlowUntilToday }

    val leftToPayToday: Double = BudgetDao.calculateLeftToPayToday(
        budgetType = category.category.budgetType,
        expectedRemainingFlowTomorrow = aggregatedBudget.expectedFlowFromTomorrow,
        expectedRemainingFlowToday = aggregatedBudget.expectedFlowFromToday,
        expectedFlowUntilNow = aggregatedBudget.expectedFlowUntilToday,
        realTotalFlowToday = category.realTotalFlowToday,
        realTotalFlow = category.realTotalFlow
    )

    val leftToPay: Double = BudgetDao.calculateLeftToPayFromToday(
        budgetType = category.category.budgetType,
        expectedRemainingFlowTomorrow = aggregatedBudget.expectedFlowFromTomorrow,
        leftToPayToday = leftToPayToday,
        expectedTotalFlow = aggregatedBudget.expectedTotalFlow,
        realTotalFlow = category.realTotalFlow
    )

    val childrenLeftToPay: Double = subCategories
        .sumOf {
            val x = it.leftToPay + it.childrenLeftToPay
            x
        }

    val leftToPayTodaySeries: Map<LocalDate, Double> = aggregatedBudget
        .dateRange
        ?.toSequence { it.plusDays(1) }
        ?.associateWith {
            BudgetDao.calculateLeftToPayToday(
                budgetType = category.category.budgetType,
                expectedRemainingFlowTomorrow = aggregatedBudget.expectedFlowFromTomorrowSeries[it]
                    ?: 0.0,
                expectedRemainingFlowToday = aggregatedBudget.expectedFlowFromTodaySeries[it]
                    ?: 0.0,
                expectedFlowUntilNow = aggregatedBudget.expectedFlowUntilTodaySeries[it] ?: 0.0,
                realTotalFlowToday = category.realTotalFlowToday,
                realTotalFlow = category.realTotalFlow
            )
        } ?: emptyMap()

    val leftToPaySeries: Map<LocalDate, Double> = aggregatedBudget
        .dateRange
        ?.toSequence { it.plusDays(1) }
        ?.associateWith {
            BudgetDao.calculateLeftToPayFromToday(
                budgetType = category.category.budgetType,
                expectedRemainingFlowTomorrow = aggregatedBudget.expectedFlowFromTomorrowSeries[it]
                    ?: 0.0,
                leftToPayToday = leftToPayTodaySeries[it] ?: 0.0,
                expectedTotalFlow = aggregatedBudget.expectedTotalFlow,
                realTotalFlow = category.realTotalFlowSeries[it] ?: 0.0
            )
        } ?: emptyMap()

    val pastForecast: Map<LocalDate, Double> = if (dateRange != null && currentDate != null) {
        category.realTotalFlowUntilTodaySeries
            .filterKeys { (dateRange.start..currentDate).contains(it) }
    } else {
        emptyMap()
    }

    val childrenPastForecast: Map<LocalDate, Double> = subCategories
        .mapSumOf { it.pastForecast + it.childrenPastForecast }

    val futureForecast: Map<LocalDate, Double> = if (dateRange != null && currentDate != null) {
        val lastPastForecast = pastForecast[currentDate] ?: 0.0
        mapOf(currentDate to lastPastForecast).plus(
            (leftToPayTodaySeries[currentDate] ?: 0.0).let { leftToPayLastDay ->
                aggregatedBudget.expectedFlowTodaySeries
                    .filterKeys { (currentDate.plusDays(1)..dateRange.endInclusive).contains(it) }
                    .toList()
                    .runningReduce { (_, valueAcc), (localDate, value) ->
                        localDate to valueAcc + value
                    }.associate { (localDate, value) ->
                        localDate to value + leftToPayLastDay
                    }
            }
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