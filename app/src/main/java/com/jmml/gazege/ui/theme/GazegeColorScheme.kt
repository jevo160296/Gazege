package com.jmml.gazege.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.jmml.gazege.extensions.closedrange.toSequence
import com.jmml.gazege.ui.theme.GazegeColorScheme.Companion.copyM3HCT
import com.jmml.gazege.ui.theme.GazegeColorScheme.Companion.generateTonalPalette
import com.jmml.gazege.ui.theme.GazegeColorScheme.Companion.getColor
import com.jmml.gazege.ui.theme.GazegeColorScheme.Companion.harmonizeColor
import com.jmml.gazege.ui.theme.GazegeColorScheme.Companion.harmonizePalette
import com.jmml.gazege.ui.theme.tokens.ColorDarkTokens
import com.jmml.gazege.ui.theme.tokens.ColorLightTokens
import com.jmml.gazege.ui.widgets.GProgressIndicator
import com.patrykandpatrick.vico.compose.component.shape.composeShape


@Stable
class GazegeColorScheme(
    transfer: Color,
    income: Color,
    expense: Color,
    good: Color,
    bad: Color,
    neutral: Color
) {
    var transfer by mutableStateOf(transfer, structuralEqualityPolicy())
        internal set
    var income by mutableStateOf(income, structuralEqualityPolicy())
        internal set
    var outcome by mutableStateOf(expense, structuralEqualityPolicy())
    var good by mutableStateOf(good, structuralEqualityPolicy())
        internal set
    var bad by mutableStateOf(bad, structuralEqualityPolicy())
        internal set
    var neutral by mutableStateOf(neutral, structuralEqualityPolicy())
        internal set

    /** Returns a copy of this ColorScheme, optionally overriding some of the values. */
    fun copy(
        transfer: Color = this.transfer,
        income: Color = this.income,
        outcome: Color = this.outcome,
        good: Color = this.good,
        bad: Color = this.bad,
        neutral: Color = this.neutral,
    ): GazegeColorScheme =
        GazegeColorScheme(
            transfer = transfer,
            income = income,
            expense = outcome,
            good = good,
            bad = bad,
            neutral = neutral,
        )

    override fun toString(): String {
        return "ColorScheme(" +
                "transfer=$transfer" +
                "income=$income" +
                "outcome=$outcome" +
                "good=$good" +
                "bad=$bad" +
                "neutral=$neutral" +
                ")"
    }

    companion object {
        private fun Float.coerceCircular() = this.mod(360f)

        fun Color.harmonizeColor(
            baseColor: Color
        ): Color {
            val baseColorHTC = FloatArray(3)
            val colorHTC = FloatArray(3)
            ColorUtils.colorToM3HCT(baseColor.toArgb(), baseColorHTC)
            ColorUtils.colorToM3HCT(toArgb(), colorHTC)

            val baseHue = baseColorHTC[0].coerceCircular()
            val colorHue = colorHTC[0].coerceCircular()
            val maxShift = 30f

            val distance1 = baseHue - colorHue
            val distance2 = colorHue - baseHue

            val angularDistance1 = distance1.coerceCircular()
            val angularDistance2 = distance2.coerceCircular()

            val mapper = { value: Float -> maxShift * value / 180f }

            val shift = mapper(minOf(angularDistance1, angularDistance2))

            val newHue =
                if (angularDistance1 < angularDistance2) colorHue + shift
                else colorHue - shift

            return Color(ColorUtils.M3HCTToColor(newHue.coerceCircular(), colorHTC[1], colorHTC[2]))
        }

        fun Color.copyM3HCT(
            hue: Float? = null,
            chroma: Float? = null,
            tone: Float? = null
        ): Color {
            val currentM3HCT = FloatArray(3).apply { }
            ColorUtils.colorToM3HCT(this.toArgb(), currentM3HCT)
            val inputHue = hue ?: currentM3HCT[0]
            val inputChroma = chroma ?: currentM3HCT[1]
            val inputTone = tone ?: currentM3HCT[2]

            return Color(ColorUtils.M3HCTToColor(inputHue, inputChroma, inputTone))
        }

        fun List<Color>.harmonizePalette(baseColor: Color) =
            this.map { it.harmonizeColor(baseColor) }

        fun generateTonalPalette(color: Color): List<Color> {
            //              0    1   2   3   4   5   6   7   8   9   10  11  12  13  14  15  16 17
            val tones =
                arrayListOf(100, 99, 98, 95, 90, 80, 70, 60, 50, 40, 35, 30, 25, 20, 15, 10, 5, 0)
            val colorHTC = FloatArray(3)
            ColorUtils.colorToM3HCT(color.toArgb(), colorHTC)
            val hue = colorHTC[0]
            return tones.map {
                Color(ColorUtils.M3HCTToColor(hue, 100f, it.toFloat()))
            }
        }

        internal fun List<Color>.getColor(colorToken: ColorTokens) = when (colorToken) {
            ColorTokens.Background -> this[1]
            ColorTokens.OnBackground -> this[15]
            ColorTokens.Primary -> this[8]
            ColorTokens.OnPrimary -> this[5]
            ColorTokens.Surface -> this[3]
            ColorTokens.OnSurface -> this[15]
        }
    }
}

