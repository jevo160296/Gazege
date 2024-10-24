package com.jmml.gazege.core.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migrate1516 : Migration(15, 16) {
    private fun createDetailTable(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            database.execSQL(
                """
                CREATE TABLE TransactionDetails (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT, 
                    transactionId INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    description TEXT NOT NULL,
                    categoryId INTEGER,
                    aNombreDe INTEGER,
                    FOREIGN KEY(`aNombreDe`) 
                    REFERENCES `Person`(`id`) 
                    ON UPDATE NO ACTION 
                    ON DELETE SET NULL 
                    DEFERRABLE INITIALLY DEFERRED, 
                    FOREIGN KEY(`categoryId`) 
                    REFERENCES `Category`(`id`) 
                    ON UPDATE NO ACTION 
                    ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED,
                    FOREIGN KEY(`transactionId`) 
                    REFERENCES `Transaction`(`id`) 
                    ON UPDATE NO ACTION 
                    ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
                );
            """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO TransactionDetails (transactionId, amount, description, categoryId, aNombreDe)
                SELECT id, amount, description, categoryId, aNombreDe
                FROM `transaction`;
            """.trimIndent()
            )
            database.setTransactionSuccessful()
        } catch (e: Throwable) {
            throw e
        } finally {
            database.endTransaction()
        }
    }

    private fun dropTransactionsColumns(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            database.execSQL("PRAGMA foreign_keys=off;")
            database.execSQL("ALTER TABLE `transaction` RENAME TO `transaction_old`;")

            database.execSQL(
                """
                CREATE TABLE `Transaction` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT, 
                    `sourceId` INTEGER NOT NULL, 
                    `destinationId` INTEGER NOT NULL, 
                    `date` INTEGER NOT NULL, 
                    FOREIGN KEY(`destinationId`) 
                    REFERENCES `Account`(`id`) 
                    ON UPDATE NO ACTION 
                    ON DELETE CASCADE 
                    DEFERRABLE INITIALLY DEFERRED, 
                    FOREIGN KEY(`sourceId`) 
                    REFERENCES `Account`(`id`) 
                    ON UPDATE
                    NO ACTION 
                    ON DELETE CASCADE 
                    DEFERRABLE INITIALLY DEFERRED)
            """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO `transaction` (id, sourceId, destinationId, date)
                SELECT id, sourceId, destinationId, date
                FROM `transaction_old`;
            """.trimIndent()
            )
            database.execSQL("DROP TABLE `transaction_old`;")
            database.execSQL("PRAGMA foreign_keys=on;")
            database.setTransactionSuccessful()
        } catch (e: Throwable) {
            throw e
        } finally {
            database.endTransaction()
        }
    }

    override fun migrate(database: SupportSQLiteDatabase) {
        createDetailTable(database)
        dropTransactionsColumns(database)
    }
}