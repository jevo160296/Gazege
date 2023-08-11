package com.example.gazege.core.export

import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.BudgetType
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.FrequencyType
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.Writer
import java.time.LocalDate
import java.time.LocalTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CreateBackupDocument : CreateDocument("application/gazip")

private fun getCSVFormat() = CSVFormat.EXCEL

fun <T> writeCsv(
    outputStream: OutputStream,
    items: List<T>,
    writerFunc: Writer.(items: List<T>) -> Unit
) = outputStream.use {
    it.writer()
        .use { writer ->
            writer.writerFunc(items)
        }
}

fun writeTransactions(outputStream: OutputStream, transactions: List<Transaction>) =
    writeCsv(outputStream, transactions) { items ->
        var tick = LocalTime.now()
        var tack: LocalTime
        getCSVFormat()
            .print(this)
            .apply {
                printRecord(
                    "id",
                    "amount",
                    "description",
                    "sourceId",
                    "destinationId",
                    "categoryId",
                    "date",
                    "aNombreDe"
                )
                items.forEachIndexed { index, transaction ->
                    tack = LocalTime.now()
                    if (tack.minusSeconds(3) > tick) {
                        tick = LocalTime.now()
                    }
                    printRecord(
                        transaction.id,
                        transaction.amount,
                        transaction.description,
                        transaction.sourceId,
                        transaction.destinationId,
                        transaction.categoryId,
                        transaction.date,
                        transaction.aNombreDe
                    )
                }
            }
    }

fun writePersons(outputStream: OutputStream, persons: List<Person>) =
    writeCsv(outputStream, persons) { items ->
        getCSVFormat()
            .print(this)
            .apply {
                printRecord(
                    "id",
                    "name",
                    "importance"
                )
                items.forEach {
                    printRecord(
                        it.id,
                        it.name,
                        it.importance
                    )
                }
            }
    }

fun writeCategories(outputStream: OutputStream, categories: List<Category>) =
    writeCsv(outputStream, categories) { items ->
        getCSVFormat()
            .print(this)
            .apply {
                printRecord(
                    "id",
                    "name",
                    "parentId"
                )
                items.forEach {
                    printRecord(
                        it.id,
                        it.name,
                        it.parentId,
                    )
                }
            }
    }

fun writeAccounts(outputStream: OutputStream, accounts: List<Account>) =
    writeCsv(outputStream, accounts) { items ->
        getCSVFormat()
            .print(this)
            .apply {
                printRecord(
                    "id",
                    "name",
                    "ownerId",
                    "parentId",
                    "includedInTotal",
                    "isIncome",
                    "isOutcome"
                )
                items.forEach {
                    printRecord(
                        it.id,
                        it.name,
                        it.ownerId,
                        it.parentId,
                        it.includedInTotal,
                        it.isIncome,
                        it.isOutcome,
                    )
                }
            }
    }

fun writeBudget(outputStream: OutputStream, budget: List<Budget>) =
    writeCsv(outputStream, budget) { items ->
        getCSVFormat()
            .print(this)
            .apply {
                printRecord(
                    "id",
                    "categoryId",
                    "value",
                    "each",
                    "frequency",
                    "frequencyType",
                    "budgetType",
                    "startDate",
                    "description"
                )
                items.forEach {
                    printRecord(
                        it.id,
                        it.categoryId,
                        it.value,
                        it.each,
                        it.frequency,
                        it.frequencyType.name,
                        it.budgetType.name,
                        it.startDate,
                        it.description
                    )
                }
            }
    }

fun writeZipBackup(
    transactionsInputStream: InputStream,
    personsInputStream: InputStream,
    categoriesInputStream: InputStream,
    accountsInputStream: InputStream,
    budgetInputStream: InputStream,
    zipOutputStream: ZipOutputStream
) {
    zipOutputStream.use { output ->
        listOf(
            transactionsInputStream,
            personsInputStream,
            categoriesInputStream,
            budgetInputStream,
            accountsInputStream

        )
            .forEachIndexed { index, input ->
                input.use { usedInput ->
                    val name = when (index) {
                        0 -> "transacciones.csv"
                        1 -> "personas.csv"
                        2 -> "categorias.csv"
                        3 -> "presupuesto.csv"
                        4 -> "cuentas.csv"
                        else -> "unkown.csv"
                    }
                    BufferedInputStream(usedInput).use { input ->
                        output.putNextEntry(ZipEntry(name))
                        input.copyTo(output, 2048)
                    }
                }
            }
    }
}

