package com.jmml.gazege.extensions.density

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Density.toDp(valuePx: Float): Dp = valuePx.div(this.density).dp

fun Density.toPx(valueDp: Dp): Float = valueDp.times(this.density).value

fun Density.toPx(valueDp: Float): Float = valueDp.times(this.density)