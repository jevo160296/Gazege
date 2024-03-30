package com.jmml.gazege.ui.theme

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

enum class AppMode {
    DEBUG,
    RELEASE
}

internal val LocalAppMode = staticCompositionLocalOf { AppMode.DEBUG }
internal val LocalGazegeColorScheme = staticCompositionLocalOf { lightColorScheme().second }

@Composable
fun GazegeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appMode: String = "DEBUG",
    isDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val appModeParsed = when (appMode) {
        "DEBUG" -> AppMode.DEBUG
        "RELEASE" -> AppMode.RELEASE
        else -> error("AppMode debe ser 'DEBUG' o 'RELEASE'. AppMode actual es: $appMode")
    }

    val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colors = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current) to DarkColors.second
        dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current) to LightColors.second
        darkTheme -> DarkColors
        else -> LightColors
    }

    val rememberedColorScheme = remember {
        colors.second.copy()
    }.apply {
        updateColorSchemeFrom(colors.second)
    }

    MaterialTheme(
        colorScheme = colors.first,
        typography = Typography,
        shapes = Shapes,
        content = {
            CompositionLocalProvider(
                LocalAppMode provides appModeParsed,
                LocalContentColor provides colors.first.onBackground,
                LocalGazegeColorScheme provides rememberedColorScheme
            ) {
                content()
            }
        }
    )
}

object GazegeTheme {
    val appMode: AppMode
        @Composable
        @ReadOnlyComposable
        get() = LocalAppMode.current

    val gazegeColorScheme: GazegeColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalGazegeColorScheme.current
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ThemePreviewLight() {
    var isDynamicColor by remember { mutableStateOf(true) }
    GazegeTheme(isDynamicColor = isDynamicColor) {
        val colors = MaterialTheme.colorScheme.let {
            listOf(
                it.primary to "primary",
                it.onPrimary to "on primary",
                it.primaryContainer to "primary container",
                it.onPrimaryContainer to "on primary container",
                it.secondary to "secondary",
                it.onSecondary to "on secondary",
                it.secondaryContainer to "secondary container",
                it.onSecondaryContainer to "on secondary container",
                it.tertiary to "tertiary",
                it.onTertiary to "on tertiary",
                it.tertiaryContainer to "tertiary container",
                it.onTertiaryContainer to "on tertiary container",
                it.background to "background",
                it.onBackground to "on background",
                it.surface to "surface",
                it.onSurface to "on surface",
                it.surfaceTint to "surface tint",
                it.surfaceVariant to "surface variant",
                it.onSurfaceVariant to "on surface variant",
                it.inverseSurface to "inverse surface",
                it.inverseOnSurface to "inverse on surface",
                it.inversePrimary to "inverse primary",
                it.error to "error",
                it.onError to "on error",
                it.errorContainer to "error container",
                it.onErrorContainer to "on error container",
                it.outline to "outline",
                it.outlineVariant to "outline variant",
                it.scrim to "scrim",
            )
        }
        Scaffold(
            Modifier.padding(vertical = 50.dp, horizontal = 8.dp),
            bottomBar = {
                BottomAppBar {
                    IconButton(onClick = { isDynamicColor = !isDynamicColor }) {
                        Image(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = ""
                        )
                    }
                }
            }
        ) { padding ->
            FlowRow(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(padding),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                colors.forEach {
                    Column(
                        Modifier
                            .width(84.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            Modifier
                                .background(color = it.first)
                                .padding(42.dp)
                        ) {}
                        Text(it.second, overflow = TextOverflow.Ellipsis, softWrap = true)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ThemePreviewDark() {
    GazegeTheme(darkTheme = true) {
        val colors = MaterialTheme.colorScheme.let {
            listOf(
                it.primary to "primary",
                it.onPrimary to "on primary",
                it.primaryContainer to "primary container",
                it.onPrimaryContainer to "on primary container",
                it.secondary to "secondary",
                it.onSecondary to "on secondary",
                it.secondaryContainer to "secondary container",
                it.onSecondaryContainer to "on secondary container",
                it.tertiary to "tertiary",
                it.onTertiary to "on tertiary",
                it.tertiaryContainer to "tertiary container",
                it.onTertiaryContainer to "on tertiary container",
                it.background to "background",
                it.onBackground to "on background",
                it.surface to "surface",
                it.onSurface to "on surface",
                it.surfaceTint to "surface tint",
                it.surfaceVariant to "surface variant",
                it.onSurfaceVariant to "on surface variant",
                it.inverseSurface to "inverse surface",
                it.inverseOnSurface to "inverse on surface",
                it.inversePrimary to "inverse primary",
                it.error to "error",
                it.onError to "on error",
                it.errorContainer to "error container",
                it.onErrorContainer to "on error container",
                it.outline to "outline",
                it.outlineVariant to "outline variant",
                it.scrim to "scrim",
            )
        }
        Scaffold(
            Modifier.padding(vertical = 50.dp, horizontal = 8.dp),
            bottomBar = {
                BottomAppBar {
                    Image(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = ""
                    )
                }
            }
        ) { padding ->
            FlowRow(
                Modifier
                    .padding(padding)
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                colors.forEach {
                    Column(
                        Modifier
                            .width(84.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            Modifier
                                .background(color = it.first)
                                .padding(42.dp)
                        ) {}
                        Text(it.second, overflow = TextOverflow.Ellipsis, softWrap = true)
                    }
                }
            }
        }
    }
}