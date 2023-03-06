package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.example.gazege.core.dateBetween
import java.time.LocalDate

data class AccountAndOwnerWithTransactions(
    @Embedded val account: Account,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "id"
    )
    val owner: Person,
    @Relation(
        parentColumn = "id",
        entityColumn = "sourceId"
    )
    val outTransactions: List<Transaction>,
    @Relation(
        parentColumn = "id",
        entityColumn = "destinationId"
    )
    val inTransactions: List<Transaction>,
) {
    @Ignore
    private var total: Double = Double.NaN

    @Ignore
    private var range: Pair<LocalDate?, LocalDate?>? = null

    private fun calculateTotal(startDate: LocalDate?, endDate: LocalDate?): Double {
        val totalIn = calculateIngresos(startDate, endDate)
        val totalOut = calculateEgresos(startDate, endDate)
        return totalIn - totalOut
    }

    fun calculateIngresos(
        startDate: LocalDate?,
        endDate: LocalDate?,
        accountsToOmit: List<Account> = listOf()
    ): Double {
        return inTransactions
            .filter { it.sourceId !in accountsToOmit.map { account -> account.id } }
            .filter { dateBetween(it.date, startDate, endDate) }
            .sumOf { it.amount }
    }

    fun calculateEgresos(
        startDate: LocalDate?,
        endDate: LocalDate?,
        accountsToOmit: List<Account> = listOf()
    ): Double {
        return outTransactions
            .filter { it.destinationId !in accountsToOmit.map { account -> account.id } }
            .filter { dateBetween(it.date, startDate, endDate) }
            .sumOf { it.amount }
    }

    fun getTotal(startDate: LocalDate?, endDate: LocalDate?): Double {
        val newRange = Pair(startDate, endDate)
        if (total.isNaN() || range != newRange) {
            total = calculateTotal(startDate, endDate)
            range = newRange
        }
        return total
    }

    companion object {
        fun from(
            accounts: List<Account>,
            owners: List<Person>,
            transactions: List<Transaction>
        ): List<AccountAndOwnerWithTransactions> {
            return accounts.map { account ->
                AccountAndOwnerWithTransactions(
                    account,
                    owner = owners.firstOrNull { owner -> owner.id == account.ownerId }
                        ?: Person.empty(),
                    outTransactions = transactions.filter { transaction -> account.id == transaction.sourceId },
                    inTransactions = transactions.filter { transaction -> account.id == transaction.destinationId }
                )
            }
        }
    }
}