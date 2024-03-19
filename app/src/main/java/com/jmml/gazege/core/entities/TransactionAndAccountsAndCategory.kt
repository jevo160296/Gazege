package com.jmml.gazege.core.entities

data class TransactionAndAccountsAndCategory(
    val transaction: Transaction,
    val sourceAccount: Account,
    val destinationAccount: Account,
    val category: Category?
) {
    fun toTransactionAndAccounts(): TransactionAndAccounts =
        TransactionAndAccounts(transaction, sourceAccount, destinationAccount)

    companion object {
        fun from(
            transactions: List<Transaction>,
            accounts: List<Account>,
            categories: List<Category>
        ): List<TransactionAndAccountsAndCategory> {
            val accountMap: Map<Int?, Account> = accounts.associateBy { it.id }
            val categoriesMap: Map<Int?, Category> = categories.associateBy { it.id }
            return transactions.mapNotNull { transaction: Transaction ->
                val sourceAccount = accountMap[transaction.sourceId]
                val destinationAccount = accountMap[transaction.destinationId]
                if (sourceAccount != null && destinationAccount != null) {
                    TransactionAndAccountsAndCategory(
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