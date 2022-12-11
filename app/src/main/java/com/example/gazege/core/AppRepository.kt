package com.example.gazege.core

import androidx.annotation.WorkerThread
import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.entities.Person
import kotlinx.coroutines.flow.Flow

class AppRepository(private val personDao: PersonDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allPersons: Flow<List<Person>> = personDao.getAll()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(person: Person) {
        personDao.insertAll(person)
    }

    @WorkerThread
    suspend fun delete(person: Person){
        personDao.delete(person)
    }
}