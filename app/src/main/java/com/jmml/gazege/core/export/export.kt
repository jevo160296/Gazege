package com.jmml.gazege.core.export

import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.Budget
import com.jmml.gazege.core.entities.BudgetType
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.core.entities.FrequencyType
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PromissoryNote
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionAndDetails
import com.jmml.gazege.core.entities.TransactionDetails
import com.jmml.gazege.core.entities.TransactionWithDetails
import com.jmml.zoo.extensions.closedrange.toSequence
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

fun writeTransactions(outputStream: OutputStream, transactions: List<TransactionWithDetails>) =
    writeCsv(outputStream, transactions) { items ->
        var tick = LocalTime.now()
        var tack: LocalTime
        getCSVFormat()
            .print(this)
            .apply {
                printRecord(
                    "transactionId",
                    "transactionDetailsId",
                    "amount",
                    "description",
                    "sourceId",
                    "destinationId",
                    "categoryId",
                    "date",
                    "aNombreDe",
                    "budgetDate"
                )
                TransactionAndDetails.from(items).forEachIndexed { index, transaction ->
                    tack = LocalTime.now()
                    if (tack.minusSeconds(3) > tick) {
                        tick = LocalTime.now()
                    }
                    printRecord(
                        transaction.transactionId,
                        transaction.transactionDetailsId,
                        transaction.amount,
                        transaction.description,
                        transaction.sourceId,
                        transaction.destinationId,
                        transaction.categoryId,
                        transaction.date,
                        transaction.aNombreDe,
                        transaction.budgetDate
                    )
                }
            }
    }

fun writePromissoryNotes(outputStream: OutputStream, promissoryNotes: List<PromissoryNote>) {
    writeCsv(outputStream, promissoryNotes) { items ->
        getCSVFormat()
            .print(this)
            .apply {
                printRecord(
                    "id",
                    "amount",
                    "date",
                    "sourceId",
                    "destinationId",
                    "description"
                )
                items.forEach { note ->
                    printRecord(
                        note.id,
                        note.amount,
                        note.date,
                        note.sourceId,
                        note.destinationId,
                        note.description
                    )
                }
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
                    "importance",
                    "debtsIncludedInTotal"
                )
                items.forEach {
                    printRecord(
                        it.id,
                        it.name,
                        it.importance,
                        it.debtsIncludedInTotal
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
                    "parentId",
                    "budgetType"
                )
                items.forEach {
                    printRecord(
                        it.id,
                        it.name,
                        it.parentId,
                        it.budgetType
                    )
                }
            }
    }

fun writeCategoriesWithCalculatedData(
    outputStream: OutputStream,
    categories: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>
) =
    writeCsv(outputStream, categories) { items ->
        getCSVFormat()
            .print(this)
            .apply {
                printRecord(
                    "Category name",
                    "Date",
                    "pastForecast",
                    "futureForecast",
                    "Left to pay",
                    "Left to pay today",
                    "realTotalFlowSeries",
                    "realTotalFlowTodaySeries",
                    "realTotalFlowUntilTodaySeries",
                    "expectedFlowFromTodaySeries",
                    "expectedFlowFromTomorrowSeries",
                    "expectedFlowTodaySeries",
                    "expectedFlowUntilTodaySeries",
                    "dailyValueTimeSeries",
                    "transactionsTimeSeries",
                    "accumulatedDailyValueTimeSeries",
                    "accumulatedTransactionsTimeSeries",
                    "forecastedTransactionsTimeSeries",
                    "currentDate"
                )
                items.forEach { category ->
                    val dates = category.dateRange?.toSequence { it.plusDays(1) }
                    val categoryName = category.category.name
                    val pastForecast = category.pastForecast
                    val futureForecast = category.futureForecast
                    val leftToPaySeries = category.leftToPaySeries
                    val leftToPayTodaySeries = category.leftToPayTodaySeries
                    val realTotalFlowSeries = category.category.realTotalFlowSeries
                    val realTotalFlowTodaySeries = category.category.realTotalFlowTodaySeries
                    val realTotalFlowUntilTodaySeries =
                        category.category.realTotalFlowUntilTodaySeries
                    val expectedFlowFromTodaySeries =
                        category.aggregatedBudget.expectedFlowFromTodaySeries
                    val expectedFlowFromTomorrowSeries =
                        category.aggregatedBudget.expectedFlowFromTomorrowSeries
                    val expectedFlowTodaySeries = category.aggregatedBudget.expectedFlowTodaySeries
                    val expectedFlowUntilTodaySeries =
                        category.aggregatedBudget.expectedFlowUntilTodaySeries
                    val dailyValueTimeSeries = category.expectedFlowTodaySeries
                    val transactionsTimeSeries = category.realTotalFlowTodaySeries
                    val accumulatedDailyValueTimeSeries =
                        category.accumulatedExpectedFlowTodaySeries
                    val accumulatedTransactionsTimeSeries =
                        category.accumulatedRealTotalFlowTodaySeries
                    val forecastedTransactionsTimeSeries = category.forecastedTransactionsTimeSeries

                    dates?.forEach { today ->
                        printRecord(
                            categoryName,
                            today,
                            pastForecast[today],
                            futureForecast[today],
                            leftToPaySeries[today],
                            leftToPayTodaySeries[today],
                            realTotalFlowSeries[today],
                            realTotalFlowTodaySeries[today],
                            realTotalFlowUntilTodaySeries[today],
                            expectedFlowFromTodaySeries[today],
                            expectedFlowFromTomorrowSeries[today],
                            expectedFlowTodaySeries[today],
                            expectedFlowUntilTodaySeries[today],
                            dailyValueTimeSeries[today],
                            transactionsTimeSeries[today],
                            accumulatedDailyValueTimeSeries[today],
                            accumulatedTransactionsTimeSeries[today],
                            forecastedTransactionsTimeSeries[today],
                            category.currentDate
                        )
                    }
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
                        it.startDate,
                        it.description
                    )
                }
            }
    }

