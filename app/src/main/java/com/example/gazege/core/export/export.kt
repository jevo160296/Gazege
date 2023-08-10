package com.example.gazege.core.export

import com.example.gazege.ExportTransaction
import org.apache.commons.csv.CSVFormat
import java.io.InputStream
import java.io.OutputStream
import java.io.Writer

data class ExportedItem(
    val name: String,
    val value: Int
)

private fun Writer.writeCsv(items: List<ExportTransaction>) {
    CSVFormat.DEFAULT.print(this).apply {
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

fun readCsv(inputStream: InputStream): List<ExportedItem> =
    CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
        setIgnoreSurroundingSpaces(true)
    }.build().parse(inputStream.reader())
        .drop(1)
        .map {
            ExportedItem(
                name = it[0],
                value = it[1].toInt()
            )
        }
