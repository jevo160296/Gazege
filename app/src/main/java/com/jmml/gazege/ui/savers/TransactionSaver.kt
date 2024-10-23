package com.jmml.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionAndDetails
import com.jmml.gazege.core.entities.TransactionDetails
import com.jmml.gazege.core.entities.TransactionWithDetails
import com.jmml.gazege.core.entities.TransactionWithDetailsAndAccounts
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class PartialTransactionDetails(
    val id: Int? = null,
    val transactionId: Int?,
    val amount: Double?,
    val description: String?,
    val categoryId: Int?,
    val aNombreDe: Int?
) : PartialEntity<TransactionDetails> {
    override fun isComplete(): Boolean {
        return amount != null &&
                transactionId != null
    }

    override fun toFull(): TransactionDetails {
        if (isComplete()) {
            return TransactionDetails(
                id = id,
                transactionId = transactionId!!,
                amount = amount!!.toDouble(),
                description = description ?: "",
                categoryId = categoryId,
                aNombreDe = aNombreDe
            )
        } else {
            throw Exception()
        }
    }

    companion object {
        fun from(transactionDetails: TransactionDetails): PartialTransactionDetails =
            transactionDetails.run {
                PartialTransactionDetails(
                    id = id,
                    transactionId = transactionId,
                    amount = amount,
                    description = description,
                    categoryId = categoryId,
                    aNombreDe = aNombreDe
                )
            }
    }
}

data class PartialTransactionWithDetails(
    var transactionId: Int?,
    var sourceId: Int?,
    var destinationId: Int?,
    var date: LocalDate?,
    var transactionDetails: List<PartialTransactionDetails>
) : PartialEntity<TransactionWithDetails>
{
    override fun isComplete(): Boolean {
        return sourceId != null &&
                destinationId != null &&
                date != null
    }

    override fun toFull(): TransactionWithDetails {
        if(isComplete()){
            return TransactionWithDetails(
                transaction = Transaction(
                    id = transactionId,
                    sourceId = sourceId!!,
                    destinationId = destinationId!!,
                    date = date!!
                ),
                transactionDetails = transactionDetails.map { it.toFull() }
            )
        } else {
            throw Exception()
        }
    }

    companion object {
        fun from(transactionWithDetails: TransactionWithDetails): PartialTransactionWithDetails =
            transactionWithDetails.run {
                PartialTransactionWithDetails(
                    transactionId = transaction.id,
                    sourceId = transaction.sourceId,
                    destinationId = transaction.destinationId,
                    date = transaction.date,
                    transactionDetails = transactionDetails.map { PartialTransactionDetails.from(it) }
                )
            }
    }

}

data class PartialTransactionAndDetails(
    var transactionId: Int?,
    var sourceId: Int?,
    var destinationId: Int?,
    var date: LocalDate?,
    var transactionDetails: PartialTransactionDetails?
) : PartialEntity<TransactionAndDetails> {
    override fun isComplete(): Boolean {
        return sourceId != null &&
                destinationId != null &&
                date != null &&
                transactionDetails != null
    }

    override fun toFull(): TransactionAndDetails {
        if (isComplete()) {
            return TransactionAndDetails(
                transaction = Transaction(
                    id = transactionId,
                    sourceId = sourceId!!,
                    destinationId = destinationId!!,
                    date = date!!
                ),
                transactionDetails = transactionDetails!!.toFull()
            )
        } else {
            throw Exception()
        }
    }
}

data class PartialTransactionWithDetailsAndAccounts(
    val transaction: PartialTransactionWithDetails,
    val sourceAccount: Account?,
    val destinationAccount: Account?
) : PartialEntity<TransactionWithDetailsAndAccounts>
{
    override fun isComplete(): Boolean {
        return transaction.isComplete() &&
                sourceAccount != null &&
                destinationAccount != null
    }

    override fun toFull(): TransactionWithDetailsAndAccounts {
        if(isComplete()){
            return TransactionWithDetailsAndAccounts(
                transaction = transaction.toFull(),
                sourceAccount = sourceAccount!!,
                destinationAccount = destinationAccount!!
            )
        } else {
            throw Exception()
        }
    }

    companion object {

        fun from(
            transaction: PartialTransactionWithDetails,
            sourceAccount: Account?,
            destinationAccount: Account?
        ): PartialTransactionWithDetailsAndAccounts = PartialTransactionWithDetailsAndAccounts(
            transaction = transaction,
            sourceAccount = sourceAccount,
            destinationAccount = destinationAccount
        )
    }

}

@Parcelize
data class ParcelableTransaction(
    var id: Int?,
    var amount: Double?,
    var description: String?,
    var sourceId: Int?,
    var destinationId: Int?,
    var date: LocalDate?,
    var aNombreDe: Int?,
    var categoryId: Int?
) : Parcelable

@Parcelize
data class ParcelableTransactionDetails(
    var id: Int?,
    var transactionId: Int?,
    var amount: Double?,
    var description: String?,
    var categoryId: Int?,
    var aNombreDe: Int?
) : Parcelable

@Parcelize
data class ParcelableTransactionAndAccounts(
    val transaction: ParcelableTransaction,
    val sourceAccount: ParcelableAccount?,
    val destinationAccount: ParcelableAccount?
) : Parcelable

val transactionDetailsListSaver =
    listSaver<SnapshotStateList<PartialTransactionDetails>, ParcelableTransactionDetails>(
        save = { state ->
            state.map {
                ParcelableTransactionDetails(
                    id = it.id,
                    transactionId = it.transactionId,
                    amount = it.amount,
                    description = it.description,
                    categoryId = it.categoryId,
                    aNombreDe = it.aNombreDe
                )
            }
        },
        restore = { state ->
            mutableStateListOf(
                *state.map {
                    PartialTransactionDetails(
                        id = it.id,
                        transactionId = it.transactionId,
                        amount = it.amount,
                        description = it.description,
                        categoryId = it.categoryId,
                        aNombreDe = it.aNombreDe
                    )
                }.toTypedArray()
            )
        }
    )