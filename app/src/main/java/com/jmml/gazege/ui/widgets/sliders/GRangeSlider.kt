package com.jmml.gazege.ui.widgets.sliders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.ui.floatToShortText
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.SmallBody

@Composable
fun GRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 10,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.tertiary,
        activeTrackColor = MaterialTheme.colorScheme.tertiary,
        inactiveTrackColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
    )
) {
    val coercedStart = value.start.coerceIn(valueRange.start, valueRange.endInclusive)
    val coercedEnd = value.endInclusive.coerceIn(valueRange.start, valueRange.endInclusive)

    val startValueText = floatToShortText(value.start, 2)
    val endValueText = floatToShortText(value.endInclusive, 2)

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SmallBody(
                text = startValueText,
                modifier = Modifier.width(64.dp),
                maxLines = 1
            )
            SmallBody(
                text = endValueText,
                modifier = Modifier.width(64.dp),
                maxLines = 1,
                textAlign = TextAlign.End
            )
        }
        RangeSlider(
            value = coercedStart..coercedEnd,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            colors = colors
        )
    }
}

@Preview
@Composable
private fun GRangeSliderPreview() {
    var start: Float by remember { mutableFloatStateOf(10.0f) }
    var end: Float by remember { mutableFloatStateOf(15.0f) }
    GazegeTheme {
        Box(
            Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            GRangeSlider(
                value = start..end,
                onValueChange = {
                    if (it.start != start) {
                        start = it.start
                    }
                    if (it.endInclusive != end) {
                        end = it.endInclusive
                    }
                },
                valueRange = 0.0f..100.0f
            )
        }
    }
}
