package com.jmml.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.jmml.gazege.core.entities.ExtendedTransaction
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

    @androidx.room.Transaction
    @Query(
        """
        SELECT 
            T.id,T.sourceId,T.destinationId,T.date,
            TD.id as transactionDetailId,TD.amount,TD.description,TD.categoryId,TD.aNombreDe,COALESCE(TD.budgetDate, T.date) as budgetDate
        FROM TransactionDetails AS TD INNER JOIN `Transaction` AS T ON TD.transactionId = T.id
        WHERE 
            (:budgetStartDate is null OR COALESCE(TD.budgetDate, T.date) >= :budgetStartDate) AND 
            (:budgetEndDate is null OR COALESCE(TD.budgetDate, T.date) <= :budgetEndDate)
        ORDER BY T.date DESC, T.id DESC
    """
    )
    fun getAllForBudget(
        budgetStartDate: LocalDate?,
        budgetEndDate: LocalDate?
    ): Flow<List<ExtendedTransaction>>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Insert
    suspend fun insert(transactionDetails: TransactionDetails): Long

    @Insert
    suspend fun insertAll(vararg transactions: Transaction): List<Long>

    @Insert
    suspend fun insertAll(vararg transactionDetails: TransactionDetails): List<Long>

    @Upsert
    suspend fun upsert(transaction: Transaction)

    @Upsert
    suspend fun upsert(transactionDetails: TransactionDetails)

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

    @Query(
        "DELETE FROM TransactionDetails WHERE transactionId = :transactionId"
    )
    suspend fun deleteTransactionDetailsById(transactionId: Int)

    suspend fun deleteAll() {
        deleteAllTransactionsDetails()
        deleteAllTransactions()
    }
}