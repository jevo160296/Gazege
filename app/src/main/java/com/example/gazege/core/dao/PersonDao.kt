package com.example.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Transaction
    @Query("SELECT * FROM Person")
    fun getAll(): Flow<List<PersonWithAccounts>>

    @Insert
    suspend fun insertAll(vararg persons: Person): List<Long>

    @Delete
    suspend fun delete(person: Person): Int
}