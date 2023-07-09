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
    val accounts: List<AccountAndOwnerWithTransactionsAndPockets>
) {
    @Ignore
    var totalCache: MutableMap<Pair<LocalDate?, LocalDate?>, Double> = mutableMapOf()

    @Ignore
    var ingresosCache: MutableMap<Pair<LocalDate?, LocalDate?>, Double> = mutableMapOf()

    @Ignore
    var egresosCache: MutableMap<Pair<LocalDate?, LocalDate?>, Double> = mutableMapOf()

    @Ignore
    var flujos: MutableMap<Person, Double> = mutableMapOf()

    companion object {
        fun from(
            persons: List<Person>,
            accountAndOwnerWithTransactionsAndPockets: List<AccountAndOwnerWithTransactionsAndPockets>
        ): List<PersonWithAccounts> {
            return persons.map { person ->
                PersonWithAccounts(
                    person = person,
                    accounts = accountAndOwnerWithTransactionsAndPockets
                        .filter { it.accountAndOwnerWithTransactions.owner == person }
                )
            }
        }
    }
}