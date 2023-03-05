package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import java.time.LocalDate

data class PersonWithAccounts(
    @Embedded val person: Person,
    @Relation(
        entity = Account::class,
        parentColumn = "id",
        entityColumn = "ownerId"
    )
    val accounts: List<AccountAndOwnerWithTransactions>
) {
    @Ignore
    private var total: Double = Double.NaN
    fun getTotal(startDate: LocalDate?, endDate: LocalDate?): Double {
        if (total.isNaN()) {
            total = accounts.sumOf { it.getTotal(startDate, endDate) }
        }
        return total
    }
}