internal enum class ColorTokens {
    Background, OnBackground, Primary, OnPrimary, Surface, OnSurface
}

fun GazegeColorScheme.updateColorSchemeFrom(colorScheme: GazegeColorScheme) {
    this.income = colorScheme.income
    this.outcome = colorScheme.outcome
    this.transfer = colorScheme.transfer
    this.good = colorScheme.good
    this.bad = colorScheme.bad
    this.neutral = colorScheme.neutral
}

/**
 * Returns a light Material color scheme.
 */
fun lightColorScheme(
    primary: Color = ColorLightTokens.Primary,
    onPrimary: Color = ColorLightTokens.OnPrimary,
    primaryContainer: Color = ColorLightTokens.PrimaryContainer,
    onPrimaryContainer: Color = ColorLightTokens.OnPrimaryContainer,
    inversePrimary: Color = ColorLightTokens.InversePrimary,
    secondary: Color = ColorLightTokens.Secondary,
    onSecondary: Color = ColorLightTokens.OnSecondary,
    secondaryContainer: Color = ColorLightTokens.SecondaryContainer,
    onSecondaryContainer: Color = ColorLightTokens.OnSecondaryContainer,
    tertiary: Color = ColorLightTokens.Tertiary,
    onTertiary: Color = ColorLightTokens.OnTertiary,
    tertiaryContainer: Color = ColorLightTokens.TertiaryContainer,
    onTertiaryContainer: Color = ColorLightTokens.OnTertiaryContainer,
    background: Color = ColorLightTokens.Background,
    onBackground: Color = ColorLightTokens.OnBackground,
    surface: Color = ColorLightTokens.Surface,
    onSurface: Color = ColorLightTokens.OnSurface,
    surfaceVariant: Color = ColorLightTokens.SurfaceVariant,
    onSurfaceVariant: Color = ColorLightTokens.OnSurfaceVariant,
    surfaceTint: Color = primary,
    inverseSurface: Color = ColorLightTokens.InverseSurface,
    inverseOnSurface: Color = ColorLightTokens.InverseOnSurface,
    error: Color = ColorLightTokens.Error,
    onError: Color = ColorLightTokens.OnError,
    errorContainer: Color = ColorLightTokens.ErrorContainer,
    onErrorContainer: Color = ColorLightTokens.OnErrorContainer,
    outline: Color = ColorLightTokens.Outline,
    outlineVariant: Color = ColorLightTokens.OutlineVariant,
    scrim: Color = ColorLightTokens.Scrim,
    transfer: Color = md_theme_light_transfer,
    income: Color = md_theme_light_income,
    expense: Color = md_theme_light_expense,
    good: Color = md_theme_light_good_chart,
    bad: Color = md_theme_light_bad_chart,
    neutral: Color = md_theme_light_neutral_chart,
): Pair<ColorScheme, GazegeColorScheme> =
    Pair(
        ColorScheme(
            primary,
            onPrimary,
            primaryContainer,
            onPrimaryContainer,
            inversePrimary,
            secondary,
            onSecondary,
            secondaryContainer,
            onSecondaryContainer,
            tertiary,
            onTertiary,
            tertiaryContainer,
            onTertiaryContainer,
            background,
            onBackground,
            surface,
            onSurface,
            surfaceVariant,
            onSurfaceVariant,
            surfaceTint,
            inverseSurface,
            inverseOnSurface,
            error,
            onError,
            errorContainer,
            onErrorContainer,
            outline,
            outlineVariant,
            scrim
        ),
        GazegeColorScheme(
            transfer = transfer,
            income = income,
            expense = expense,
            good = good,
            bad = bad,
            neutral = neutral
        )
    )

