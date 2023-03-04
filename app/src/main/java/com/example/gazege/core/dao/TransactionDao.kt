package com.example.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    @Query("SELECT * FROM `Transaction`")
    fun getAll(): Flow<List<TransactionAndAccounts>>

    @androidx.room.Transaction
    @Query(
        "SELECT *" +
                "FROM `Transaction`" +
                "WHERE date >= :startDate AND date <= :endDate"
    )
    fun getAll(startDate: LocalDate, endDate: LocalDate): Flow<List<TransactionAndAccounts>>

    @Insert
    suspend fun insertAll(vararg transactions: Transaction): List<Long>

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction): Int
}