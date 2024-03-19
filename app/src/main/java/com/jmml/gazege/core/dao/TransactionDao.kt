package com.jmml.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionAndAccounts
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
                "WHERE " +
                "(:startDate is null OR date >= :startDate) AND " +
                "(:endDate is null OR date <= :endDate) " +
                "ORDER BY date DESC, id DESC"
    )
    fun getAll(startDate: LocalDate?, endDate: LocalDate?): Flow<List<Transaction>>

    @Insert
    suspend fun insertAll(vararg transactions: Transaction): List<Long>

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction): Int

    @Query(
        "DELETE FROM `Transaction`"
    )
    suspend fun deleteAll()
}