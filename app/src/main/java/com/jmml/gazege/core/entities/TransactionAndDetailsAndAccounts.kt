package com.jmml.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionAndDetailsAndAccounts(
    @Embedded val transaction: TransactionAndDetails,
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
            transactionDetails: List<TransactionDetails>,
            accounts: List<Account>
        ): List<TransactionAndDetailsAndAccounts> {
            val transactionMap = transactions.associateBy { it.id }
            return transactionDetails.map { transactionDetail ->
                val transaction = transactionMap[transactionDetail.transactionId]!!
                TransactionAndDetailsAndAccounts(
                    transaction = TransactionAndDetails(
                        transaction = transaction,
                        transactionDetails = transactionDetail
                    ),
                    sourceAccount = accounts.firstOrNull { acc -> acc.id == transaction.sourceId }
                        ?: Account.empty(),
                    destinationAccount = accounts.firstOrNull { acc -> acc.id == transaction.destinationId }
                        ?: Account.empty()
                )
            }
        }
    }
}