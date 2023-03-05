package com.example.gazege.core

import androidx.annotation.WorkerThread
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.dao.TransactionDao
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(
    private val personDao: PersonDao,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getPersons(): Flow<List<PersonWithAccounts>> {
        return personDao.getAll()
    }

    fun getAccounts(): Flow<List<AccountAndOwnerWithTransactions>> {
        return accountDao.getAll()
    }

    fun getTransactions(
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Flow<List<TransactionAndAccounts>> {
        return transactionDao.getAll(startDate, endDate)
    }

    @WorkerThread
    suspend fun insertPerson(person: Person) {
        personDao.insertAll(person)
    }

    @WorkerThread
    suspend fun updatePerson(person: Person) {
        personDao.update(person)
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
    suspend fun insertAccount(account: Account) {
        accountDao.insertAll(account)
    }

    @WorkerThread
    suspend fun updateAccount(account: Account) {
        accountDao.update(account)
    }

    @WorkerThread
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertAll(transaction)
    }

    @WorkerThread
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    @WorkerThread
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction = transaction)
    }
}