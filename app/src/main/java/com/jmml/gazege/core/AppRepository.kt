package com.jmml.gazege.core

import androidx.annotation.WorkerThread
import com.jmml.gazege.core.dao.AccountDao
import com.jmml.gazege.core.dao.BudgetDao
import com.jmml.gazege.core.dao.CategoryDao
import com.jmml.gazege.core.dao.PersonDao
import com.jmml.gazege.core.dao.TransactionDao
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.Budget
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(
    private val personDao: PersonDao,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getPersons(): Flow<List<Person>> {
        return personDao.getAll()
    }

    fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAll()
    }

    fun getTransactions(
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Flow<List<Transaction>> {
        return transactionDao.getAll(startDate, endDate)
    }

    fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAll()
    }

    fun getBudgets(): Flow<List<Budget>> {
        return budgetDao.getAll()
    }

    @WorkerThread
    suspend fun insertPerson(vararg person: Person): List<Long> {
        return personDao.insertAll(*person)
    }

    @WorkerThread
    suspend fun updatePerson(vararg person: Person) {
        personDao.updateAll(*person)
    }

    @WorkerThread
    suspend fun deletePerson(person: Person) {
        personDao.delete(person)
    }

    @WorkerThread
    suspend fun deleteAccount(account: Account) {
        accountDao.delete(account)
    }

    @WorkerThread
    suspend fun insertAccount(vararg account: Account): List<Long> {
        return accountDao.insertAll(*account)
    }

    @WorkerThread
    suspend fun updateAccount(account: Account) {
        accountDao.update(account)
    }

    @WorkerThread
    suspend fun insertTransaction(vararg transaction: Transaction) {
        transactionDao.insertAll(*transaction)
    }

    @WorkerThread
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    @WorkerThread
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction = transaction)
    }

    @WorkerThread
    suspend fun insertCategory(vararg category: Category): List<Long> {
        return categoryDao.insertAll(*category)
    }

    @WorkerThread
    suspend fun deleteCategory(category: Category) = categoryDao.deleteAll(category)

    @WorkerThread
    suspend fun updateCategory(vararg category: Category) = categoryDao.updateAll(*category)

    @WorkerThread
    suspend fun insertBudget(vararg budget: Budget) {
        budgetDao.insertAll(*budget)
    }

    @WorkerThread
    suspend fun updateBudget(budget: Budget) {
        budgetDao.update(budget)
    }

    @WorkerThread
    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteAll(budget)
    }

    @WorkerThread
    suspend fun deleteAllData() {
        accountDao.deleteAll()
        budgetDao.deleteAll()
        categoryDao.deleteAll()
        personDao.deleteAll()
        transactionDao.deleteAll()
    }
}