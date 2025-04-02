package com.jmml.gazege.plot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.core.entities.ITransactionListDetailGrouped
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionDetails
import com.jmml.gazege.core.entities.TransactionWithDetails
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.doubleToShortMoneyText
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.MediumHeadline
import com.jmml.zoo.extensions.closedrange.toSequence
import com.jmml.zoo.extensions.localdate.startOfMonth
import com.jmml.zoo.extensions.sequence.repeat
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shape.composeShape
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.extension.round
import java.time.LocalDate
import java.time.Period

data class PlotDataFromTransactions(
    val transactionsListItemDetails: List<ITransactionListDetailGrouped>
) : PlotData<List<ITransactionListDetailGrouped>> {
    override val inputData: Array<List<ITransactionListDetailGrouped>>
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

    override fun generateList(inputData: List<ITransactionListDetailGrouped>): List<Pair<LocalDate, Double>> {
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
                        TransactionWithDetails(
                            transaction = Transaction(
                                null,
                                -1,
                                -1,
                                minDate
                            ),
                            transactionDetails = listOf(
                                TransactionDetails(
                                    id = null,
                                    transactionId = 0,
                                    amount = 0.0,
                                    description = "",
                                    categoryId = null,
                                    aNombreDe = null,
                                    budgetDate = null
                                )
                            )
                        )
                    },
                    nextFunction = { trx ->
                        val newDate = trx.date.plusDays(1)
                        if (newDate <= maxDate) {
                            TransactionWithDetails(
                                transaction = Transaction(
                                    null,
                                    -1,
                                    -1,
                                    newDate
                                ),
                                transactionDetails = listOf(
                                    TransactionDetails(
                                        id = null,
                                        transactionId = 0,
                                        amount = 0.0,
                                        description = "",
                                        categoryId = null,
                                        aNombreDe = null,
                                        budgetDate = null
                                    )
                                )
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
                    it.date.startOfMonth()
                } else {
                    it.date
                }
            }
            .map { it.key to it.value.sumOf { trx -> trx.totalAmount } }
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

    companion object {
        private fun preFrom(rawEntries: Map<LocalDate, Float>): Pair<List<Entry>, Map<Float, String>> =
            rawEntries
                .toList()
                .sortedBy { it.first }
                .mapIndexed { x, (date, y) -> Triple(x.toFloat(), date, y) }
                .let {
                    Pair(
                        it.map { (x, date, y) -> Entry(date, x, y) },
                        it.associate { (x, date, _) -> x to date.toString() }
                    )
                }

        fun from(rawEntries: List<Map<LocalDate, Float>>): Pair<List<List<Entry>>, (Float) -> String> =
            rawEntries
                .map { preFrom(it) }
                .let { list ->
                    val values = mutableListOf<List<Entry>>()
                    val mapper = mutableMapOf<Float, String>()
                    list.forEach { (value, iMapper) ->
                        values.add(value)
                        mapper.plusAssign(iMapper)
                    }
                    values to { value: Float -> mapper[value] ?: "" }
                }

        fun <V> from(
            rawEntries: Map<LocalDate, V>,
            valueToList: (V) -> List<Float?>
        ): Pair<List<List<Entry>>, (Float) -> LocalDate?> {
            val mapperFloatToKey = mutableMapOf<Float, LocalDate>()
            val entryList: MutableList<MutableList<Entry>> = mutableListOf()
            var cantEntry: Int? = null

            rawEntries
                .toList()
                .sortedBy { it.first }
                .onEachIndexed { index, (key, value) ->
                    val indexFloat = index.toFloat()
                    val valueList = valueToList(value)
                    if (cantEntry == null) cantEntry = valueList.size
                    val castedCantEntry = cantEntry
                    if (castedCantEntry != null && entryList.size != castedCantEntry) (1..castedCantEntry).toList()
                        .onEach { entryList.add(mutableListOf()) }
                    mapperFloatToKey[indexFloat] = key
                    valueList.onEachIndexed { indexList, fl ->
                        fl?.let {
                            entryList[indexList].add(
                                Entry(key, indexFloat, fl)
                            )
                        }
                    }
                }
            val floatToKey: (Float) -> LocalDate? = { mapperFloatToKey[it] }
            return entryList to floatToKey
        }
    }
}

@Composable
fun Plot(plotData: PlotDataFromTransactions?) {
    if (plotData == null) {
        MediumHeadline("Null")
    } else {
        AccountNotNullPlot(plotData)
    }
}


data class DataModel(
    val val1: Float?,
    val val2: Float?,
    val val3: Float?
)

@Composable
fun <V> BarPlot(
    data: Map<LocalDate, V>,
    valueToList: (V) -> List<Float?>,
    dateToString: (LocalDate) -> String,
    colors: List<Color>
) {
    val (entries, mapper) = remember(data, valueToList) {
        Entry.from(
            rawEntries = data,
            valueToList = valueToList
        )
    }

    val columns = mutableListOf<LineComponent>()

    colors
        .asSequence()
        .repeat()
        .take(entries.size)
        .forEachIndexed { index, color ->
            val isLast = index + 1 == entries.size
            val shape =
                if (isLast) Shapes.roundedCornerShape(topLeftPercent = 50, topRightPercent = 50)
                    .composeShape() else Shapes.roundedCornerShape().composeShape()
            columns.add(lineComponent(color, shape = shape))
        }

    val horizontalAxisValueFormatter = remember {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            mapper(value)?.let {
                dateToString(
                    it
                )
            } ?: ""
        }
    }
    Chart(
        chart = columnChart(
            columns = columns,
            mergeMode = ColumnChart.MergeMode.Stack
        ),
        model = ChartEntryModelProducer(entries).getModel(),
        bottomAxis = bottomAxis(valueFormatter = horizontalAxisValueFormatter)
    )
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
                    LineChart.LineSpec(lineColor = Color.Green.toArgb()),
                    LineChart.LineSpec(lineColor = Color.Red.toArgb())
                )
            )
        }
    }
}

@Preview
@Composable
fun BarPlotPreview() {
    GazegeTheme {
        val data: Map<LocalDate, DataModel> = mapOf(
            LocalDate.now() to DataModel(1.0f, 4.0f, 1.0f),
            LocalDate.now().plusDays(2) to DataModel(2.0f, 5.0f, 1.0f),
            LocalDate.now().plusDays(3) to DataModel(null, 7.0f, 1.0f)
        )
        val valueToList: (DataModel) -> List<Float?> = { listOf(it.val1, it.val2, it.val3) }
        val colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column {
                Text("Chart start")
                BarPlot(
                    data = data,
                    valueToList = valueToList,
                    colors = colors,
                    dateToString = { "${it.year}/${it.monthValue}" }
                )
                Text("Chart end")
            }
        }
    }
}