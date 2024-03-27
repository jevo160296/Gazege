package com.jmml.gazege.core.entities

data class AccountAndOwnerWithTransactionsAndPockets(
    val accountAndOwnerWithTransactions: AccountAndOwnerWithTransactions,
    val pockets: List<AccountAndOwnerWithTransactionsAndPockets>
) {
    val allOutTransactionsWithOutPocketTransactions: List<Transaction>
        get() = listOf(
            *accountAndOwnerWithTransactions.outTransactions.toTypedArray(),
            *pockets.flatMap { it.allOutTransactionsWithOutPocketTransactions }.toTypedArray()
        )
    val allInTransactionsWithInPocketTransactions: List<Transaction>
        get() = listOf(
            *accountAndOwnerWithTransactions.inTransactions.toTypedArray(),
            *pockets.flatMap { it.allInTransactionsWithInPocketTransactions }.toTypedArray()
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

        fun from(
            accountAndOwnerWithTransactionsList: List<AccountAndOwnerWithTransactions>
        ): List<AccountAndOwnerWithTransactionsAndPockets> {
            return accountAndOwnerWithTransactionsList
                .filter { it.account.parentId == null }
                .map { from(it, accountAndOwnerWithTransactionsList) }
        }
    }
}