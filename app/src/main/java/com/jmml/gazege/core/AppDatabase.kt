package com.jmml.gazege.core

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jmml.gazege.core.converters.Converters
import com.jmml.gazege.core.dao.AccountDao
import com.jmml.gazege.core.dao.BudgetDao
import com.jmml.gazege.core.dao.CategoryDao
import com.jmml.gazege.core.dao.PersonDao
import com.jmml.gazege.core.dao.TransactionDao
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.Budget
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.migrations.Migrate34
import com.jmml.gazege.core.migrations.MigrateSpec56

@Database(
    entities = [
        Person::class,
        Account::class,
        Transaction::class,
        Category::class,
        Budget::class
    ],
    version = 14,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, MigrateSpec56::class),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(8, 9),
        AutoMigration(9, 10),
        AutoMigration(10, 11),
        AutoMigration(11, 12),
        AutoMigration(12, 13),
        AutoMigration(13, 14)
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