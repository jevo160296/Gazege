package com.jmml.gazege.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import com.jmml.gazege.ui.theme.tokens.ColorDarkTokens
import com.jmml.gazege.ui.theme.tokens.ColorLightTokens


@Stable
class GazegeColorScheme(
    transfer: Color,
    income: Color,
    expense: Color,
    neutral: Color,
    onNeutral: Color
) {
    var transfer by mutableStateOf(transfer, structuralEqualityPolicy())
        internal set
    var income by mutableStateOf(income, structuralEqualityPolicy())
        internal set
    var outcome by mutableStateOf(expense, structuralEqualityPolicy())
    var neutral by mutableStateOf(neutral, structuralEqualityPolicy())
        internal set
    var onNeutral by mutableStateOf(onNeutral, structuralEqualityPolicy())
        internal set

    /** Returns a copy of this ColorScheme, optionally overriding some of the values. */
    fun copy(
        transfer: Color = this.transfer,
        income: Color = this.income,
        outcome: Color = this.outcome,
        neutral: Color = this.neutral,
        onNeutral: Color = this.onNeutral
    ): GazegeColorScheme =
        GazegeColorScheme(
            transfer = transfer,
            income = income,
            expense = outcome,
            neutral = neutral,
            onNeutral = onNeutral
        )

    override fun toString(): String {
        return "ColorScheme(" +
                "transfer=$transfer" +
                "income=$income" +
                "outcome=$outcome" +
                "neutral=$neutral" +
                "onNeutral=$onNeutral" +
                ")"
    }
}

fun GazegeColorScheme.updateColorSchemeFrom(colorScheme: GazegeColorScheme) {
    this.income = colorScheme.income
    this.outcome = colorScheme.outcome
    this.transfer = colorScheme.transfer
    this.neutral = colorScheme.neutral
    this.onNeutral = colorScheme.onNeutral
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
    neutral: Color = md_theme_light_neutral_chart,
    onNeutral: Color = md_theme_light_on_neutral_chart
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
            neutral = neutral,
            onNeutral = onNeutral
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
    neutral: Color = md_theme_dark_neutral_chart,
    onNeutral: Color = md_theme_dark_on_neutral_chart
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
            neutral = neutral,
            onNeutral = onNeutral
        )
    )