/**
 * Returns a dark Material color scheme.
 */
fun darkColorScheme(
    primary: Color = ColorDarkTokens.Primary,
    onPrimary: Color = ColorDarkTokens.OnPrimary,
    primaryContainer: Color = ColorDarkTokens.PrimaryContainer,
    onPrimaryContainer: Color = ColorDarkTokens.OnPrimaryContainer,
    inversePrimary: Color = ColorDarkTokens.InversePrimary,
    secondary: Color = ColorDarkTokens.Secondary,
    onSecondary: Color = ColorDarkTokens.OnSecondary,
    secondaryContainer: Color = ColorDarkTokens.SecondaryContainer,
    onSecondaryContainer: Color = ColorDarkTokens.OnSecondaryContainer,
    tertiary: Color = ColorDarkTokens.Tertiary,
    onTertiary: Color = ColorDarkTokens.OnTertiary,
    tertiaryContainer: Color = ColorDarkTokens.TertiaryContainer,
    onTertiaryContainer: Color = ColorDarkTokens.OnTertiaryContainer,
    background: Color = ColorDarkTokens.Background,
    onBackground: Color = ColorDarkTokens.OnBackground,
    surface: Color = ColorDarkTokens.Surface,
    onSurface: Color = ColorDarkTokens.OnSurface,
    surfaceVariant: Color = ColorDarkTokens.SurfaceVariant,
    onSurfaceVariant: Color = ColorDarkTokens.OnSurfaceVariant,
    surfaceTint: Color = primary,
    inverseSurface: Color = ColorDarkTokens.InverseSurface,
    inverseOnSurface: Color = ColorDarkTokens.InverseOnSurface,
    error: Color = ColorDarkTokens.Error,
    onError: Color = ColorDarkTokens.OnError,
    errorContainer: Color = ColorDarkTokens.ErrorContainer,
    onErrorContainer: Color = ColorDarkTokens.OnErrorContainer,
    outline: Color = ColorDarkTokens.Outline,
    outlineVariant: Color = ColorDarkTokens.OutlineVariant,
    scrim: Color = ColorDarkTokens.Scrim,
    transfer: Color = md_theme_dark_transfer,
    income: Color = md_theme_dark_income,
    expense: Color = md_theme_dark_expense,
    good: Color = md_theme_dark_good_chart,
    bad: Color = md_theme_dark_bad_chart,
    neutral: Color = md_theme_dark_neutral_chart,
): Pair<ColorScheme, GazegeColorScheme> =
    Pair(
        ColorScheme(
            primary,
            onPrimary,
            primaryContainer,
            onPrimaryContainer,
            inversePrimary,
            secondary,
            onSecondary,
            secondaryContainer,
            onSecondaryContainer,
            tertiary,
            onTertiary,
            tertiaryContainer,
            onTertiaryContainer,
            background,
            onBackground,
            surface,
            onSurface,
            surfaceVariant,
            onSurfaceVariant,
            surfaceTint,
            inverseSurface,
            inverseOnSurface,
            error,
            onError,
            errorContainer,
            onErrorContainer,
            outline,
            outlineVariant,
            scrim
        ),
        GazegeColorScheme(
            transfer = transfer,
            income = income,
            expense = expense,
            good = good,
            bad = bad,
            neutral = neutral,
        )
    )

