package com.jmml.gazege.ui.widgets

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.ui.doubleToPercentageString
import com.jmml.gazege.ui.theme.GazegeTheme

@Composable
fun GProgressIndicator(
    compleition: Double,
    labelString: String = "Progress",
    compact: Boolean = false,
    excessColor: Color = MaterialTheme.colorScheme.error,
    color: Color = ProgressIndicatorDefaults.linearColor
) {
    if (!compact) {
        Text(text = "$labelString: ${doubleToPercentageString(compleition)}")
    }
    val calculatedCompletion =
        if (compleition <= 0.0) 0.0
        else if (compleition <= 1.0) compleition
        else if (compleition < Double.POSITIVE_INFINITY) 1.0 / compleition
        else 0.0
    LinearProgressIndicator(
        progress = { calculatedCompletion.toFloat() },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        color = color,
        trackColor = if (compleition <= 1.0) color.copy(alpha = 0.2f) else excessColor,
    )
}

@Composable
fun GDefiniteCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    animated: Boolean = true,
    easing: Easing = EaseInOut
) {
    if (animated) {
        val infiniteTransition = rememberInfiniteTransition("Infinite transition")
        val animatedProgress by infiniteTransition.animateFloat(
            initialValue = progress,
            targetValue = 1.0F,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    1000,
                    easing = easing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Progress"
        )
        val animatedAngularPosition by infiniteTransition.animateFloat(
            initialValue = 0.0F,
            targetValue = 360.0F,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    1000,
                    easing = easing
                ),
                repeatMode = RepeatMode.Restart
            ), label = "Rotation"
        )
        GDefiniteCircularProgressIndicator(
            progress = animatedProgress,
            angularPosition = animatedAngularPosition + 360.0F * progress / 2
        )
    } else {
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
fun GDefiniteCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    angularPosition: Float
) {
    val currentArc = 360.0F * progress
    val rotation = -(currentArc / 2) + angularPosition
    CircularProgressIndicator(
        progress = { progress },
        modifier = modifier.rotate(rotation),
        strokeCap = StrokeCap.Round,
    )
}

@Composable
fun GIndefiniteCircularProgressIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier,
        strokeCap = StrokeCap.Round
    )
}

@Preview
@Composable
fun IndefiniteCircularProgress() {
    GazegeTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            GIndefiniteCircularProgressIndicator()
        }
    }
}

@Preview
@Composable
fun TurningCircularProgress() {
    val currentProgress = 0.2F
    GazegeTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            GDefiniteCircularProgressIndicator(
                progress = currentProgress,
                animated = true
            )
        }
    }
}

@Preview
@Composable
fun LinearProgress() {
    val (numerator, onNumeratorChange) = remember { mutableDoubleStateOf(1.0) }
    val (denominator, onDenominatorChange) = remember { mutableDoubleStateOf(2.0) }
    GazegeTheme {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .statusBarsPadding()
        ) {
            GProgressIndicator(compleition = numerator / denominator)
            Slider(
                value = numerator.toFloat(),
                onValueChange = { onNumeratorChange(it.toDouble()) },
                valueRange = -2.0f..2.0f
            )
            Slider(
                value = denominator.toFloat(),
                onValueChange = { onDenominatorChange(it.toDouble()) },
                valueRange = 0.0f..2.0f
            )
        }
    }
}