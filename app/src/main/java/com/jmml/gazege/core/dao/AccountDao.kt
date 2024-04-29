package com.jmml.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.core.entities.AccountAndOwnerWithTransactions
import com.jmml.gazege.core.entities.AccountAndOwnerWithTransactionsAndPockets
import com.jmml.gazege.core.entities.Person
import com.jmml.zoo.extensions.localdate.isBetween
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

    @Query(
        "DELETE FROM Account"
    )
    suspend fun deleteAll()

    companion object {
        fun calculateIngresos(
            account: AccountAndOwnerWithTransactions,
            startDate: LocalDate?,
            endDate: LocalDate?,
            accountsToOmit: List<Account> = listOf()
        ): Double {
            return account.inTransactions
                .filter { it.sourceId !in accountsToOmit.map { account -> account.id } }
                .filter { it.date.isBetween(startDate, endDate) }
                .sumOf { it.amount }
        }

        private fun calculateChildrenIngresos(
            account: AccountAndOwnerWithTransactionsAndPockets,
            startDate: LocalDate?,
            endDate: LocalDate?,
            accountsToOmit: List<Account> = listOf()
        ): Double {
            return account.pockets.sumOf {
                calculateIngresos(
                    it.accountAndOwnerWithTransactions,
                    startDate,
                    endDate,
                    accountsToOmit
                ) + calculateChildrenIngresos(it, startDate, endDate, accountsToOmit)
            }
        }

        fun calculateEgresos(
            account: AccountAndOwnerWithTransactions,
            startDate: LocalDate?,
            endDate: LocalDate?,
            accountsToOmit: List<Account> = listOf()
        ): Double {
            return account.outTransactions
                .filter { it.destinationId !in accountsToOmit.map { account -> account.id } }
                .filter { it.date.isBetween(startDate, endDate) }
                .sumOf { it.amount }
        }

        private fun calculateChildrenEgresos(
            account: AccountAndOwnerWithTransactionsAndPockets,
            startDate: LocalDate?,
            endDate: LocalDate?,
            accountsToOmit: List<Account> = listOf()
        ): Double {
            return account.pockets.sumOf {
                calculateEgresos(
                    it.accountAndOwnerWithTransactions,
                    startDate,
                    endDate,
                    accountsToOmit
                ) + calculateChildrenEgresos(it, startDate, endDate, accountsToOmit)
            }
        }

        private fun calculateTotal(
            account: AccountAndOwnerWithTransactions, startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            val totalIn = calculateIngresos(account, startDate, endDate)
            val totalOut = calculateEgresos(account, startDate, endDate)
            return totalIn - totalOut
        }

        private fun calculateChildrenTotal(
            account: AccountAndOwnerWithTransactionsAndPockets,
            startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            val totalIn = calculateChildrenIngresos(account, startDate, endDate)
            val totalOut = calculateChildrenEgresos(account, startDate, endDate)
            return totalIn - totalOut
        }

        fun getChildrenTotal(
            account: AccountAndOwnerWithTransactionsAndPockets,
            startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            val newRange = Pair(startDate, endDate)
            val shouldBeCalculated = account.accountAndOwnerWithTransactions.childrenTotal.isNaN()
                    || account.accountAndOwnerWithTransactions.range != newRange
            if (shouldBeCalculated) {
                val childrenTotal = calculateChildrenTotal(account, startDate, endDate)
                account.accountAndOwnerWithTransactions.childrenTotal = childrenTotal
                account.accountAndOwnerWithTransactions.range = newRange
            }
            return account.accountAndOwnerWithTransactions.childrenTotal
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