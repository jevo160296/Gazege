package com.jmml.gazege.core

import androidx.annotation.WorkerThread
import com.jmml.gazege.core.dao.AccountDao
import com.jmml.gazege.core.dao.BudgetDao
import com.jmml.gazege.core.dao.CategoryDao
import com.jmml.gazege.core.dao.PersonDao
import com.jmml.gazege.core.dao.PromissoryNoteDao
import com.jmml.gazege.core.dao.TransactionDao
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.Budget
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.NewTransactionWithDetails
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PromissoryNote
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionDetails
import com.jmml.gazege.core.entities.TransactionWithDetails
import com.jmml.gazege.core.entities.TransactionWithDetails.Companion.toTransactionDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(
    private val personDao: PersonDao,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val promissoryNoteDao: PromissoryNoteDao
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
    ): Flow<List<TransactionWithDetails>> {
        return transactionDao.getAll(startDate, endDate)
    }

    fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAll()
    }

    fun getBudgets(): Flow<List<Budget>> {
        return budgetDao.getAll()
    }

    fun getPromissoryNotes(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Flow<List<PromissoryNote>>{
        return promissoryNoteDao.getAll(startDate, endDate)
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
    suspend fun insertTransaction(vararg transaction: NewTransactionWithDetails) {
        transaction.forEach {
            val id = transactionDao.insert(it.toTransaction())
            transactionDao.insertAll(*it.toTransactionDetails(id.toInt()).toTypedArray())
        }
    }

    @WorkerThread
    suspend fun insertTransaction(vararg transaction: TransactionWithDetails) {
        transactionDao.insertAll(*transaction.map { it.toTransaction() }.toTypedArray())
        transactionDao.insertAll(*transaction.flatMap { it.toTransactionDetails() }.toTypedArray())
    }

    @WorkerThread
    suspend fun updateTransaction(vararg transaction: TransactionWithDetails) {
        transaction.forEach {
            it.toTransactionDetails().forEach { transactionDetails ->
                transactionDao.update(transactionDetails)
            }
            transactionDao.update(it.toTransaction())
        }
    }

    @WorkerThread
    suspend fun deleteTransaction(vararg transaction: Transaction) {
        transaction.forEach {
            transactionDao.delete(it)
        }
    }

    @WorkerThread
    suspend fun deleteTransactionDetails(vararg transaction: TransactionDetails) {
        transaction.forEach { transactionDao.delete(it) }
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
    suspend fun insertPromissoryNote(vararg promissoryNote: PromissoryNote){
        promissoryNoteDao.insertAll(*promissoryNote)
    }

    @WorkerThread
    suspend fun updatePromissoryNote(promissoryNote: PromissoryNote){
        promissoryNoteDao.update(promissoryNote)
    }

    @WorkerThread
    suspend fun deletePromissoryNote(promissoryNote: PromissoryNote){
        promissoryNoteDao.delete(promissoryNote)
    }

    @WorkerThread
    suspend fun deleteAllData() {
        accountDao.deleteAll()
        budgetDao.deleteAll()
        categoryDao.deleteAll()
        personDao.deleteAll()
        transactionDao.deleteAll()
        promissoryNoteDao.deleteAll()
    }
}