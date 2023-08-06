package com.example.gazege.ui.widgets

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color

@Composable
fun PulsatingCard(
    modifier: Modifier = Modifier,
    color: Color,
    minAlpha: Float = 0.2F,
    maxAlpha: Float = 1.0F,
    animationDurationMillis: Int = 1000
) {
    val infiniteTransition = rememberInfiniteTransition("Infinite transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(
                animationDurationMillis, easing = CubicBezierEasing(
                    0.22F, 1.0F, 0.36F, 1.0F
                )
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha animation"
    )
    Box(
        modifier
            .alpha(alpha)
            .background(color))
}