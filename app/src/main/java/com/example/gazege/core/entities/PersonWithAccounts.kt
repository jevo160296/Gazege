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
    var range: Pair<LocalDate?, LocalDate?>? = null

    @Ignore
    var total: Double = Double.NaN

    @Ignore
    var ingresos: Double = Double.NaN

    @Ignore
    var egresos: Double = Double.NaN

    @Ignore
    var flujos: MutableMap<Person, Double> = mutableMapOf()

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