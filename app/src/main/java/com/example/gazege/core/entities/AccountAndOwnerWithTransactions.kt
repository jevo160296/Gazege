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
        val totalIn = inTransactions
            .filter { dateBetween(it.date, startDate, endDate) }
            .sumOf { it.amount }
        val totalOut = outTransactions
            .filter { dateBetween(it.date, startDate, endDate) }
            .sumOf { it.amount }
        return totalIn - totalOut
    }

    fun getTotal(startDate: LocalDate?, endDate: LocalDate?): Double {
        val newRange = Pair(startDate, endDate)
        if (total.isNaN() || range != newRange) {
            total = calculateTotal(startDate, endDate)
            range = newRange
        }
        return total
    }
}