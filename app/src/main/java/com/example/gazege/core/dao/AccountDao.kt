package com.example.gazege.core.dao

import androidx.room.*
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Transaction
    @Query("SELECT * FROM Account")
    fun getAll(): Flow<List<AccountAndOwnerWithTransactions>>

    @Insert
    suspend fun insertAll(vararg accounts: Account)

    @Delete
    suspend fun delete(account: Account)
}