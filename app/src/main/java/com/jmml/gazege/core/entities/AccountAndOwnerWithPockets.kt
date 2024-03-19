package com.jmml.gazege.core.entities

data class AccountAndOwnerWithPockets(
    val accountAndOwner: AccountAndOwner,
    val pockets: List<AccountAndOwnerWithPockets>
) {
    companion object {
        fun from(
            accountAndOwner: AccountAndOwner,
            accountAndOwnerList: List<AccountAndOwner>
        ): AccountAndOwnerWithPockets {
            return AccountAndOwnerWithPockets(
                accountAndOwner,
                accountAndOwnerList
                    .filter {
                        it.account.parentId == accountAndOwner.account.id
                    }.map {
                        from(
                            it,
                            accountAndOwnerList
                        )
                    }
            )
        }
    }
}