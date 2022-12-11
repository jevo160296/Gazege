package com.example.gazege

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gazege.core.entities.Person
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    @Test
    fun createDataBase(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database: AppDatabase = AppDatabase.getDatabase(appContext)
        database.assertNotMainThread()
    }

    @Test
    fun addAndGetPersons(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database: AppDatabase = AppDatabase.getDatabase(appContext)
        val initialPersons = database.personDao().getAll()
        val maxId = initialPersons.maxOfOrNull { it.id?: 0 } ?: 0
        val personasIn = arrayListOf(
            Person(id=maxId + 1, name="Persona1"),
            Person(id=maxId + 2, name="Persona2")
        )
        val personasOut = with(database.personDao()){
            this.insertAll(*personasIn.toTypedArray())
            this.getAll()
        }
        assertEquals("Personas diferentes",
            personasIn,
            personasOut.filter { !initialPersons.contains(it) }
        )
    }

    @Test
    fun deletePersons(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database: AppDatabase = AppDatabase.getDatabase(appContext)
        val initialPersons = database.personDao().getAll()
        val maxId = initialPersons.maxOfOrNull { it.id?: 0 } ?: 0
        val personasIn = arrayListOf(
            Person(id=maxId + 1, name="Persona1"),
            Person(id=maxId + 2, name="Persona2")
        )
        val personasOut = with(database.personDao()){
            this.insertAll(*personasIn.toTypedArray())
            this.getAll().toList()
        }
        assertTrue(
            "Error al añadir personas, no se puede probar esta función",
            personasOut.containsAll(personasIn)
        )
        personasIn.forEach {
            database.personDao().delete(it)
        }
        val remainingPersons = database.personDao().getAll()
        val allPersonsDeleted = !remainingPersons.containsAll(personasIn)
        assertTrue(
            "Error al eliminar personas.",
            allPersonsDeleted
        )
    }
}