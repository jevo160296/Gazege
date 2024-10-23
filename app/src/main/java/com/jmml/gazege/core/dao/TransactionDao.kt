package com.jmml.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionAndDetailsAndAccounts
import com.jmml.gazege.core.entities.TransactionDetails
import com.jmml.gazege.core.entities.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT *
        FROM `Transaction`
    """
    )
    fun getAll(): Flow<List<TransactionAndDetailsAndAccounts>>

    @androidx.room.Transaction
    @Query(
        """
        SELECT *
        FROM `Transaction`
        WHERE (:startDate is null OR date >= :startDate) AND (:endDate is null OR date <= :endDate)
        ORDER BY date DESC, id DESC
    """
    )
    fun getAll(startDate: LocalDate?, endDate: LocalDate?): Flow<List<TransactionWithDetails>>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Insert
    suspend fun insert(transactionDetails: TransactionDetails): Long

    @Insert
    suspend fun insertAll(vararg transactions: Transaction): List<Long>

    @Insert
    suspend fun insertAll(vararg transactionDetails: TransactionDetails): List<Long>

    @Update
    suspend fun update(transaction: Transaction)

    @Update
    suspend fun update(transactionDetails: TransactionDetails)

    @Delete
    suspend fun delete(transaction: Transaction): Int

    @Delete
    suspend fun delete(transactionDetails: TransactionDetails): Int

    @Query(
        "DELETE FROM `Transaction`"
    )
    suspend fun deleteAllTransactions()

    @Query(
        "DELETE FROM TransactionDetails"
    )
    suspend fun deleteAllTransactionsDetails()

    suspend fun deleteAll() {
        deleteAllTransactionsDetails()
        deleteAllTransactions()
    }
}