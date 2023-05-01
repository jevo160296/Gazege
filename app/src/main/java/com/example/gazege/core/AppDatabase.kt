package com.example.gazege.core

import android.content.Context
import androidx.room.*
import com.example.gazege.core.converters.Converters
import com.example.gazege.core.dao.*
import com.example.gazege.core.entities.*
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.migrations.Migrate34
import com.example.gazege.core.migrations.MigrateSpec56

@Database(
    entities = [
        Person::class,
        Account::class,
        Transaction::class,
        Category::class,
        Budget::class
    ],
    version = 11,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, MigrateSpec56::class),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(8, 9),
        AutoMigration(9, 10),
        AutoMigration(10, 11)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun budgetDao(): BudgetDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                        .fallbackToDestructiveMigrationFrom(1)
                        .addMigrations(Migrate34())
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}