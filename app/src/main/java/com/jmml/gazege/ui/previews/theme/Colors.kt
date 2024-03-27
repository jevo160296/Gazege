package com.jmml.gazege.ui.previews.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.ui.theme.GazegeTheme

@Composable
private fun BackgroundForeground(
    background: Color,
    backgroundName: String,
    foreground: Color,
    foregroundName: String
) = Column(
    Modifier
        .clip(RoundedCornerShape(4.dp))
        .size(90.dp)
        .background(background)
) {
    Text(text = backgroundName, color = foreground)
    Text(foregroundName, color = foreground)
}

@Composable
private fun SimpleColor(color: Color, name: String) = Box(
    Modifier
        .clip(RoundedCornerShape(4.dp))
        .size(90.dp)
        .background(color)
) { Text(name, color = MaterialTheme.colorScheme.contentColorFor(color)) }

@Preview(widthDp = 320, heightDp = 920)
@Composable
private fun ColorsPreview() {
    val state = rememberLazyGridState()
    GazegeTheme {
        val colorPairs = MaterialTheme.colorScheme.run {
            arrayOf(
                Pair(Pair(onPrimary, primary), Pair("onPrimary", "primary")),
                Pair(Pair(onSecondary, secondary), Pair("onSecondary", "secondary")),
                Pair(Pair(onTertiary, tertiary), Pair("onTertiary", "tertiary")),

                Pair(
                    Pair(onPrimaryContainer, primaryContainer),
                    Pair("onPrimaryContainer", "primaryContainer")
                ),
                Pair(
                    Pair(onSecondaryContainer, secondaryContainer),
                    Pair("onSecondaryContainer", "secondaryContainer")
                ),
                Pair(
                    Pair(onTertiaryContainer, tertiaryContainer),
                    Pair("onTertiaryContainer", "tertiaryContainer")
                ),

                Pair(Pair(onSurface, surface), Pair("onSurface", "surface")),
                Pair(
                    Pair(onSurfaceVariant, surfaceVariant),
                    Pair("onSurfaceVariant", "surfaceVariant")
                ),
                Pair(
                    Pair(inverseSurface, inverseOnSurface),
                    Pair("inverseSurface", "inverseOnSurface")
                ),

                Pair(Pair(onBackground, background), Pair("onBackground", "background")),
                Pair(Pair(onError, error), Pair("onError", "error")),
                Pair(
                    Pair(onErrorContainer, errorContainer),
                    Pair("onErrorContainer", "errorContainer")
                ),
            )
        }
        val simpleColors = MaterialTheme.colorScheme.run {
            arrayOf(
                Pair(scrim, "scrim"),
                Pair(outline, "outline"),
                Pair(outlineVariant, "outlineVariant"),
                Pair(surfaceTint, "surfaceTint")
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(90.dp),
            state = state,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = colorPairs.toList()) {
                BackgroundForeground(
                    background = it.first.second,
                    foreground = it.first.first,
                    backgroundName = it.second.second,
                    foregroundName = it.second.first
                )
            }
            items(items = simpleColors.toList()) {
                SimpleColor(color = it.first, name = it.second)
            }
        }
    }
}