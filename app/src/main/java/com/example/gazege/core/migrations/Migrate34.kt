package com.example.gazege.core.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migrate34: Migration(3, 4){
    private fun migratePerson(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try{
            database.execSQL("""
                CREATE TABLE person_bckp (
                    id INTEGER,
                    name TEXT
                );
                """.trimIndent())
            database.execSQL("""
            INSERT INTO person_bckp (id, name)
               WITH 
                 new_names AS
                 (
                   SELECT id, name, ROW_NUMBER() OVER (PARTITION BY name ORDER BY id) as rn
                   FROM person
                 )
                 SELECT 
                    person.id,
                    CASE (new_names.rn > 1)
                    WHEN TRUE THEN new_names.name || "_" || CAST(new_names.rn - 1 AS STRING)
                    WHEN FALSE THEN new_names.name
                    END AS name
                 FROM person
                 INNER JOIN new_names on person.id = new_names.id;
        """.trimIndent())

            database.execSQL("""
            DELETE FROM person;
        """.trimIndent())
            database.execSQL("CREATE UNIQUE INDEX index_Person_name ON person(name);")
            database.execSQL("""
                INSERT INTO person (id, name)
                SELECT id, name
                FROM person_bckp;
            """.trimIndent())
            database.execSQL("DROP TABLE person_bckp;")
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    private fun migrateAccounts(database: SupportSQLiteDatabase){
        database.beginTransaction()
        try{
            database.execSQL("""
                CREATE TABLE account_bckp (
                    id INTEGER,
                    name TEXT,
                    ownerId INTEGER,
                    initial_balance REAL
                );
                """.trimIndent())
            database.execSQL("""
            INSERT INTO account_bckp (id, name, ownerId, initial_balance)
               WITH
                    rn AS 
                    (
                        SELECT id, name, ownerid, ROW_NUMBER() OVER (PARTITION BY name, ownerid ORDER BY id) as rn
                        FROM account
                    )
                SELECT 
                    account.id,
                    CASE (rn.rn > 1)
                        WHEN TRUE THEN account.name || "_" || CAST(rn.rn - 1 AS STRING)
                        WHEN FALSE THEN account.name
                    END as name,
                    account.ownerId,
                    account.initial_balance
                FROM account INNER JOIN rn on account.id = rn.id;
        """.trimIndent())

            database.execSQL("""
            DELETE FROM account;
        """.trimIndent())
            database.execSQL("CREATE UNIQUE INDEX index_Account_name_ownerId ON account(name, ownerId);")
            database.execSQL("""
                INSERT INTO account (id, name, ownerId, initial_balance)
                SELECT id, name, ownerId, initial_balance
                FROM account_bckp;
            """.trimIndent())
            database.execSQL("DROP TABLE account_bckp;")
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    override fun migrate(database: SupportSQLiteDatabase) {
        migratePerson(database)
        migrateAccounts(database)
    }

}