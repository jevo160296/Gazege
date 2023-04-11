package com.example.gazege.core.dao

import androidx.room.*
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.Category
import kotlinx.coroutines.flow.Flow

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
    suspend fun update(account: Category)
}