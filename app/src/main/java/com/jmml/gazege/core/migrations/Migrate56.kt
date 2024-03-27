package com.jmml.gazege.core.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(
    tableName = "Account",
    columnName = "initial_balance"
)
class MigrateSpec56 : AutoMigrationSpec