fun <T> readFromCsv(
    inputStream: InputStream,
    converter: (record: CSVRecord, columnIndex: Map<String, Int>, index: Int) -> T
): List<T> =
    getCSVFormat().builder().build().parse(inputStream.reader())
        .use { parser ->
            val columns = parser.firstOrNull()
            if (columns == null) {
                emptyList()
            } else {
                val columnIndex = columns
                    .values()
                    .mapIndexed { index, s ->
                        object {
                            val index = index
                            val columnName = s
                        }
                    }
                    .associate {
                        Pair(it.columnName, it.index)
                    }
                parser
                    .mapIndexed { index, it ->
                        converter(it, columnIndex, index)
                    }
            }
        }

fun readTransactionsFromCsv(inputStream: InputStream): List<Transaction> =
    readFromCsv(inputStream) { record, columnIndex, _ ->
        Transaction(
            id = record[columnIndex["id"] ?: 0].toIntOrNull(),
            amount = record[columnIndex["amount"] ?: 0].toDoubleOrNull() ?: 0.0,
            description = record[columnIndex["description"] ?: 0],
            sourceId = record[columnIndex["sourceId"] ?: 0].toIntOrNull() ?: 0,
            destinationId = record[columnIndex["destinationId"] ?: 0].toIntOrNull() ?: 0,
            categoryId = record[columnIndex["categoryId"] ?: 0].toIntOrNull(),
            date = LocalDate.parse(record[columnIndex["date"] ?: 0]),
            aNombreDe = record[columnIndex["aNombreDe"] ?: 0].toIntOrNull()
        )
    }

fun readPersonsFromCsv(inputStream: InputStream): List<Person> =
    readFromCsv(inputStream) { record, columnIndex, _ ->
        Person(
            id = record[columnIndex["id"] ?: 0].toIntOrNull(),
            name = record[columnIndex["name"] ?: 0],
            importance = record[columnIndex["importance"] ?: 0].toIntOrNull()
        )
    }

fun readAccountFromCsv(inputStream: InputStream): List<Account> =
    readFromCsv(inputStream) { record, columnIndex, _ ->
        Account(
            id = record[columnIndex["id"] ?: 0].toIntOrNull(),
            name = record[columnIndex["name"] ?: 0],
            ownerId = record[columnIndex["ownerId"] ?: 0].toIntOrNull() ?: 0,
            parentId = record[columnIndex["parentId"] ?: 0].toIntOrNull(),
            includedInTotal = record[columnIndex["includedInTotal"] ?: 0].toBoolean(),
            isIncome = record[columnIndex["isIncome"] ?: 0].toBoolean(),
            isOutcome = record[columnIndex["isOutcome"] ?: 0].toBoolean()
        )
    }

fun readBudgetFromCsv(inputStream: InputStream): List<Budget> =
    readFromCsv(inputStream) { record, columnIndex, index ->
        Budget(
            id = record[columnIndex["id"] ?: 0].toIntOrNull(),
            categoryId = record[columnIndex["categoryId"] ?: 0].toIntOrNull() ?: 0,
            value = record[columnIndex["value"] ?: 0].toDoubleOrNull() ?: 0.0,
            each = record[columnIndex["each"] ?: 0].toIntOrNull() ?: 0,
            frequency = record[columnIndex["frequency"] ?: 0].toIntOrNull() ?: 0,
            frequencyType = FrequencyType.valueOf(record[columnIndex["frequencyType"] ?: 0]),
            budgetType = BudgetType.valueOf(record[columnIndex["budgetType"] ?: 0]),
            startDate = LocalDate.parse(record[columnIndex["startDate"] ?: 0]),
            description = record[columnIndex["description"] ?: 0]
        )
    }

fun readCategoryFromCsv(inputStream: InputStream): List<Category> =
    readFromCsv(inputStream) { record, columnIndex, _ ->
        Category(
            id = record[columnIndex["id"] ?: 0].toIntOrNull(),
            name = record[columnIndex["name"] ?: 0],
            parentId = record[columnIndex["parentId"] ?: 0].toIntOrNull()
        )
    }


