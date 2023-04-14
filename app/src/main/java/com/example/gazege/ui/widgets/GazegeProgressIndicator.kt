package com.example.gazege.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.doubleToPercentageString

@Composable
fun GazegeProgressIndicator(
    compleition: Double,
    labelString: String = "Progress",
    color: Color = ProgressIndicatorDefaults.linearColor
) {
    Text(text = "$labelString: ${doubleToPercentageString(compleition)}")
    LinearProgressIndicator(
        progress = compleition.toFloat(),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        color = color,
        trackColor = color.copy(alpha = 0.2f)
    )
}