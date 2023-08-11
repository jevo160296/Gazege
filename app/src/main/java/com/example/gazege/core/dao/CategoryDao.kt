package com.example.gazege.core.dao

import androidx.room.*
import com.example.gazege.core.dateBetween
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithTransactions
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CategoryDao {
    @Query(
        """
        SELECT *
        FROM Category
    """
    )
    fun getAll(): Flow<List<Category>>

    @Insert
    suspend fun insertAll(vararg categories: Category): List<Long>

    @Delete
    suspend fun deleteAll(vararg category: Category): Int

    @Update
    suspend fun update(account: Category)

    @Update
    suspend fun updateAll(vararg category: Category)

    @Query(
        "DELETE FROM Category"
    )
    suspend fun deleteAll()

    companion object {
        fun calculateOneCategoryRealFlow(
            categoryWithTransactions: CategoryWithTransactions,
            startDate: LocalDate,
            endDate: LocalDate
        ) = categoryWithTransactions.let {
            it.inTransactions
                .filter { trx -> dateBetween(trx.date, startDate, endDate) }
                .sumOf { trx -> trx.amount } -
                    it.outTransactions
                        .filter { trx -> dateBetween(trx.date, startDate, endDate) }
                        .sumOf { trx -> trx.amount }
        }

        fun calculateCategoryCompleition(
            realTotalFlow: Double,
            expectedTotalFlow: Double
        ) =
            realTotalFlow
                .div(expectedTotalFlow)
                .takeIf { !it.isNaN() }
                .let { it ?: 0.0 }
                .coerceIn(0.0..1.0)
    }
}