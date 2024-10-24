package com.jmml.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.NewTransactionDetails
import com.jmml.gazege.core.entities.NewTransactionWithDetails
import com.jmml.gazege.core.entities.NewTransactionWithDetailsAndAccounts
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionAndDetails
import com.jmml.gazege.core.entities.TransactionDetails
import com.jmml.gazege.core.entities.TransactionWithDetails
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

        fun new() = PartialTransactionDetails(
            transactionId = null,
            amount = null,
            description = null,
            categoryId = null,
            aNombreDe = null
        )
    }
}

data class PartialNewTransactionDetails(
    val id: Int?,
    val transactionId: Int?,
    val amount: Double?,
    val description: String?,
    val categoryId: Int?,
    val aNombreDe: Int?
) : PartialEntity<NewTransactionDetails> {
    override fun isComplete(): Boolean {
        return amount != null
    }

    override fun toFull(): NewTransactionDetails {
        if (isComplete()) {
            return NewTransactionDetails(
                id = id,
                transactionId = transactionId,
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
        fun from(transactionDetails: TransactionDetails): PartialNewTransactionDetails =
            PartialNewTransactionDetails(
                id = transactionDetails.id,
                transactionId = transactionDetails.transactionId,
                amount = transactionDetails.amount,
                description = transactionDetails.description,
                categoryId = transactionDetails.categoryId,
                aNombreDe = transactionDetails.aNombreDe
            )

        fun new() = PartialNewTransactionDetails(
            id = null,
            transactionId = null,
            amount = null,
            description = null,
            categoryId = null,
            aNombreDe = null
        )
    }
}

data class PartialTransactionWithDetails(
    var transactionId: Int?,
    var sourceId: Int?,
    var destinationId: Int?,
    var date: LocalDate?,
    var transactionDetails: List<PartialNewTransactionDetails>
) : PartialEntity<NewTransactionWithDetails>
{
    override fun isComplete(): Boolean {
        return sourceId != null &&
                destinationId != null &&
                date != null
    }

    override fun toFull(): NewTransactionWithDetails {
        if(isComplete()){
            return NewTransactionWithDetails(
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
                    transactionDetails = transactionDetails.map {
                        PartialNewTransactionDetails.from(
                            it
                        )
                    }
                )
            }

        fun from(transaction: Transaction, transactionDetails: List<PartialNewTransactionDetails>):
                PartialTransactionWithDetails = PartialTransactionWithDetails(
            transactionId = transaction.id,
            sourceId = transaction.sourceId,
            destinationId = transaction.destinationId,
            date = transaction.date,
            transactionDetails = transactionDetails
                )
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
) : PartialEntity<NewTransactionWithDetailsAndAccounts>
{
    override fun isComplete(): Boolean {
        return transaction.isComplete() &&
                sourceAccount != null &&
                destinationAccount != null
    }

    override fun toFull(): NewTransactionWithDetailsAndAccounts {
        if(isComplete()){
            return NewTransactionWithDetailsAndAccounts(
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
data class ParcelableNewTransactionDetails(
    val id: Int?,
    val transactionId: Int?,
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

val newTransactionDetailsListSaver =
    listSaver<SnapshotStateList<PartialNewTransactionDetails>, ParcelableNewTransactionDetails>(
        save = { state ->
            state.map {
                ParcelableNewTransactionDetails(
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
                    PartialNewTransactionDetails(
                        transactionId = it.transactionId,
                        id = it.id,
                        amount = it.amount,
                        description = it.description,
                        categoryId = it.categoryId,
                        aNombreDe = it.aNombreDe
                    )
                }.toTypedArray()
            )
        }
    )