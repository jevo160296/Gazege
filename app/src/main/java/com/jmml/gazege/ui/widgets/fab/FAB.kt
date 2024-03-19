package com.jmml.gazege.ui.widgets.fab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.jmml.gazege.R
import com.jmml.gazege.ui.theme.Shapes

@Composable
fun FAB(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        shape = Shapes.small,
        content = icon
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExtendedFAB(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    icon: @Composable () -> Unit
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        shape = Shapes.small
    ) {
        icon()
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.DefaultPadding)))
        AnimatedContent(
            targetState = text,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) {
            Text(text = it)
        }
    }
}