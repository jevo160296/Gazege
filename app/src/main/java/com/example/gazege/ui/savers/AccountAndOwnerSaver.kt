package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import kotlinx.parcelize.Parcelize

data class PartialAccount (
    var id: Int? = null,
    var name: String? = null,
    var ownerId: Int? = null
): PartialEntity<Account>
{
    override fun isComplete(): Boolean{
        return name != null
                && name!!.isNotBlank()
                && ownerId != null
    }

    override fun toFull(): Account {
        if(isComplete()){
            return Account(
                id = id,
                name = name!!,
                ownerId = ownerId!!
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
): PartialEntity<AccountAndOwner>
{
    override fun isComplete(): Boolean{
        return account.isComplete() && owner != null
    }

    override fun toFull(): AccountAndOwner {
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
data class ParcelableAccount(
    var id: Int?,
    var name: String?,
    var ownerId: Int?
): Parcelable
{
    fun toPartial(): PartialAccount{
        return PartialAccount(
            id = id,
            name = name,
            ownerId = ownerId
        )
    }
}

@Parcelize
data class ParcelableAccountAndOwner(
    val account: ParcelableAccount,
    val owner: ParcelablePerson?
) : Parcelable

val accountAndOwnerSaver = Saver<PartialAccountAndOwner, ParcelableAccountAndOwner>(
    save = { state ->
        ParcelableAccountAndOwner(
            account = ParcelableAccount(
                id = state.account.id,
                name = state.account.name,
                ownerId = state.account.ownerId
            ),
            owner = if(state.owner != null){
                ParcelablePerson(
                    id = state.owner?.id,
                    name = state.owner?.name
                )
            } else {
                null
            }
        )
    },
    restore = {
        PartialAccountAndOwner(
            account = it.account.toPartial(),
            owner = it.owner?.toPartial()?.toFull()
        )
    }
)