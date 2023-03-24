package com.example.gazege.core.entities

data class AccountAndOwnerWithTransactionsAndPockets(
    val accountAndOwnerWithTransactions: AccountAndOwnerWithTransactions,
    val pockets: List<AccountAndOwnerWithTransactions>
) {
    companion object {
        fun from(
            accountAndOwnerWithTransactions: AccountAndOwnerWithTransactions,
            accountAndOwnerWithTransactionsList: List<AccountAndOwnerWithTransactions>
        ): AccountAndOwnerWithTransactionsAndPockets {
            return AccountAndOwnerWithTransactionsAndPockets(
                accountAndOwnerWithTransactions = accountAndOwnerWithTransactions,
                pockets = accountAndOwnerWithTransactionsList
                    .filter {
                        accountAndOwnerWithTransactions.account.id == it.account.parentId
                    }
            )
        }
    }
}