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
    private var range: Pair<LocalDate?, LocalDate?>? = null

    @Ignore
    private var total: Double = Double.NaN

    @Ignore
    private var ingresos: Double = Double.NaN

    @Ignore
    var egresos: Double = Double.NaN

    private fun calculateValues(startDate: LocalDate?, endDate: LocalDate?) {
        val newRange = Pair(startDate, endDate)
        if (newRange != range) {
            val selfAccounts = accounts.map { it.account }
            total = accounts
                .filter { it.account.includedInTotal }
                .sumOf { it.getTotal(startDate, endDate) }
            ingresos = accounts
                .sumOf {
                    it.calculateIngresos(
                        startDate,
                        endDate,
                        selfAccounts
                    )
                }
            egresos = accounts
                .sumOf {
                    it.calculateEgresos(
                        startDate,
                        endDate,
                        selfAccounts
                    )
                }
            range = newRange
        }
    }

    fun getTotal(startDate: LocalDate?, endDate: LocalDate?): Double {
        calculateValues(startDate, endDate)
        return total
    }

    fun getIngresos(startDate: LocalDate?, endDate: LocalDate?): Double {
        calculateValues(startDate, endDate)
        return ingresos
    }

    fun getEgresos(startDate: LocalDate?, endDate: LocalDate?): Double {
        calculateValues(startDate, endDate)
        return egresos
    }
}