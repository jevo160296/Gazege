package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
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
    var total: Double = Double.NaN

    @Ignore
    var childrenTotal: Double = Double.NaN

    @Ignore
    var range: Pair<LocalDate?, LocalDate?>? = null

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