package com.example.gazege.plot

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionListItemDetails
import com.example.gazege.core.entities.toSequence
import com.example.gazege.core.firstDayOfMonth
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.doubleToShortMoneyText
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.MediumHeadline
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.extension.round
import java.time.LocalDate
import java.time.Period

data class PlotDataFromTransactions(
    val transactionsListItemDetails: List<TransactionListItemDetails>
) : PlotData<List<TransactionListItemDetails>> {
    override val inputData: Array<List<TransactionListItemDetails>>
        get() = arrayOf(
            transactionsListItemDetails
        )
    override val dateRange =
        transactionsListItemDetails.minOfOrNull { it.transaction.date }.let { minDate ->
            transactionsListItemDetails.maxOfOrNull { it.transaction.date }.let { maxDate ->
                if (minDate != null && maxDate != null) {
                    minDate..maxDate
                } else {
                    null
                }
            }
        }
    private val mapper = dateRange
        ?.toSequence { it.plusDays(1) }
        ?.mapIndexed { index, localDate -> Pair(localDate, index.toFloat()) }
        ?.toMap()

    override fun dateMapper(date: LocalDate): Float = mapper?.get(date) ?: 0f

    override fun generateList(inputData: List<TransactionListItemDetails>): List<Pair<LocalDate, Double>> {
        val transactions = inputData.map { it.transaction }
        val minDate = dateRange?.start
        val maxDate = dateRange?.endInclusive
        val monthSpan = if (maxDate != null && minDate != null) {
            Period.between(minDate, maxDate).toTotalMonths()
        } else {
            null
        }
        return listOf(
            *transactions.toTypedArray(),
            *if (minDate != null && maxDate != null) {
                generateSequence(
                    seedFunction = {
                        Transaction(
                            null,
                            0.0,
                            "",
                            -1,
                            -1,
                            null,
                            minDate,
                            null
                        )
                    },
                    nextFunction = { trx ->
                        val newDate = trx.date.plusDays(1)
                        if (newDate <= maxDate) {
                            Transaction(
                                null,
                                0.0,
                                "",
                                -1,
                                -1,
                                null,
                                newDate,
                                null
                            )
                        } else {
                            null
                        }
                    }
                ).toList().toTypedArray()
            } else {
                arrayOf()
            })
            .groupBy {
                if (monthSpan != null && monthSpan > 1) {
                    firstDayOfMonth(it.date)
                } else {
                    it.date
                }
            }
            .map { it.key to it.value.sumOf { trx -> trx.amount } }
            .let { listOf(*it.toTypedArray()) }
            .sortedBy { it.first }
    }
}

data class PlotDataFromTimeSeries(
    val series: List<Map<LocalDate, Double>>,
    override val dateRange: ClosedRange<LocalDate>
) : PlotData<Map<LocalDate, Double>> {
    override fun generateList(inputData: Map<LocalDate, Double>): List<Pair<LocalDate, Double>> {
        return inputData.toList().sortedBy { it.first }
    }

    private val mapper = dateRange
        .toSequence { it.plusDays(1) }
        .mapIndexed { index, localDate -> Pair(localDate, index.toFloat()) }
        .toMap()

    override fun dateMapper(date: LocalDate): Float {
        return mapper[date] ?: 0f
    }

    override val inputData: Array<Map<LocalDate, Double>> get() = series.toTypedArray()

}

class Entry(
    val date: LocalDate,
    override val x: Float,
    override val y: Float
) : ChartEntry {
    override fun withY(y: Float): ChartEntry = Entry(date, x, y)
}

@Composable
fun Plot(plotData: PlotDataFromTransactions?) {
    if (plotData == null) {
        MediumHeadline("Null")
    } else {
        AccountNotNullPlot(plotData)
    }
}

@Composable
private fun AccountNotNullPlot(
    data: PlotDataFromTransactions
) {
    val chartEntryModel = data.chartEntryModel
    val horizontalAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, chartValues ->
            (chartValues.chartEntryModel.entries.first().getOrNull(value.toInt()) as? Entry)
                ?.date
                ?.run { "$dayOfMonth/$monthValue" }
                .orEmpty()
        }
    val verticalAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        doubleToMoneyString(value.toDouble())
    }
    Box(
        Modifier
            .height(130.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Chart(
            chart = lineChart(),
            model = chartEntryModel,
            bottomAxis = bottomAxis(valueFormatter = horizontalAxisValueFormatter),
            startAxis = startAxis(valueFormatter = verticalAxisValueFormatter)
        )
    }
}

@Composable
private fun CategoryNullPlot() {
    Text("Null plot")
}

@Composable
fun CategoryNotNullPlot(
    data: PlotDataFromTimeSeries,
    lines: List<LineChart.LineSpec>
) {
    val chartEntryModel = data.chartEntryModel
    val verticalAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        doubleToShortMoneyText(value.round.toDouble(), 1)
    }
    Box(
        Modifier
            .height(130.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Chart(
            chart = lineChart(
                lines = lines,
                spacing = 1.dp
            ),
            model = chartEntryModel,
            startAxis = startAxis(
                valueFormatter = verticalAxisValueFormatter,
                guideline = null
            ),
            autoScaleUp = AutoScaleUp.Full,
        )
    }
}

@Composable
fun CategoryPlot(
    pastForecast: Map<LocalDate, Double>,
    futureForecast: Map<LocalDate, Double>,
    dateRange: ClosedRange<LocalDate>
) = if (pastForecast.isEmpty() && futureForecast.isEmpty()) {
    CategoryNullPlot()
} else {
    CategoryNotNullPlot(
        data = PlotDataFromTimeSeries(listOf(pastForecast, futureForecast), dateRange),
        lines = listOf(
            LineChart.LineSpec(lineColor = MaterialTheme.colorScheme.tertiary.toArgb()),
            LineChart.LineSpec(
                lineColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f).toArgb()
            )
        )
    )
}

interface PlotData<T> {
    fun generateList(inputData: T): List<Pair<LocalDate, Double>>
    fun generateChartData(inputData: List<Pair<LocalDate, Double>>): List<Entry> =
        inputData
            .map { (date, y) -> Entry(date, dateMapper(date), y.toFloat()) }

    fun dateMapper(date: LocalDate): Float

    val inputData: Array<T>

    val dateRange: ClosedRange<LocalDate>?
    val chartData: Array<List<Entry>>
        get() = inputData.map {
            generateChartData(generateList(it))
        }.toTypedArray()

    val chartEntryModel: ChartEntryModel get() = ChartEntryModelProducer(*chartData).getModel()
}

@Preview
@Composable
fun ChartPreview() {
    val data = PlotDataFromTimeSeries(
        listOf(
            mapOf(
                LocalDate.of(2023, 1, 1) to 1.0,
                LocalDate.of(2023, 1, 2) to 2.0,
                LocalDate.of(2023, 1, 5) to 3.0,
            ),
            mapOf(
                LocalDate.of(2023, 1, 21) to 19.0,
                LocalDate.of(2023, 1, 22) to 20.0,
            )
        ), LocalDate.of(2023, 1, 1)..LocalDate.of(2023, 1, 31)
    )
    GazegeTheme {
        Box(Modifier.fillMaxSize()) {
            CategoryNotNullPlot(
                data = data,
                listOf(
                    LineChart.LineSpec(lineColor = Color.GREEN),
                    LineChart.LineSpec(lineColor = Color.RED)
                )
            )
        }
    }
}