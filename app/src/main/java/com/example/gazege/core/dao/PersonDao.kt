package com.example.gazege.core.dao

import com.example.gazege.core.entities.Person
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM Person")
    fun getAll(): Flow<List<Person>>

    @Insert
    fun insertAll(vararg persons: Person)

    @Delete
    fun delete(person: Person)
}