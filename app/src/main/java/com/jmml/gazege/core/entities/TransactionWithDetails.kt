package com.jmml.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Relation
import java.time.LocalDate

data class TransactionWithDetails(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val transactionDetails: List<TransactionDetails>
) {
    val date: LocalDate get() = transaction.date
    val sourceId: Int get() = transaction.sourceId
    val destinationId: Int get() = transaction.destinationId
    val totalAmount: Double get() = transactionDetails.sumOf { it.amount }
    val descriptionString: String
        get() = transactionDetails.filter { it.description.isNotEmpty() }
            .joinToString(", ") { it.description }

    fun toTransaction() = transaction.copy()

    companion object {
        fun from(
            transaction: Transaction,
            transactionDetails: List<TransactionDetails>
        ): TransactionWithDetails {
            if (transaction.id != null) {
                return TransactionWithDetails(
                    transaction = transaction,
                    transactionDetails = transactionDetails
                )
            } else {
                throw Exception("Transaction must have id distinct from null.")
            }
        }

        fun from(
            transactions: List<Transaction>,
            transactionDetails: List<TransactionDetails>
        ): List<TransactionWithDetails> {
            val transactionDetailsMap = transactionDetails.groupBy { it.transactionId }
            return transactions.map {
                from(
                    transaction = it,
                    transactionDetails = transactionDetailsMap[it.id] ?: emptyList()
                )
            }
        }

        fun TransactionWithDetails.toTransactionDetails(): List<TransactionDetails> =
            transactionDetails.map { it.copy() }

        fun new(
            sourceId: Int,
            destinationId: Int,
            date: LocalDate,
            amount: Double,
            description: String,
            categoryId: Int?,
            aNombreDe: Int?,
            budgetDate: LocalDate?
        ) = NewTransactionWithDetails(
            transaction = Transaction(
                sourceId = sourceId,
                destinationId = destinationId,
                date = date
            ),
            transactionDetails = listOf(
                NewTransactionDetails(
                    amount = amount,
                    description = description,
                    categoryId = categoryId,
                    aNombreDe = aNombreDe,
                    budgetDate = budgetDate
                )
            )
        )
    }
}

data class NewTransactionWithDetails(
    val transaction: Transaction,
    val transactionDetails: List<NewTransactionDetails>
) {
    fun toTransaction(): Transaction = transaction
    fun toTransactionDetails(transactionId: Int): List<TransactionDetails> =
        transactionDetails.map {
            TransactionDetails(
                id = it.id,
                transactionId = transactionId,
                amount = it.amount,
                description = it.description,
                categoryId = it.categoryId,
                aNombreDe = it.aNombreDe,
                budgetDate = it.budgetDate
            )
        }
}

data class NewTransactionDetails(
    val id: Int? = null,
    val transactionId: Int? = null,
    val amount: Double,
    val description: String,
    val categoryId: Int?,
    val aNombreDe: Int?,
    val budgetDate: LocalDate?
)