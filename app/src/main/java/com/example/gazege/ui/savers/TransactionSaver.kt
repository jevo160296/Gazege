package com.example.gazege.ui.savers

import android.os.Parcelable
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import com.example.gazege.ui.widgets.SignedBigDecimal
import com.example.gazege.ui.widgets.toSignedBigDecimal
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class PartialTransaction(
    var id: Int?,
    var amount: SignedBigDecimal?,
    var description: String?,
    var sourceId: Int?,
    var destinationId: Int?,
    var date: LocalDate?,
    var aNombreDe: Int?,
    var categoryId: Int?
): PartialEntity<Transaction>
{
    override fun isComplete(): Boolean {
        return amount != null &&
                sourceId != null &&
                destinationId != null &&
                date != null
    }

    override fun toFull(): Transaction {
        if(isComplete()){
            return Transaction(
                id = id,
                amount = amount!!.toDouble(),
                description = description ?: "",
                sourceId = sourceId!!,
                destinationId = destinationId!!,
                date = date!!,
                aNombreDe = aNombreDe,
                categoryId = categoryId
            )
        } else {
            throw Exception()
        }
    }

    companion object {
        fun blankEntity(): PartialTransaction {
            return PartialTransaction(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }

        fun from(transaction: Transaction): PartialTransaction = transaction.run {
            PartialTransaction(
                id = id,
                amount = amount.toSignedBigDecimal(),
                description = description,
                sourceId = sourceId,
                destinationId = destinationId,
                date = date,
                aNombreDe = aNombreDe,
                categoryId = categoryId
            )
        }
    }

}

data class PartialTransactionAndAccounts(
    var transaction: PartialTransaction,
    var sourceAccount: Account?,
    var destinationAccount: Account?
): PartialEntity<TransactionAndAccounts>
{
    override fun isComplete(): Boolean {
        return transaction.isComplete() &&
                sourceAccount != null &&
                destinationAccount != null
    }

    override fun toFull(): TransactionAndAccounts {
        if(isComplete()){
            return TransactionAndAccounts(
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
            transaction: PartialTransaction,
            sourceAccount: Account?,
            destinationAccount: Account?
        ): PartialTransactionAndAccounts = PartialTransactionAndAccounts(
            transaction = transaction,
            sourceAccount = sourceAccount,
            destinationAccount = destinationAccount
        )
    }

}

@Parcelize
data class ParcelableTransaction(
    var id: Int?,
    var amount: SignedBigDecimal?,
    var description: String?,
    var sourceId: Int?,
    var destinationId: Int?,
    var date: LocalDate?,
    var aNombreDe: Int?,
    var categoryId: Int?
) : Parcelable

@Parcelize
data class ParcelableTransactionAndAccounts(
    val transaction: ParcelableTransaction,
    val sourceAccount: ParcelableAccount?,
    val destinationAccount: ParcelableAccount?
) : Parcelable
