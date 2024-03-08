package com.example.gazege.core.entities

import com.example.gazege.core.dao.CategoryDao
import java.time.LocalDate

data class CategoryWithCalculatedData(
    val category: Category,
    val realTotalFlow: Double,
    val realTotalFlowToday: Double,
    val realTotalFlowSeries: Map<LocalDate, Double>,
    val realTotalFlowTodaySeries: Map<LocalDate, Double>,
    val realTotalFlowUntilTodaySeries: Map<LocalDate, Double>
) {
    val id get() = category.id
    val name get() = category.name
    val parentId get() = category.parentId

    companion object {
        fun from(
            categoryWithTransactions: List<CategoryWithTransactions>,
            currentDate: LocalDate,
            startDate: LocalDate,
            endDate: LocalDate
        ): List<CategoryWithCalculatedData> = categoryWithTransactions
            .map {
                val realTotalFlow = CategoryDao.calculateOneCategoryRealFlow(
                    it,
                    startDate,
                    endDate
                )
                val realTotalFlowToday = CategoryDao.calculateOneCategoryRealFlow(
                    it,
                    currentDate,
                    currentDate
                )
                val rangeDate = (startDate..endDate).let { dRange ->
                    generateSequence(dRange.start) { testDate ->
                        testDate.plusDays(1L).takeIf { nextDate -> dRange.contains(nextDate) }
                    }
                }
                val realTotalFlowSeries = rangeDate.associateWith { testDate ->
                    CategoryDao.calculateOneCategoryRealFlow(
                        it,
                        startDate,
                        testDate
                    )
                }
                val realTotalFlowTodaySeries = rangeDate.associateWith { testDate ->
                    CategoryDao.calculateOneCategoryRealFlow(
                        it,
                        testDate,
                        testDate
                    )
                }
                val realTotalFlowUntilTodaySeries = rangeDate.associateWith { testDate ->
                    CategoryDao.calculateOneCategoryRealFlow(
                        it,
                        startDate,
                        testDate
                    )
                }
                CategoryWithCalculatedData(
                    category = it.category,
                    realTotalFlow = realTotalFlow,
                    realTotalFlowToday = realTotalFlowToday,
                    realTotalFlowSeries = realTotalFlowSeries,
                    realTotalFlowTodaySeries = realTotalFlowTodaySeries,
                    realTotalFlowUntilTodaySeries = realTotalFlowUntilTodaySeries
                )
            }
    }
}