package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class PartialTransaction(
    var id: Int? = null,
    var amount: Double? = null,
    var description: String? = null,
    var sourceId: Int? = null,
    var destinationId: Int? = null,
    var date: LocalDate? = null
): PartialEntity<Transaction>
{
    override fun isComplete(): Boolean {
        return amount != null &&
                description != null &&
                sourceId != null &&
                destinationId != null &&
                date != null
    }

    override fun toFull(): Transaction {
        if(isComplete()){
            return Transaction(
                id = id,
                amount = amount!!,
                description = description!!,
                sourceId = sourceId!!,
                destinationId = destinationId!!,
                date = date!!
            )
        }
        else{
            throw Exception()
        }
    }

}

data class PartialTransactionAndAccounts(
    var transaction: PartialTransaction = PartialTransaction(),
    var sourceAccount: Account? = null,
    var destinationAccount: Account? = null
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
        }
        else{
            throw Exception()
        }
    }

}

@Parcelize
data class ParcelableTransaction(
    var id: Int?,
    var amount: Double?,
    var description: String?,
    var sourceId: Int?,
    var destinationId: Int?,
    var date: LocalDate?
): Parcelable {
    fun toPartial(): PartialTransaction {
        return PartialTransaction(
            id = id,
            amount = amount,
            description = description,
            sourceId = sourceId,
            destinationId = destinationId,
            date = date
        )
    }
}

@Parcelize
data class ParcelableTransactionAndAccounts(
    val transaction: ParcelableTransaction,
    val sourceAccount: ParcelableAccount?,
    val destinationAccount: ParcelableAccount?
) : Parcelable

val transactionSaver = Saver<PartialTransactionAndAccounts, ParcelableTransactionAndAccounts>(
    save = { state ->
        ParcelableTransactionAndAccounts(
            transaction = ParcelableTransaction(
                id = state.transaction.id,
                amount = state.transaction.amount,
                description = state.transaction.description,
                sourceId = state.transaction.sourceId,
                destinationId = state.transaction.destinationId,
                date = state.transaction.date
            ),
            sourceAccount = if(state.sourceAccount != null){
                ParcelableAccount(
                    id = state.sourceAccount!!.id,
                    name = state.sourceAccount!!.name,
                    ownerId = state.sourceAccount!!.ownerId
                )
            }else{
                null
            },
            destinationAccount = if(state.destinationAccount != null){
                ParcelableAccount(
                    id = state.destinationAccount!!.id,
                    name = state.destinationAccount!!.name,
                    ownerId = state.destinationAccount!!.ownerId
                )
            } else{
                null
            }
        )
    },
    restore = {
        PartialTransactionAndAccounts(
            transaction = it.transaction.toPartial(),
            sourceAccount = it.sourceAccount?.toPartial()?.toFull(),
            destinationAccount = it.destinationAccount?.toPartial()?.toFull()
        )
    }
)