package com.example.gazege.core.dao

import androidx.room.*
import com.example.gazege.core.dateBetween
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Person
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AccountDao {
    @Transaction
    @Query("SELECT * " +
            "FROM Account " +
            "ORDER BY name")
    fun getAll(): Flow<List<Account>>

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

    companion object {
        fun calculateIngresos(
            account: AccountAndOwnerWithTransactions,
            startDate: LocalDate?,
            endDate: LocalDate?,
            accountsToOmit: List<Account> = listOf()
        ): Double {
            return account.inTransactions
                .filter { it.sourceId !in accountsToOmit.map { account -> account.id } }
                .filter { dateBetween(it.date, startDate, endDate) }
                .sumOf { it.amount }
        }

        fun calculateEgresos(
            account: AccountAndOwnerWithTransactions,
            startDate: LocalDate?,
            endDate: LocalDate?,
            accountsToOmit: List<Account> = listOf()
        ): Double {
            return account.outTransactions
                .filter { it.destinationId !in accountsToOmit.map { account -> account.id } }
                .filter { dateBetween(it.date, startDate, endDate) }
                .sumOf { it.amount }
        }

        private fun calculateTotal(
            account: AccountAndOwnerWithTransactions, startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            val totalIn = calculateIngresos(account, startDate, endDate)
            val totalOut = calculateEgresos(account, startDate, endDate)
            return totalIn - totalOut
        }

        fun getTotal(
            account: AccountAndOwnerWithTransactions,
            startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            val newRange = Pair(startDate, endDate)
            if (account.total.isNaN() || account.range != newRange) {
                account.total = calculateTotal(account, startDate, endDate)
                account.range = newRange
            }
            return account.total
        }
    }
}