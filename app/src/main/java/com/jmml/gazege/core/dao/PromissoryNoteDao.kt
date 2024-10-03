package com.jmml.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jmml.gazege.core.entities.PromissoryNote
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PromissoryNoteDao {
    @Query("SELECT * FROM PromissoryNote")
    fun getAll(): Flow<List<PromissoryNote>>

    @Transaction
    @Query(
        """
            SELECT *
            FROM PromissoryNote
            WHERE
            (:startDate is null OR date >= :startDate) AND
            (:endDate is null OR date <= :endDate)
            ORDER BY date DESC, id DESC
        """
    )
    fun getAll(startDate: LocalDate?, endDate: LocalDate?): Flow<List<PromissoryNote>>

    @Insert
    suspend fun insertAll(vararg promissoryNotes: PromissoryNote): List<Long>

    @Update
    suspend fun update(promissoryNote: PromissoryNote)

    @Delete
    suspend fun delete(promissoryNote: PromissoryNote): Int

    @Query("DELETE FROM PromissoryNote")
    suspend fun deleteAll()
}