package com.example.gazege.ui.savers

import android.os.Parcelable
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import kotlinx.parcelize.Parcelize
import java.util.*

data class PartialTransaction(
    var id: Int? = null,
    var amount: Double? = null,
    var description: String? = null,
    var sourceId: Int? = null,
    var destinationId: Int? = null,
    var date: Date? = null
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
data class ParcelableTransactionAndAccounts(
    val account_id: Int?,
    val account_name: String?,
    val account_ownerId: Int?,
    val account_initial_balance: Double?,
    val owner_id: Int?,
    val owner_name: String?
) : Parcelable