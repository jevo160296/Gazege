package com.example.gazege.core.entities

data class AccountAndOwnerWithTransactionsAndPockets(
    val accountAndOwnerWithTransactions: AccountAndOwnerWithTransactions,
    val pockets: List<AccountAndOwnerWithTransactionsAndPockets>
) {
    val allOutTransactionsWithOutPocketTransactions: List<Transaction>
        get() = listOf(
            *accountAndOwnerWithTransactions.outTransactions.toTypedArray(),
            *pockets.flatMap { it.allOutTransactionsWithOutPocketTransactions }.toTypedArray()
        )
    val allTransactionsWithPocketTransactions: List<Transaction>
        get() = listOf(
            *accountAndOwnerWithTransactions.allTransactions.toTypedArray(),
            *pockets.flatMap { it.allTransactionsWithPocketTransactions }.toTypedArray()
        )

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
                    .map {
                        from(
                            it,
                            accountAndOwnerWithTransactionsList
                        )
                    }
            )
        }
    }
}