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
    private var egresos: Double = Double.NaN

    @Ignore
    private var flujos: MutableMap<Person, Double> = mutableMapOf()


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

    private fun calculateFlujo(
        otherPersonWithAccounts: PersonWithAccounts
    ): Double {
        val selfAccounts = this.accounts.toTypedArray()
        val otherAccountIds = otherPersonWithAccounts.accounts.map { it.account.id }
        val inTransactions = selfAccounts.flatMap { account ->
            account.inTransactions.filter { transaction ->
                transaction.sourceId in otherAccountIds
            }
        }
        val outTransactions = selfAccounts.flatMap { account ->
            account.outTransactions.filter { transaction ->
                transaction.destinationId in otherAccountIds
            }
        }

        val totalIn = inTransactions.sumOf { it.amount }
        val totalOut = outTransactions.sumOf { it.amount }

        return totalOut - totalIn
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

    /**
     * Total entregado por esta persona a la otra persona (Negativo si la otra persona le entregó
     * dinero).
     */
    fun getFlujo(
        otherPersonWithAccounts: PersonWithAccounts
    ): Double {
        val backedFlujo = flujos[otherPersonWithAccounts.person]
        val flujo = if (backedFlujo == null) {
            val calculatedFlujo = calculateFlujo(otherPersonWithAccounts)
            flujos[otherPersonWithAccounts.person] = calculatedFlujo
            calculatedFlujo
        } else {
            backedFlujo
        }
        return flujo
    }

    companion object {
        fun from(
            persons: List<Person>,
            accountAndOwnerWithTransactions: List<AccountAndOwnerWithTransactions>
        ): List<PersonWithAccounts> {
            return persons.map { person ->
                PersonWithAccounts(
                    person = person,
                    accounts = accountAndOwnerWithTransactions.filter { it.owner == person }
                )
            }
        }
    }
}