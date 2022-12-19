package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.example.gazege.core.entities.*
import kotlinx.parcelize.Parcelize

data class PartialAccount (
    var id: Int? = null,
    var name: String? = null,
    var ownerId: Int? = null,
    var initial_balance: Double? = 0.0
)
{
    fun isComplete(): Boolean{
        return name != null
                && name!!.isNotBlank()
                && ownerId != null
                && initial_balance != null
    }

    fun toFull(): Account {
        if(isComplete()){
            return Account(
                id = id,
                name = name!!,
                ownerId = ownerId!!,
                initial_balance = initial_balance!!
            )
        }
        else{
            throw Exception()
        }
    }
}

data class PartialAccountAndOwner (
    var account: PartialAccount = PartialAccount(),
    var owner: Person? = null
)
{
    fun isComplete(): Boolean{
        return account.isComplete() && owner != null
    }

    fun toFull(): AccountAndOwner {
        if(isComplete()){
            return AccountAndOwner(
                account = account.toFull(),
                owner = owner!!
            )
        }
        else{
            throw Exception()
        }
    }
}

@Parcelize
data class ParcelableAccountAndOwner(
    val account_id: Int?,
    val account_name: String?,
    val account_ownerId: Int?,
    val account_initial_balance: Double?,
    val owner_id: Int?,
    val owner_name: String?
) : Parcelable

val accountAndOwnerSaver = Saver<PartialAccountAndOwner, ParcelableAccountAndOwner>(
    save = { state ->
        ParcelableAccountAndOwner(
            account_id = state.account.id,
            account_name = state.account.name,
            account_ownerId = state.account.ownerId,
            account_initial_balance = state.account.initial_balance,
            owner_id = state.owner?.id,
            owner_name = state.owner?.name
        )
    },
    restore = {
        PartialAccountAndOwner(
            account = PartialAccount(
                it.account_id,
                it.account_name,
                it.account_ownerId,
                it.account_initial_balance
            ),
            owner = if(it.owner_name != null) Person(
                it.owner_id,
                it.owner_name
            ) else null
        )
    }
)