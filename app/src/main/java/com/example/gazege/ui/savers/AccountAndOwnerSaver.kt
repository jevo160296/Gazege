package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParcelableAccountAndOwner(
    val account_id: Int?,
    val account_name: String,
    val account_ownerId: Int,
    val account_initial_balance: Double,
    val owner_id: Int?,
    val owner_name: String
) : Parcelable

val accountAndOwnerSaver = Saver<AccountAndOwner, ParcelableAccountAndOwner>(
    save = { state ->
        ParcelableAccountAndOwner(
            account_id = state.account.id,
            account_name = state.account.name,
            account_ownerId = state.account.ownerId,
            account_initial_balance = state.account.initial_balance,
            owner_id = state.owner.id,
            owner_name = state.owner.name
        )
    },
    restore = {
        AccountAndOwner(
            account = Account(
                it.account_id,
                it.account_name,
                it.account_ownerId,
                it.account_initial_balance
            ),
            owner = Person(
                it.owner_id,
                it.owner_name
            )
        )
    }
)