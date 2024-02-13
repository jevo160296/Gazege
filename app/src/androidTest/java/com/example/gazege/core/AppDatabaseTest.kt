package com.example.gazege.core

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gazege.core.entities.*
import com.example.gazege.ui.databaseSample
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.*
import kotlin.random.Random

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun initDatabase() = runTest{
        val random = Random(100)
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.getDatabase(appContext)

        val initialPersons = database.personDao().getAll().first()
        val maxIdPersons = initialPersons.maxOfOrNull {it.id ?: 0} ?: 0
        val personasIn = (1..20).map {
            Person(id = maxIdPersons + it, name = "Persona$it")
        }
        val Perinserted = database.personDao().insertAll(*personasIn.toTypedArray()).map { it.toInt() }
        val insertedSize = Perinserted.size
        val initialAccounts = database.accountDao().getAll().first()
        val maxIdAccounts = initialAccounts.maxOfOrNull { it.id ?: 0 } ?: 0
        val accountsIn = (1..20).map {
            val ownerId = Perinserted[random.nextInt(0, insertedSize)]
            Account(
                id = maxIdAccounts + it,
                name="Account$it",
                ownerId = ownerId
            )
        }
        val accInserted = database.accountDao().insertAll(*accountsIn.toTypedArray()).map{it.toInt()}
        val accInsertedSize = accInserted.size
        val initialTransactions = database.transactionDao().getAll().first()
        val maxIdTransaction = initialTransactions.maxOfOrNull{it.transaction.id ?: 0} ?: 0
        val transactionsIn = (1..200).map{
            val amount = random.nextDouble(1000.0, 200000.0)
            val dia = random.nextInt(1, 28)
            val mes = random.nextInt(1, 12)
            val anio = 2023
            val fecha = LocalDate.of(anio, mes, dia)
            val sourceId = accInserted[random.nextInt(0, accInsertedSize)]
            val destinationId = accInserted
                .filter{it != sourceId}[random.nextInt(0, accInsertedSize - 1)]
            Transaction(
                id = maxIdTransaction + it,
                amount = amount,
                description = "",
                sourceId = sourceId,
                destinationId = destinationId,
                date = fecha,
                categoryId = null,
                aNombreDe = null
            )
        }
        database.transactionDao().insertAll(*transactionsIn.toTypedArray())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun initDatabaseSmall() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.getDatabase(appContext)

        val persons = database.personDao().getAll().first()
        persons.forEach {
            database.personDao().delete(it)
        }
        database
            .categoryDao()
            .getAll()
            .first()
            .run { database.categoryDao().deleteAll(*this.toTypedArray()) }
        val newPersons = listOf(
            "Pablo",
            "Banco",
            "Petunia",
            "Hortensia",
            "__ESPECIAL__"
        ).mapIndexed { index, s ->
            Person(
                index, s, if (index == 0) {
                    1
                } else {
                    null
                }
            )
        }
        var index = 0
        val random = Random(3)
        val newAccounts: List<Account> = newPersons
            .flatMap {
                when (it.name) {
                    "Pablo" -> Pair(
                        0, (0..10).map {
                            val pIndex = index++
                            val hasParent = random.nextBoolean()
                            val parentId = if (hasParent && pIndex > 0) {
                                random.nextInt(pIndex)
                            } else {
                                null
                            }
                            Triple(pIndex, "Cuenta $pIndex", parentId)
                        }
                    )
                    "Banco" -> Pair(
                        1, listOf(
                            Triple(index++, "Banco", null)
                        )
                    )
                    "Petunia" -> Pair(
                        2, listOf(
                            Triple(index++, "Petunia", null)
                        )
                    )
                    "Hortensia" -> Pair(
                        3, listOf(
                            Triple(index++, "Hortensia", null)
                        )
                    )
                    "__ESPECIAL__" -> Pair(
                        4, listOf(
                            Triple(index++, "__INGRESOS__", null),
                            Triple(index++, "__GASTOS__", null)
                        )
                    )
                    else -> null
                }
                    .let { pair ->
                        if (pair != null) {
                            val ownerId = pair.first
                            val accounts = pair.second
                            accounts.map { triple ->
                                val isIncome = triple.second == "__INGRESOS__"
                                val isOutcome = triple.second == "__GASTOS__"
                                Account(
                                    triple.first,
                                    triple.second,
                                    ownerId,
                                    triple.third,
                                    isIncome = isIncome,
                                    isOutcome = isOutcome
                                )
                            }
                        } else {
                            listOf()
                        }
                    }
            }
        index = 0
        val newTransactions: List<Transaction> = (0..100).map {
            val from = newPersons
                .let {
                    val selected = random.nextInt(it.size)
                    it[selected].id
                }
            val to = newPersons
                .filter { it.id != from }
                .let {
                    val selected = random.nextInt(it.size)
                    it[selected].id
                }
            val sourceAccount = newAccounts
                .filter { it.ownerId == from }
                .let {
                    val selected = random.nextInt(it.size)
                    it[selected].id
                }
            val destinationAccount = newAccounts
                .filter { it.ownerId == to }
                .let {
                    val selected = random.nextInt(it.size)
                    it[selected].id
                }
            Transaction(
                index++,
                random.nextDouble(100.0, 500000.0),
                description = "",
                sourceId = sourceAccount ?: 0,
                destinationId = destinationAccount ?: 1,
                date = LocalDate.of(2023, 1, 1).plusDays(random.nextLong(365)),
                aNombreDe = null,
                categoryId = null
            )
        }
        val newCategories: Array<Category> = (0..90).map {
            val parentId = when (it) {
                4 -> 0
                6 -> 0
                7 -> 3
                8 -> 6
                else -> null
            }
            Category(it, "Category$it", BudgetType.FIXED, parentId)
        }.toTypedArray()
        database.personDao().insertAll(*newPersons.toTypedArray())
        database.accountDao().insertAll(*newAccounts.toTypedArray())
        database.transactionDao().insertAll(*newTransactions.toTypedArray())
        database.categoryDao().insertAll(*newCategories)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun initDateBaseFromSamples() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.getDatabase(appContext)

        val persons = database.personDao().getAll().first()
        persons.forEach {
            database.personDao().delete(it)
        }
        database
            .categoryDao()
            .getAll()
            .first()
            .run { database.categoryDao().deleteAll(*this.toTypedArray()) }

        databaseSample {
            this@runTest.launch {
                database.personDao().insertAll(*personSample.toTypedArray())
                database.accountDao().insertAll(*accountSample.toTypedArray())
                database.categoryDao()
                    .insertAll(*categorieSample.map { it.copy(parentId = null) }.toTypedArray())
                database.categoryDao().updateAll(*categorieSample.toTypedArray())
                database.transactionDao().insertAll(*transactionSample.toTypedArray())
                database.budgetDao().insertAll(*budgetSample.toTypedArray())
            }
        }
    }

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
        val maxId = initialPersons.maxOfOrNull { it.id ?: 0 } ?: 0
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
        val maxId = initialPersons.maxOfOrNull { it.id ?: 0 } ?: 0
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
            personasOut.map{it}.containsAll(personasIn)
        )
        personasIn.forEach {
            database.personDao().delete(it)
        }
        val remainingPersons = database.personDao().getAll().first()
        val allPersonsDeleted = !remainingPersons.map{it}.containsAll(personasIn)
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
                it.id ?: -1
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
            Account(ownerId = persons[0].id ?: -1, name = "Cuenta4"),
            Account(ownerId = persons[0].id ?: -1, name = "Cuenta5"),
            Account(ownerId = persons[0].id ?: -1, name = "Cuenta6"),
            Account(ownerId = persons[0].id ?: -1, name = "Cuenta7"),
            Account(ownerId = persons[0].id ?: -1, name = "Cuenta8"),
            Account(ownerId = persons[0].id ?: -1, name = "Cuenta9"),
            Account(ownerId = persons[1].id ?: -1, name = "Cuenta10"),
            Account(ownerId = persons[1].id ?: -1, name = "Cuenta11"),
            Account(ownerId = persons[1].id ?: -1, name = "Cuenta12"),
            Account(ownerId = persons[1].id ?: -1, name = "Cuenta13"),
            Account(ownerId = persons[1].id ?: -1, name = "Cuenta14"),
            Account(ownerId = persons[1].id ?: -1, name = "Cuenta15"),
            Account(ownerId = persons[1].id ?: -1, name = "Cuenta16")
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
        val maxId = persons.maxOfOrNull { it.id ?: -1 } ?: -1

        val accountToAdd = Account(ownerId = maxId + 1, name = "")
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
            val minId = database.personDao().getAll().first().minOfOrNull { it.id ?: 0 } ?: 0
            val accountsToAdd = arrayOf(
                Account(
                    name = "Cuenta1",
                    ownerId = minId
                ),
                Account(
                    name = "Cuenta2",
                    ownerId = minId
                ),
                Account(
                    name = "Cuenta3",
                    ownerId = minId
                )
            )
            database.accountDao().insertAll(*accountsToAdd)
        }
        val accounts = database.accountDao().getAll().first()
        val transactionsToAdd = arrayOf(
            Transaction(
                amount = 10.0,
                description = "Test",
                sourceId = accounts[0].id ?: -1,
                destinationId = accounts[1].id ?: -1,
                date = LocalDate.now(),
                categoryId = null,
                aNombreDe = null
            ),
            Transaction(
                amount = 10.0,
                description = "Test",
                sourceId = accounts[1].id ?: -1,
                destinationId = accounts[0].id ?: -1,
                date = LocalDate.now(),
                aNombreDe = null,
                categoryId = null
            ),
            Transaction(
                amount = 10.0,
                description = "Test",
                sourceId = accounts[0].id ?: -1,
                destinationId = accounts[1].id ?: -1,
                date = LocalDate.now(),
                categoryId = null,
                aNombreDe = null
            )
        )
        database.transactionDao().insertAll(*transactionsToAdd)
        val transactions = database.transactionDao().getAll().first()
        assertEquals(initialTransactions.size + transactionsToAdd.size, transactions.size)
    }
}