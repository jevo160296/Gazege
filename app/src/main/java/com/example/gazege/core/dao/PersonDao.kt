package com.example.gazege.core.dao

import android.app.Person
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PersonDao {
    @Query("SELECT * FROM Person")
    fun getAll(): List<Person>

    @Insert
    fun insertAll(vararg persons: Person)

    @Delete
    fun delete(person: Person)
}