package com.example.gazege.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.gazege.R
import com.example.gazege.core.firstDayOfMonth
import com.example.gazege.core.lastDayOfMonth
import com.example.gazege.core.stableMinusMonths
import com.example.gazege.core.stablePlusMonths
import com.example.gazege.ui.DateFormat
import com.example.gazege.ui.localDateToString
import java.time.LocalDate

@Composable
fun Filter(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onRangeChanged: (LocalDate?, LocalDate?) -> Unit
) {
    val isFiltered = startDate != null || endDate != null
    val dateString = rangeToString(startDate, endDate)
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (startDate != null && endDate != null) {
                    val newRange = Pair(
                        stableMinusMonths(startDate, 1L),
                        stableMinusMonths(endDate, 1L)
                    )
                    onRangeChanged(newRange.first, newRange.second)
                }
            },
            enabled = startDate != null && endDate != null
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.round_arrow_left_24
                ), contentDescription = "Left"
            )
        }
        Text(
            dateString,
            modifier = Modifier.clickable {
                val newRange = LocalDate.now().let {
                    Pair(
                        firstDayOfMonth(it),
                        lastDayOfMonth(it)
                    )
                }
                onRangeChanged(newRange.first, newRange.second)
            })
        IconButton(
            onClick = {
                if (startDate != null && endDate != null) {
                    val newRange = Pair(
                        stablePlusMonths(startDate, 1L),
                        stablePlusMonths(endDate, 1L)
                    )
                    onRangeChanged(newRange.first, newRange.second)
                }
            },
            enabled = startDate != null && endDate != null
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.round_arrow_right_24
                ), contentDescription = "Right"
            )
        }
        IconButton(
            onClick = { onRangeChanged(null, null) },
            enabled = isFiltered
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.round_filter_list_off_24
                ), contentDescription = "Clear filters"
            )
        }
    }
}

fun rangeToString(startDate: LocalDate?, endDate: LocalDate?) =
    if (startDate == null && endDate == null) "Todo"
    else if (startDate != null && endDate != null)
        localDateToString(startDate, DateFormat.YEARMONTHNAME)
    else "?"