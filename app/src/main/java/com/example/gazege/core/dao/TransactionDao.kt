package com.example.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM `Transaction`")
    fun getAll(): Flow<List<TransactionAndAccounts>>

    @Insert
    suspend fun insertAll(vararg transactions: Transaction): List<Long>

    @Delete
    suspend fun delete(transaction: Transaction): Int
}