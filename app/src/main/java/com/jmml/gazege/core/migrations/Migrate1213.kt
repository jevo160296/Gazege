package com.jmml.gazege.core.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(
    tableName = "Budget",
    columnName = "BudgetType"
)
class MigrateSpec1213 : AutoMigrationSpec