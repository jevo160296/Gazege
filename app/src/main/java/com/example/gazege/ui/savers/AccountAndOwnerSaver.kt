package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import kotlinx.parcelize.Parcelize

data class PartialAccount (
    var id: Int?,
    var name: String?,
    var ownerId: Int?,
    var includedInTotal: Boolean?,
    var isIncome: Boolean?,
    var isOutcome: Boolean?
): PartialEntity<Account>
{
    override fun isComplete(): Boolean{
        return name != null
                && name!!.isNotBlank()
                && ownerId != null
                && includedInTotal != null
                && isIncome != null
                && isOutcome != null
    }

    override fun toFull(): Account {
        if(isComplete()){
            return Account(
                id = id,
                name = name!!,
                ownerId = ownerId!!,
                includedInTotal = includedInTotal!!,
                isIncome = isIncome!!,
                isOutcome = isOutcome!!
            )
        } else {
            throw Exception()
        }
    }

    companion object {
        fun blankEntity(): PartialAccount {
            return PartialAccount(
                null,
                null,
                null,
                null,
                null,
                null
            )
        }
    }
}

data class PartialAccountAndOwner(
    var account: PartialAccount,
    var owner: Person?
): PartialEntity<AccountAndOwner>
{
    override fun isComplete(): Boolean{
        return account.isComplete() && owner != null
    }

    override fun toFull(): AccountAndOwner {
        if (isComplete()) {
            return AccountAndOwner(
                account = account.toFull(),
                owner = owner!!
            )
        } else {
            throw Exception()
        }
    }

    companion object {
        fun blankEntity(): PartialAccountAndOwner {
            return PartialAccountAndOwner(PartialAccount.blankEntity(), null)
        }
    }
}

@Parcelize
data class ParcelableAccount(
    var id: Int?,
    var name: String?,
    var ownerId: Int?,
    var includedInTotal: Boolean?,
    var isIncome: Boolean?,
    var isOutcome: Boolean?
): Parcelable
{
    fun toPartial(): PartialAccount{
        return PartialAccount(
            id = id,
            name = name,
            ownerId = ownerId,
            includedInTotal = includedInTotal,
            isIncome = isIncome,
            isOutcome = isOutcome
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
                ownerId = state.account.ownerId,
                includedInTotal = state.account.includedInTotal,
                isIncome = state.account.isIncome,
                isOutcome = state.account.isOutcome
            ),
            owner = if(state.owner != null){
                ParcelablePerson(
                    id = state.owner?.id,
                    name = state.owner?.name,
                    importance = state.owner?.importance
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