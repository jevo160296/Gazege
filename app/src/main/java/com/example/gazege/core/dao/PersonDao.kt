package com.example.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gazege.core.entities.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Transaction
    @Query("SELECT * " +
            "FROM Person " +
            "ORDER BY name")
    fun getAll(): Flow<List<Person>>

    @Insert
    suspend fun insertAll(vararg persons: Person): List<Long>

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person): Int
}