package com.jmml.gazege.core.entities

import com.jmml.gazege.core.dao.PersonDao

sealed interface ITransactionListDetail {
    val sourceAccount: Account
    val destinationAccount: Account
    val transactionType: TransactionType
}

sealed interface ITransactionListDetailGrouped : ITransactionListDetail {
    val transaction: TransactionWithDetails
    val categories: List<Category>
    val descriptions: List<String> get() = transaction.transactionDetails.map { it.description }
    val totalAmount: Double get() = transaction.transactionDetails.sumOf { it.amount }

    val categoriesString: String?
        get() =
            categories
                .distinctBy { it.id }
                .takeIf { it.isNotEmpty() }
                ?.joinToString(", ", limit = 3, truncated = "...") { it.name }
}

sealed interface ITransactionListDetailIndividual : ITransactionListDetail {
    val transaction: TransactionAndDetails
    val category: Category?
}

data class TransactionListItemDetails(
    override val transaction: TransactionWithDetails,
    override val categories: List<Category>,
    override val sourceAccount: Account,
    override val destinationAccount: Account,
    override val transactionType: TransactionType
) : ITransactionListDetailGrouped {
    companion object {
        fun from(
            transactions: List<TransactionWithDetails>,
            categories: List<Category>,
            accounts: List<Account>,
            principalPersonId: Int?
        ): List<TransactionListItemDetails> {
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            return transactions.map { transaction ->
                val category = transaction.transactionDetails.mapNotNull {
                    categoryMap[it.categoryId]
                }
                val sourceAccount = accountMap[transaction.transaction.sourceId]!!
                val destinationAccount = accountMap[transaction.transaction.destinationId]!!

                val sourceAccountIsExternal = sourceAccount.ownerId != principalPersonId
                val destinationAccountIsExternal =
                    destinationAccount.ownerId != principalPersonId

                val transactionType =
                    when (Pair(sourceAccountIsExternal, destinationAccountIsExternal)) {
                        Pair(true, true) -> TransactionType.TRANSFER
                        Pair(true, false) -> TransactionType.INCOME
                        Pair(false, true) -> TransactionType.OUTCOME
                        Pair(false, false) -> TransactionType.TRANSFER
                        else -> TODO("Unkown state ${sourceAccount.isIncome}, ${destinationAccount.isOutcome}")
                    }
                TransactionListItemDetails(
                    transaction = transaction,
                    categories = category,
                    sourceAccount = sourceAccount,
                    destinationAccount = destinationAccount,
                    transactionType = transactionType
                )
            }
        }
    }
}

data class TransactionListItemDetailsWithAccount(
    override val transaction: TransactionWithDetails,
    override val categories: List<Category>,
    override val sourceAccount: Account,
    override val destinationAccount: Account,
    override val transactionType: TransactionType,
    val transactionAccountType: TransactionType
) : ITransactionListDetailGrouped {
    companion object {
        fun from(
            transactions: List<TransactionWithDetails>,
            categories: List<Category>,
            accounts: List<Account>,
            principalPersonId: Int?,
            includedAccountsId: Set<Int>
        ): List<TransactionListItemDetailsWithAccount> {
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            return transactions.map { transaction ->
                val category = transaction.transactionDetails.mapNotNull {
                    categoryMap[it.categoryId]
                }
                val sourceAccount = accountMap[transaction.transaction.sourceId]!!
                val destinationAccount = accountMap[transaction.transaction.destinationId]!!

                val sourceAccountIsExternal = sourceAccount.ownerId != principalPersonId
                val destinationAccountIsExternal =
                    destinationAccount.ownerId != principalPersonId
                val sourceAccountNotInChildren: Boolean =
                    sourceAccount.id !in includedAccountsId
                val destinationAccountNotInChildren: Boolean =
                    destinationAccount.id !in includedAccountsId

                val transactionType =
                    when (Pair(sourceAccountIsExternal, destinationAccountIsExternal)) {
                        Pair(true, true) -> TransactionType.TRANSFER
                        Pair(true, false) -> TransactionType.INCOME
                        Pair(false, true) -> TransactionType.OUTCOME
                        Pair(false, false) -> TransactionType.TRANSFER
                        else -> TODO("Unkown state ${sourceAccount.isIncome}, ${destinationAccount.isOutcome}")
                    }
                val transactionAccountType =
                    when (sourceAccountNotInChildren to destinationAccountNotInChildren) {
                        true to true -> TransactionType.TRANSFER
                        true to false -> TransactionType.INCOME
                        false to true -> TransactionType.OUTCOME
                        false to false -> TransactionType.TRANSFER
                        else -> TODO("Unkown state ${sourceAccount.isIncome}, ${destinationAccount.isOutcome}")
                    }
                TransactionListItemDetailsWithAccount(
                    transaction = transaction,
                    categories = category,
                    sourceAccount = sourceAccount,
                    destinationAccount = destinationAccount,
                    transactionType = transactionType,
                    transactionAccountType = transactionAccountType
                )
            }
        }
    }
}

data class TransactionListItemDetailsWithSign(
    override val transaction: TransactionAndDetails,
    override val category: Category?,
    override val sourceAccount: Account,
    override val destinationAccount: Account,
    override val transactionType: TransactionType,
    val fromPersonId: Int?,
    val toPersonId: Int?,
    val sign: Int
) : ITransactionListDetailIndividual {
    companion object {
        fun from(
            transactions: List<TransactionAndDetails>,
            categories: List<Category>,
            accounts: List<Account>,
            principalPersonId: Int?,
            toPersonId: Int?
        ): List<TransactionListItemDetailsWithSign> {
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            return transactions.map { transaction ->
                val category = categoryMap[transaction.categoryId]
                val sourceAccount = accountMap[transaction.transaction.sourceId]!!
                val destinationAccount = accountMap[transaction.transaction.destinationId]!!

                val sourceAccountIsExternal = sourceAccount.ownerId != principalPersonId
                val destinationAccountIsExternal =
                    destinationAccount.ownerId != principalPersonId

                val transactionType =
                    when (Pair(sourceAccountIsExternal, destinationAccountIsExternal)) {
                        Pair(true, true) -> TransactionType.TRANSFER
                        Pair(true, false) -> TransactionType.INCOME
                        Pair(false, true) -> TransactionType.OUTCOME
                        Pair(false, false) -> TransactionType.TRANSFER
                        else -> TODO("Unkown state ${sourceAccount.isIncome}, ${destinationAccount.isOutcome}")
                    }
                val sign = PersonDao.direction(
                    fromPersonId = principalPersonId,
                    toPersonId = toPersonId,
                    transaction = TransactionAndDetailsAndAccounts(
                        transaction = transaction,
                        sourceAccount = sourceAccount,
                        destinationAccount = destinationAccount
                    )
                )
                TransactionListItemDetailsWithSign(
                    transaction = transaction,
                    category = category,
                    sourceAccount = sourceAccount,
                    destinationAccount = destinationAccount,
                    transactionType = transactionType,
                    fromPersonId = principalPersonId,
                    toPersonId = toPersonId,
                    sign = sign
                )
            }
        }
    }
}

enum class TransactionType {
    INCOME,
    OUTCOME,
    TRANSFER
}