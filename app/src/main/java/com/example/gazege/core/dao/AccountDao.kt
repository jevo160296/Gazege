package com.example.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Transaction
    @Query("SELECT * FROM Account")
    fun getAll(): Flow<List<AccountAndOwner>>

    @Insert
    suspend fun insertAll(vararg accounts: Account)

    @Delete
    suspend fun delete(account: Account)
}