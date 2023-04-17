package com.example.gazege.core.entities

data class TransactionListItemDetails(
    val transaction: Transaction,
    val category: Category?,
    val sourceAccount: Account,
    val destinationAccount: Account
) {
    fun toTransactionAndAccounts(): TransactionAndAccounts = TransactionAndAccounts(
        transaction, sourceAccount, destinationAccount
    )

    companion object {
        fun from(
            transactions: List<Transaction>,
            categories: List<Category>,
            accounts: List<Account>
        ): List<TransactionListItemDetails> {
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            return transactions.mapNotNull { transaction ->
                val category = categoryMap[transaction.categoryId]
                val sourceAccount = accountMap[transaction.sourceId]
                val destinationAccount = accountMap[transaction.destinationId]
                if (sourceAccount != null && destinationAccount != null) {
                    TransactionListItemDetails(
                        transaction = transaction,
                        category = category,
                        sourceAccount = sourceAccount,
                        destinationAccount = destinationAccount
                    )
                } else {
                    null
                }
            }
        }
    }
}