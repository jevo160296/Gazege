package com.jmml.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithTransactions
import com.jmml.zoo.extensions.localdate.isBetween
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
                .filter { trx -> trx.date.isBetween(startDate, endDate) }
                .sumOf { trx -> trx.amount } -
                    it.outTransactions
                        .filter { trx -> trx.date.isBetween(startDate, endDate) }
                        .sumOf { trx -> trx.amount }
        }

        fun calculateCategoryCompleition(
            realTotalFlow: Double,
            expectedTotalFlow: Double
        ) =
            realTotalFlow
                .div(expectedTotalFlow)
                .takeIf { !it.isNaN() }
                ?: 0.0

        fun calculateAhorroExceso(realTotalFlow: Double, initialExpectation: Double): Double =
            realTotalFlow - initialExpectation
        fun calculateExpectedTotalFlow(realFlow: Double, leftToPay: Double): Double =
            realFlow + leftToPay
    }
}