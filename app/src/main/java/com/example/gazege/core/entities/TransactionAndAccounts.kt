package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionAndAccounts(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "sourceId",
        entityColumn = "id"
    )
    val sourceAccount: Account,
    @Relation(
        parentColumn = "destinationId",
        entityColumn = "id"
    )
    val destinationAccount: Account
) {
    companion object {
        fun from(
            transactions: List<Transaction>,
            accounts: List<Account>
        ): List<TransactionAndAccounts> {
            return transactions.map { transaction ->
                TransactionAndAccounts(
                    transaction = transaction,
                    sourceAccount = accounts.firstOrNull { acc -> acc.id == transaction.sourceId }
                        ?: Account.empty(),
                    destinationAccount = accounts.firstOrNull { acc -> acc.id == transaction.destinationId }
                        ?: Account.empty()
                )
            }
        }
    }
}