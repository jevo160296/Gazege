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
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CreateBackupDocument : CreateDocument("application/gazip")

private fun getCSVFormat() = CSVFormat.EXCEL

fun realizeFormatter(sampleDates: Array<String>): DateTimeFormatter {
    val sampleDate = sampleDates.firstOrNull() ?: ""
    val divider =
        if (sampleDate.contains("-")) {
            "-"
        } else if (sampleDate.contains("/")) {
            "/"
        } else if (sampleDate.contains("\\")) {
            "\\"
        } else {
            null
        }
    val isFirstYear =
        if ("\\d{4}.*".toRegex().matchEntire(sampleDate) != null) {
            true
        } else if (".*\\d{4}".toRegex().matchEntire(sampleDate) != null) {
            false
        } else {
            null
        }
    if (divider != null && isFirstYear != null) {
        return if (isFirstYear) {
            DateTimeFormatterBuilder()
                .parseLenient()
                .parseCaseInsensitive()
                .appendPattern("yyyy${divider}MM${divider}dd")
                .toFormatter()
        } else {
            DateTimeFormatterBuilder()
                .parseLenient()
                .parseCaseInsensitive()
                .appendPattern("dd${divider}MM${divider}yyyy")
                .toFormatter()
        }
    } else {
        throw DateTimeParseException("Failed to resolve format: ", sampleDate, 0)
    }
}

fun parseDate(dateText: String, formatter: DateTimeFormatter): LocalDate {
    return LocalDate.parse(dateText, formatter)
}

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
        object {
            val id = record[columnIndex["id"] ?: 0]
            val amount = record[columnIndex["amount"] ?: 0]
            val description = record[columnIndex["description"] ?: 0]
            val sourceId = record[columnIndex["sourceId"] ?: 0]
            val destinationId = record[columnIndex["destinationId"] ?: 0]
            val categoryId = record[columnIndex["categoryId"] ?: 0]
            val date = record[columnIndex["date"] ?: 0]
            val aNombreDe = record[columnIndex["aNombreDe"] ?: 0]
        }
    }
        .let { items ->
            val formatter = realizeFormatter(items.take(30).map { it.date }.toTypedArray())
            items.map { item ->
                Transaction(
                    id = item.id.toIntOrNull(),
                    amount = item.amount.toDoubleOrNull() ?: 0.0,
                    description = item.description,
                    sourceId = item.sourceId.toIntOrNull() ?: 0,
                    destinationId = item.destinationId.toIntOrNull() ?: 0,
                    categoryId = item.categoryId.toIntOrNull(),
                    date = parseDate(item.date, formatter),
                    aNombreDe = item.aNombreDe.toIntOrNull()
                )
            }
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
        object {
            val id = record[columnIndex["id"] ?: 0]
            val categoryId = record[columnIndex["categoryId"] ?: 0]
            val value = record[columnIndex["value"] ?: 0]
            val each = record[columnIndex["each"] ?: 0]
            val frequency = record[columnIndex["frequency"] ?: 0]
            val frequencyType = record[columnIndex["frequencyType"] ?: 0]
            val budgetType = record[columnIndex["budgetType"] ?: 0]
            val startDate = record[columnIndex["startDate"] ?: 0]
            val description = record[columnIndex["description"] ?: 0]
        }
    }
        .let { items ->
            val formatter = realizeFormatter(items.take(30).map { it.startDate }.toTypedArray())
            items.map { item ->
                Budget(
                    id = item.id.toIntOrNull(),
                    categoryId = item.categoryId.toIntOrNull() ?: 0,
                    value = item.value.toDoubleOrNull() ?: 0.0,
                    each = item.each.toIntOrNull() ?: 0,
                    frequency = item.frequency.toIntOrNull() ?: 0,
                    frequencyType = FrequencyType.valueOf(item.frequencyType),
                    budgetType = BudgetType.valueOf(item.budgetType),
                    startDate = parseDate(item.startDate, formatter),
                    description = item.description
                )
            }
        }

fun readCategoryFromCsv(inputStream: InputStream): List<Category> =
    readFromCsv(inputStream) { record, columnIndex, _ ->
        Category(
            id = record[columnIndex["id"] ?: 0].toIntOrNull(),
            name = record[columnIndex["name"] ?: 0],
            budgetType = BudgetType.valueOf(
                record.elementAtOrElse(
                    columnIndex["budgetType"] ?: -1
                ) { "FIXED" }),
            parentId = record[columnIndex["parentId"] ?: 0].toIntOrNull()
        )
    }


