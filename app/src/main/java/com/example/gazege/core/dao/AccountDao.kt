package com.example.gazege.core.dao

import androidx.room.*
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Transaction
    @Query("SELECT * FROM Account")
    fun getAll(): Flow<List<AccountAndOwnerWithTransactions>>

    @Insert
    suspend fun insertAll(vararg accounts: Account): List<Long>

    @Insert
    suspend fun insertPerson(person: Person): Long

    @Transaction
    suspend fun insert(accountAndOwner: AccountAndOwner): Long {
        val ownerId: Int = accountAndOwner.owner.id ?: insertPerson(accountAndOwner.owner).toInt()
        val accountToInsert = accountAndOwner.account.copy(ownerId = ownerId)
        return insertAll(accountToInsert).first()
    }

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account): Int
}