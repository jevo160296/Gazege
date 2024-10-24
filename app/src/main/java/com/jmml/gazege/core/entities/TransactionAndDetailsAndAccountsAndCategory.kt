package com.jmml.gazege.core.entities

data class TransactionAndDetailsAndAccountsAndCategory(
    val transaction: TransactionAndDetails,
    val sourceAccount: Account,
    val destinationAccount: Account,
    val category: Category?
) {
    companion object {
        fun from(
            transactions: List<TransactionAndDetails>,
            accounts: List<Account>,
            categories: List<Category>
        ): List<TransactionAndDetailsAndAccountsAndCategory> {
            val accountMap: Map<Int?, Account> = accounts.associateBy { it.id }
            val categoriesMap: Map<Int?, Category> = categories.associateBy { it.id }
            return transactions.mapNotNull { transaction: TransactionAndDetails ->
                val sourceAccount = accountMap[transaction.sourceId]
                val destinationAccount = accountMap[transaction.destinationId]
                if (sourceAccount != null && destinationAccount != null) {
                    TransactionAndDetailsAndAccountsAndCategory(
                        transaction,
                        sourceAccount = sourceAccount,
                        destinationAccount = destinationAccount,
                        category = categoriesMap[transaction.categoryId]
                    )
                } else {
                    null
                }
            }
        }
    }
}