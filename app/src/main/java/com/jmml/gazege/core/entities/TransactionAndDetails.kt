package com.jmml.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionAndDetails(
    @Embedded
    val transaction: Transaction,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val transactionDetails: TransactionDetails
) {
    val transactionId get() = transaction.id
    val sourceId get() = transaction.sourceId
    val destinationId get() = transaction.destinationId
    val date get() = transaction.date
    val transactionDetailsId get() = transactionDetails.id
    val amount get() = transactionDetails.amount
    val description get() = transactionDetails.description
    val categoryId get() = transactionDetails.categoryId
    val aNombreDe get() = transactionDetails.aNombreDe

    companion object {
        fun from(
            transaction: List<Transaction>,
            transactionDetails: List<TransactionDetails>
        ): List<TransactionAndDetails> {
            val transactionMap = transaction.associateBy { it.id }
            return transactionDetails.map {
                TransactionAndDetails(
                    transaction = transactionMap[it.transactionId]!!,
                    transactionDetails = it
                )
            }
        }

        fun from(transactions: List<TransactionWithDetails>): List<TransactionAndDetails> =
            transactions.flatMap { transactionWithDetails ->
                transactionWithDetails.transactionDetails.map { transactionDetail ->
                    TransactionAndDetails(
                        transaction = transactionWithDetails.transaction,
                        transactionDetails = transactionDetail
                    )
                }
            }
    }
}
