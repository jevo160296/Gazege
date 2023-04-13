package com.example.gazege.core.dao

import androidx.room.*
import com.example.gazege.core.entities.Category
import kotlinx.coroutines.flow.Flow

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
}