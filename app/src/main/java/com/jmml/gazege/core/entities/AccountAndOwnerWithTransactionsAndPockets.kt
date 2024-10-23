package com.jmml.gazege.core.entities

data class AccountAndOwnerWithTransactionsAndPockets(
    val accountAndOwnerWithTransactions: AccountAndOwnerWithTransactions,
    val pockets: List<AccountAndOwnerWithTransactionsAndPockets>
) {
    val allOutTransactionsWithOutPocketTransactions: List<TransactionWithDetails>
        get() = listOf(
            *accountAndOwnerWithTransactions.outTransactions.toTypedArray(),
            *pockets.flatMap { it.allOutTransactionsWithOutPocketTransactions }.toTypedArray()
        )
    val allInTransactionsWithInPocketTransactions: List<TransactionWithDetails>
        get() = listOf(
            *accountAndOwnerWithTransactions.inTransactions.toTypedArray(),
            *pockets.flatMap { it.allInTransactionsWithInPocketTransactions }.toTypedArray()
        )
    val allTransactionsWithPocketTransactions: List<TransactionWithDetails>
        get() = listOf(
            *accountAndOwnerWithTransactions.allTransactions.toTypedArray(),
            *pockets.flatMap { it.allTransactionsWithPocketTransactions }.toTypedArray()
        )

    val allPocketsAndSubPockets: List<AccountAndOwnerWithTransactionsAndPockets> = pockets
        .flatMap {
            listOf(it)
                .plus(it.allPocketsAndSubPockets)
        }

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