fun writeZipBackup(
    transactionsInputStream: InputStream,
    promissoryNotesInputStream: InputStream,
    personsInputStream: InputStream,
    categoriesInputStream: InputStream,
    accountsInputStream: InputStream,
    budgetInputStream: InputStream,
    zipOutputStream: ZipOutputStream
) {
    zipOutputStream.use { output ->
        listOf(
            transactionsInputStream,
            promissoryNotesInputStream,
            personsInputStream,
            categoriesInputStream,
            budgetInputStream,
            accountsInputStream
        )
            .forEachIndexed { index, input ->
                input.use { usedInput ->
                    val name = when (index) {
                        0 -> "transacciones.csv"
                        1 -> "promissorynotes.csv"
                        2 -> "personas.csv"
                        3 -> "categorias.csv"
                        4 -> "presupuesto.csv"
                        5 -> "cuentas.csv"
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

fun readTransactionsFromCsv(inputStream: InputStream): List<TransactionWithDetails> =
    readFromCsv(inputStream) { record, columnIndex, _ ->
        val hasId = columnIndex["id"] != null
        val hasBudgetDate = columnIndex["budgetDate"] != null
        object {
            val transactionId =
                if (hasId) record[columnIndex["id"] ?: 0] else record[columnIndex["transactionId"]
                    ?: 0]
            val transactionDetailsId = if (hasId) record[columnIndex["id"]
                ?: 0] else record[columnIndex["transactionDetailsId"] ?: 0]
            val amount = record[columnIndex["amount"] ?: 0]
            val description = record[columnIndex["description"] ?: 0]
            val sourceId = record[columnIndex["sourceId"] ?: 0]
            val destinationId = record[columnIndex["destinationId"] ?: 0]
            val categoryId = record[columnIndex["categoryId"] ?: 0]
            val date = record[columnIndex["date"] ?: 0]
            val aNombreDe = record[columnIndex["aNombreDe"] ?: 0]
            val budgetDate = if (hasBudgetDate) record[columnIndex["budgetDate"] ?: 0] else null
        }
    }
        .let { items ->
            val formatter = realizeFormatter(items.take(30).map { it.date }.toTypedArray())
            items
                .groupBy { it.transactionId }
                .map { (transactionId, items) ->
                    val firstTransaction = items.first()
                    val transaction = Transaction(
                        id = transactionId.toIntOrNull() ?: 0,
                        date = parseDate(firstTransaction.date, formatter),
                        sourceId = firstTransaction.sourceId.toIntOrNull() ?: 0,
                        destinationId = firstTransaction.destinationId.toIntOrNull() ?: 0
                    )
                    val transactionDetails = items.map { item ->
                        TransactionDetails(
                            transactionId = transaction.id ?: 0,
                            id = item.transactionDetailsId.toIntOrNull() ?: 0,
                            amount = item.amount.toDoubleOrNull() ?: 0.0,
                            description = item.description,
                            categoryId = item.categoryId.toIntOrNull(),
                            aNombreDe = item.aNombreDe.toIntOrNull(),
                            budgetDate = item.budgetDate?.takeIf { it.isNotEmpty() }
                                ?.let { parseDate(it, formatter) }
                        )
                    }
                    TransactionWithDetails.from(transaction, transactionDetails)
                }
        }

fun readPromissoryNotesFromCsv(inputStream: InputStream): List<PromissoryNote> =
    readFromCsv(inputStream) { record, columnIndex, _ ->
        object {
            val id = record[columnIndex["id"] ?: 0]
            val amount = record[columnIndex["amount"] ?: 0]
            val date = record[columnIndex["date"] ?: 0]
            val sourceId = record[columnIndex["sourceId"] ?: 0]
            val destinationId = record[columnIndex["destinationId"] ?: 0]
            val description = record[columnIndex["description"] ?: 0]
        }
    }
        .let { items ->
            val formatter = realizeFormatter(items.take(30).map { it.date }.toTypedArray())
            items.map { item ->
                PromissoryNote(
                    id = item.id.toIntOrNull(),
                    amount = item.amount.toDoubleOrNull() ?: 0.0,
                    date = parseDate(item.date, formatter),
                    sourceId = item.sourceId.toIntOrNull() ?: 0,
                    destinationId = item.destinationId.toIntOrNull() ?: 0,
                    description = item.description
                )
            }
        }

fun readPersonsFromCsv(inputStream: InputStream): List<Person> =
    readFromCsv(inputStream) { record, columnIndex, _ ->
        Person(
            id = record[columnIndex["id"] ?: 0].toIntOrNull(),
            name = record[columnIndex["name"] ?: 0],
            importance = record[columnIndex["importance"] ?: 0].toIntOrNull(),
            debtsIncludedInTotal = columnIndex["debtsIncludedInTotal"]?.let { record[it] }
                ?.lowercase()?.toBooleanStrictOrNull() ?: true
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


