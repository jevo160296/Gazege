package com.example.gazege.core

import androidx.annotation.WorkerThread
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.dao.TransactionDao
import com.example.gazege.core.entities.*
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val personDao: PersonDao,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allPersons: Flow<List<PersonWithAccounts>> = personDao.getAll()
    val allAccounts: Flow<List<AccountAndOwnerWithTransactions>> = accountDao.getAll()
    val allTransactions: Flow<List<TransactionAndAccounts>> = transactionDao.getAll()

    @WorkerThread
    suspend fun insertPerson(person: Person) {
        personDao.insertAll(person)
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
    suspend fun insertAccountAndOwner(account: AccountAndOwner){
        accountDao.insert(account)
    }

    @WorkerThread
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertAll(transaction)
    }

    @WorkerThread
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction = transaction)
    }
}