@Composable
private fun ColorPaletteGenerator(
    primaryColor: Color? = null,
    hue: Float,
    onHueChange: (Float) -> Unit,
) {
    val selectedColor = Color(ColorUtils.M3HCTToColor(hue, 70f, 50f))
    val tonalPalete = generateTonalPalette(selectedColor)
    val harmonizedTonalPalette = tonalPalete.map {
        primaryColor?.let { primaryColor -> it.harmonizeColor(primaryColor) } ?: it
    }
    val singleColorShow = @Composable { modifier: Modifier,
                                        color: Color ->
        Box(
            modifier = modifier
                .size(90.dp)
                .background(
                    color = color,
                    shape = com.patrykandpatrick.vico.core.component.shape.Shapes
                        .roundedCornerShape(50)
                        .composeShape()
                )
        )
    }
    val multipleColorShow = @Composable { modifier: Modifier,
                                          colors: List<Color> ->
        Row(modifier) {
            colors.forEach {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 10.dp)
                        .weight(1f)
                        .background(it)
                )
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        singleColorShow(Modifier.align(Alignment.CenterHorizontally), selectedColor)
        TextField(value = "Hue: $hue", onValueChange = {
            onHueChange(it.substring(4).toFloatOrNull() ?: hue)
        })
        Slider(value = hue / 360f, onValueChange = { onHueChange(it * 360f) })
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = (0f..360f)
                        .toSequence { it + 1f }
                        .map { Color(ColorUtils.M3HCTToColor(it, 70f, 50f)) }
                        .toList()
                )
            ))
        multipleColorShow(Modifier.fillMaxWidth(), tonalPalete)
        multipleColorShow(Modifier.fillMaxWidth(), harmonizedTonalPalette)
    }
}

@Composable
private fun CheckTonalPalette(
    goodColor: Color,
    badColor: Color,
    neutralColor: Color
) {
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .padding(4.dp)
    ) {
        Text(text = "On surface", color = MaterialTheme.colorScheme.onSurface)
        GProgressIndicator(
            compleition = 0.5,
            color = neutralColor,
            excessColor = goodColor
        )
        GProgressIndicator(
            compleition = 1.5,
            color = neutralColor,
            excessColor = goodColor
        )
        GProgressIndicator(
            compleition = 1.5,
            color = neutralColor,
            excessColor = badColor
        )
    }
}

@Preview
@Composable
fun TonalPalettes() {
    GazegeTheme {
        val (primaryHue, onPrimaryHueChange) = rememberSaveable { mutableFloatStateOf(0f) }
        val (goodHue, onGoodHueChange) = rememberSaveable { mutableFloatStateOf(150f) }
        val (badHue, onBadHueChange) = rememberSaveable { mutableFloatStateOf(12f) }
        val (neutralHue, onNeutralHueChange) = rememberSaveable { mutableFloatStateOf(270f) }

        val primaryColor = md_theme_light_primary.copyM3HCT(hue = primaryHue)
        val goodColor = md_theme_light_good_chart.copyM3HCT(hue = goodHue)
        val badColor = md_theme_light_bad_chart.copyM3HCT(hue = badHue)
        val neutralColor = md_theme_light_neutral_chart.copyM3HCT(hue = neutralHue)

        val goodTonalPalette: List<Color> =
            generateTonalPalette(goodColor).harmonizePalette(primaryColor)
        val badTonalPalette: List<Color> =
            generateTonalPalette(badColor).harmonizePalette(primaryColor)
        val neutralTonalPalette: List<Color> =
            generateTonalPalette(neutralColor).harmonizePalette(primaryColor)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ColorPaletteGenerator(
                    hue = goodHue,
                    onHueChange = onGoodHueChange,
                    primaryColor = primaryColor
                )
                ColorPaletteGenerator(
                    hue = badHue,
                    onHueChange = onBadHueChange,
                    primaryColor = primaryColor
                )
                ColorPaletteGenerator(
                    hue = neutralHue,
                    onHueChange = onNeutralHueChange,
                    primaryColor = primaryColor
                )
                ColorPaletteGenerator(
                    hue = primaryHue,
                    onHueChange = onPrimaryHueChange
                )
                CheckTonalPalette(
                    goodColor = GazegeTheme.gazegeColorScheme.good,
                    badColor = GazegeTheme.gazegeColorScheme.bad,
                    neutralColor = GazegeTheme.gazegeColorScheme.neutral
                )
                CheckTonalPalette(
                    goodColor = goodTonalPalette.getColor(ColorTokens.Primary),
                    badColor = badTonalPalette.getColor(ColorTokens.Primary),
                    neutralColor = neutralTonalPalette.getColor(ColorTokens.Primary)
                )
            }
        }
    }
}