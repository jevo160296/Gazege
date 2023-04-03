package com.example.gazege.core.entities

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
            return transactions.map { transaction: Transaction ->
                TransactionAndAccountsAndCategory(
                    transaction,
                    sourceAccount = accountMap[transaction.sourceId]!!,
                    destinationAccount = accountMap[transaction.destinationId]!!,
                    category = categoriesMap[transaction.categoryId]
                )
            }
        }
    }
}