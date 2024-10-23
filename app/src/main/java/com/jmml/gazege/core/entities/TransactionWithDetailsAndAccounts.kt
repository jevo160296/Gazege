package com.jmml.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithDetailsAndAccounts(
    @Embedded val transaction: TransactionWithDetails,
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
    fun toTransactionAndDetailsAndAccounts(): List<TransactionAndDetailsAndAccounts> =
        transaction.transactionDetails.map { transactionDetails ->
            TransactionAndDetailsAndAccounts(
                transaction = TransactionAndDetails(
                    transaction = transaction.transaction,
                    transactionDetails = transactionDetails
                ),
                sourceAccount = sourceAccount,
                destinationAccount = destinationAccount
            )
        }

    companion object {
        fun List<TransactionWithDetailsAndAccounts>.toTransactionAndDetailsAndAccounts():
                List<TransactionAndDetailsAndAccounts> =
            flatMap { it.toTransactionAndDetailsAndAccounts() }

        fun from(
            transactions: List<Transaction>,
            transactionDetails: List<TransactionDetails>,
            accounts: List<Account>
        ): List<TransactionWithDetailsAndAccounts> {
            val transactionDetailsGroup = transactionDetails.groupBy { it.transactionId }
            return transactions.map { transaction ->
                val filteredTransactionDetails = transactionDetailsGroup[transaction.id]!!
                TransactionWithDetailsAndAccounts(
                    transaction = TransactionWithDetails(
                        transaction = transaction,
                        transactionDetails = filteredTransactionDetails
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