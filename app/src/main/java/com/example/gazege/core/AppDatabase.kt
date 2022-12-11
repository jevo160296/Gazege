package com.example.gazege.core

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.dao.TransactionDao
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction

@Database(entities = [
    Person::class,
    Account::class,
    Transaction::class],
    version = 2
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                        .fallbackToDestructiveMigrationFrom(1)
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}