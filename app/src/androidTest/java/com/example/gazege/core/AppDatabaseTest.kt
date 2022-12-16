package com.example.gazege.core

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    @Test
    fun createDataBase() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database: AppDatabase = AppDatabase.getDatabase(appContext)
        database.assertNotMainThread()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addAndGetPersons() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database: AppDatabase = AppDatabase.getDatabase(appContext)
        val initialPersons = database.personDao().getAll().first()
        val maxId = initialPersons.maxOfOrNull { it.person.id ?: 0 } ?: 0
        val personasIn = arrayListOf(
            Person(id = maxId + 1, name = "Persona1"),
            Person(id = maxId + 2, name = "Persona2")
        )
        val personasOut = with(database.personDao()) {
            this.insertAll(*personasIn.toTypedArray())
            this.getAll().first()
        }
        assertEquals("Personas diferentes",
            personasIn,
            personasOut.filter { !initialPersons.contains(it) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deletePersons() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database: AppDatabase = AppDatabase.getDatabase(appContext)
        val initialPersons = database.personDao().getAll().first()
        val maxId = initialPersons.maxOfOrNull { it.person.id ?: 0 } ?: 0
        val personasIn = arrayListOf(
            Person(id = maxId + 1, name = "Persona1"),
            Person(id = maxId + 2, name = "Persona2")
        )
        val personasOut = with(database.personDao()) {
            this.insertAll(*personasIn.toTypedArray())
            this.getAll().first()
        }
        assertTrue(
            "Error al añadir personas, no se puede probar esta función",
            personasOut.map{it.person}.containsAll(personasIn)
        )
        personasIn.forEach {
            database.personDao().delete(it)
        }
        val remainingPersons = database.personDao().getAll().first()
        val allPersonsDeleted = !remainingPersons.map{it.person}.containsAll(personasIn)
        assertTrue(
            "Error al eliminar personas.",
            allPersonsDeleted
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addAndGetAccount() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database: AppDatabase = AppDatabase.getDatabase(appContext)

        val persons = mutableListOf(*database.personDao().getAll().first().toTypedArray())
        if (persons.size < 2) {
            val maxId = database.personDao().getAll().first().maxOfOrNull {
                it.person.id ?: -1
            } ?: -1
            val personsToAdd = arrayOf(
                Person(id = maxId + 1, name = "Persona 1"),
                Person(id = maxId + 2, name = "Persona 2")
            )
            database.personDao().insertAll(*personsToAdd)
        }
        persons.clear()
        persons.addAll(database.personDao().getAll().first())
        val initialAccounts = database.accountDao().getAll().first()
        val accountsToAdd = arrayOf(
            Account(ownerId = persons[0].person.id ?: -1, initial_balance = 0.0, name = "Cuenta4"),
            Account(ownerId = persons[0].person.id ?: -1, initial_balance = 0.1, name = "Cuenta5"),
            Account(ownerId = persons[0].person.id ?: -1, initial_balance = 0.2, name = "Cuenta6"),
            Account(ownerId = persons[0].person.id ?: -1, initial_balance = 0.3, name = "Cuenta7"),
            Account(ownerId = persons[0].person.id ?: -1, initial_balance = 0.4, name = "Cuenta8"),
            Account(ownerId = persons[0].person.id ?: -1, initial_balance = 0.5, name = "Cuenta9"),
            Account(ownerId = persons[1].person.id ?: -1, initial_balance = 1.0, name = "Cuenta10"),
            Account(ownerId = persons[1].person.id ?: -1, initial_balance = 1.1, name = "Cuenta11"),
            Account(ownerId = persons[1].person.id ?: -1, initial_balance = 1.2, name = "Cuenta12"),
            Account(ownerId = persons[1].person.id ?: -1, initial_balance = 1.3, name = "Cuenta13"),
            Account(ownerId = persons[1].person.id ?: -1, initial_balance = 1.4, name = "Cuenta14"),
            Account(ownerId = persons[1].person.id ?: -1, initial_balance = 1.5, name = "Cuenta15"),
            Account(ownerId = persons[1].person.id ?: -1, initial_balance = 1.6, name = "Cuenta16")
        )
        database.accountDao().insertAll(*accountsToAdd)
        val accounts = database.accountDao().getAll().first()
        assert(initialAccounts.size + accountsToAdd.size == accounts.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addAccountWithNonexistentPersonThrowsError() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database: AppDatabase = AppDatabase.getDatabase(appContext)

        val persons = database.personDao().getAll().first()
        val maxId = persons.maxOfOrNull { it.person.id ?: -1 } ?: -1

        val accountToAdd = Account(ownerId = maxId + 1, initial_balance = 0.0, name = "")
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                database.accountDao().insertAll(accountToAdd)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addAndGetTransactions() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database: AppDatabase = AppDatabase.getDatabase(appContext)

        val initialTransactions = database.transactionDao().getAll().first()
        val initialAccounts = database.accountDao().getAll().first()
        if (initialAccounts.size < 2) {
            val persons = database.personDao().getAll().first()
            if (persons.isEmpty()) {
                database.personDao().insertAll(Person(name = "Persona"))
            }
            val minId = database.personDao().getAll().first().minOfOrNull { it.person.id ?: 0 } ?: 0
            val accountsToAdd = arrayOf(
                Account(
                    name = "Cuenta1",
                    ownerId = minId,
                    initial_balance = 1.0
                ),
                Account(
                    name = "Cuenta2",
                    ownerId = minId,
                    initial_balance = 1.0
                ),
                Account(
                    name = "Cuenta3",
                    ownerId = minId,
                    initial_balance = 1.0
                )
            )
            database.accountDao().insertAll(*accountsToAdd)
        }
        val accounts = database.accountDao().getAll().first()
        val transactionsToAdd = arrayOf(
            Transaction(
                amount = 10.0,
                description = "Test",
                sourceId = accounts[0].account.id ?: -1,
                destinationId = accounts[1].account.id ?: -1,
                date = Date()
            ),
            Transaction(
                amount = 10.0,
                description = "Test",
                sourceId = accounts[1].account.id ?: -1,
                destinationId = accounts[0].account.id ?: -1,
                date = Date()
            ),
            Transaction(
                amount = 10.0,
                description = "Test",
                sourceId = accounts[0].account.id ?: -1,
                destinationId = accounts[1].account.id ?: -1,
                date = Date()
            )
        )
        database.transactionDao().insertAll(*transactionsToAdd)
        val transactions = database.transactionDao().getAll().first()
        assertEquals(initialTransactions.size + transactionsToAdd.size, transactions.size)
    }
}