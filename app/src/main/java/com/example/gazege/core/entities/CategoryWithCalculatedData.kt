package com.example.gazege.core.entities

import com.example.gazege.core.dao.CategoryDao
import java.time.LocalDate

data class CategoryWithCalculatedData(
    val category: Category,
    val realTotalFlow: Double
) {
    val id get() = category.id
    val name get() = category.name
    val parentId get() = category.parentId

    companion object {
        fun from(
            categoryWithTransactions: List<CategoryWithTransactions>,
            startDate: LocalDate,
            endDate: LocalDate
        ): List<CategoryWithCalculatedData> = categoryWithTransactions
            .map {
                val realTotalFlow = CategoryDao.calculateOneCategoryRealFlow(
                    it,
                    startDate,
                    endDate
                )
                CategoryWithCalculatedData(
                    it.category,
                    realTotalFlow
                )
            }
    }
}