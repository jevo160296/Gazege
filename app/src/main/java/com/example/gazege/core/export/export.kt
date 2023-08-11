package com.example.gazege.core.export

import com.example.gazege.ExportTransaction
import org.apache.commons.csv.CSVFormat
import java.io.InputStream
import java.io.OutputStream
import java.io.Writer
import java.time.LocalDate

private fun Writer.writeCsv(items: List<ExportTransaction>) {
    CSVFormat.EXCEL.print(this).apply {
        printRecord(
            "transactionId",
            "transactionAmount",
            "transactionDescription",
            "accountSourceName",
            "accountSourceOwnerName",
            "accountSourceOwnerImportance",
            "accountSourceIncludedInTotal",
            "accountSourceIsIncome",
            "accountSourceIsOutcome",
            "accountSourceParentAccountName",
            "accountSourceParentAccountOwnerName",
            "accountSourceParentAccountOwnerImportance",
            "accountSourceParentAccountIncludedInTotal",
            "accountSourceParentAccountIsIncome",
            "accountSourceParentAccountIsOutcome",
            "accountDestinationName",
            "accountDestinationOwnerName",
            "accountDestinationOwnerImportance",
            "accountDestinationIncludedInTotal",
            "accountDestinationIsIncome",
            "accountDestinationIsOutcome",
            "accountDestinationParentAccountName",
            "accountDestinationParentAccountOwnerName",
            "accountDestinationParentAccountOwnerImportance",
            "accountDestinationParentAccountIncludedInTotal",
            "accountDestinationParentAccountIsIncome",
            "accountDestinationParentAccountIsOutcome",
            "categoryName",
            "categoryParentName",
            "date",
            "aNombreDe"
        )
        items.forEach { transaction ->
            transaction.apply {
                printRecord(
                    transactionId,
                    transactionAmount,
                    transactionDescription,
                    accountSourceName,
                    accountSourceOwnerName,
                    accountSourceOwnerImportance,
                    accountSourceIncludedInTotal,
                    accountSourceIsIncome,
                    accountSourceIsOutcome,
                    accountSourceParentAccountName,
                    accountSourceParentAccountOwnerName,
                    accountSourceParentAccountOwnerImportance,
                    accountSourceParentAccountIncludedInTotal,
                    accountSourceParentAccountIsIncome,
                    accountSourceParentAccountIsOutcome,
                    accountDestinationName,
                    accountDestinationOwnerName,
                    accountDestinationOwnerImportance,
                    accountDestinationIncludedInTotal,
                    accountDestinationIsIncome,
                    accountDestinationIsOutcome,
                    accountDestinationParentAccountName,
                    accountDestinationParentAccountOwnerName,
                    accountDestinationParentAccountOwnerImportance,
                    accountDestinationParentAccountIncludedInTotal,
                    accountDestinationParentAccountIsIncome,
                    accountDestinationParentAccountIsOutcome,
                    categoryName,
                    categoryParentName,
                    date,
                    aNombreDe
                )
            }
        }
    }
}

fun writeCsv(outputStream: OutputStream, transactions: List<ExportTransaction>) =
    outputStream.use {
        it.writer().use { writer -> writer.writeCsv(transactions) }
    }

fun readTransactionsFromCsv(inputStream: InputStream): List<ExportTransaction> =
    CSVFormat.Builder.create(CSVFormat.EXCEL).apply {
        setIgnoreSurroundingSpaces(true)
    }.build().parse(inputStream.reader())
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
                    .map {
                        ExportTransaction(
                            transactionId = it[columnIndex["transactionId"] ?: 0].toIntOrNull(),
                            transactionAmount = it[columnIndex["transactionAmount"]
                                ?: 0].toDoubleOrNull() ?: 0.0,
                            transactionDescription = it[columnIndex["transactionDescription"] ?: 0],
                            accountSourceName = it[columnIndex["accountSourceName"] ?: 0],
                            accountSourceOwnerName = it[columnIndex["accountSourceOwnerName"] ?: 0],
                            accountSourceOwnerImportance = it[columnIndex["accountSourceOwnerImportance"]
                                ?: 0].toIntOrNull(),
                            accountSourceIncludedInTotal = it[columnIndex["accountSourceIncludedInTotal"]
                                ?: 0].toBoolean(),
                            accountSourceIsIncome = it[columnIndex["accountSourceIsIncome"]
                                ?: 0].toBoolean(),
                            accountSourceIsOutcome = it[columnIndex["accountSourceIsOutcome"]
                                ?: 0].toBoolean(),
                            accountSourceParentAccountName = it[columnIndex["accountSourceParentAccountName"]
                                ?: 0],
                            accountSourceParentAccountOwnerName = it[columnIndex["accountSourceParentAccountOwnerName"]
                                ?: 0],
                            accountSourceParentAccountOwnerImportance = it[columnIndex["accountSourceParentAccountOwnerImportance"]
                                ?: 0].toIntOrNull(),
                            accountSourceParentAccountIncludedInTotal = it[columnIndex["accountSourceParentAccountIncludedInTotal"]
                                ?: 0].toBoolean(),
                            accountSourceParentAccountIsIncome = it[columnIndex["accountSourceParentAccountIsIncome"]
                                ?: 0].toBoolean(),
                            accountSourceParentAccountIsOutcome = it[columnIndex["accountSourceParentAccountIsOutcome"]
                                ?: 0].toBoolean(),
                            accountDestinationName = it[columnIndex["accountDestinationName"] ?: 0],
                            accountDestinationOwnerName = it[columnIndex["accountDestinationOwnerName"]
                                ?: 0],
                            accountDestinationOwnerImportance = it[columnIndex["accountDestinationOwnerImportance"]
                                ?: 0].toIntOrNull(),
                            accountDestinationIncludedInTotal = it[columnIndex["accountDestinationIncludedInTotal"]
                                ?: 0].toBoolean(),
                            accountDestinationIsIncome = it[columnIndex["accountDestinationIsIncome"]
                                ?: 0].toBoolean(),
                            accountDestinationIsOutcome = it[columnIndex["accountDestinationIsOutcome"]
                                ?: 0].toBoolean(),
                            accountDestinationParentAccountName = it[columnIndex["accountDestinationParentAccountName"]
                                ?: 0],
                            accountDestinationParentAccountOwnerName = it[columnIndex["accountDestinationParentAccountOwnerName"]
                                ?: 0],
                            accountDestinationParentAccountOwnerImportance = it[columnIndex["accountDestinationParentAccountOwnerImportance"]
                                ?: 0].toIntOrNull(),
                            accountDestinationParentAccountIncludedInTotal = it[columnIndex["accountDestinationParentAccountIncludedInTotal"]
                                ?: 0].toBoolean(),
                            accountDestinationParentAccountIsIncome = it[columnIndex["accountDestinationParentAccountIsIncome"]
                                ?: 0].toBoolean(),
                            accountDestinationParentAccountIsOutcome = it[columnIndex["accountDestinationParentAccountIsOutcome"]
                                ?: 0].toBoolean(),
                            categoryName = it[columnIndex["categoryName"] ?: 0],
                            categoryParentName = it[columnIndex["categoryParentName"] ?: 0],
                            date = LocalDate.parse(it[columnIndex["date"] ?: 0]),
                            aNombreDe = it[columnIndex["aNombreDe"] ?: 0]
                        )
                    }
            }
        }
