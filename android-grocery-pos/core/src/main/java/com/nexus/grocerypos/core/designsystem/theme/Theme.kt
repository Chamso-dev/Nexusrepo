package com.nexus.grocerypos.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkScheme = darkColorScheme(
    primary = GlassPalette.AccentPrimary,
    secondary = GlassPalette.AccentSecondary,
    background = GlassPalette.BackdropDarkTop,
    surface = GlassPalette.BackdropDarkMid,
    error = GlassPalette.AccentDanger,
    onPrimary = GlassPalette.TextPrimaryDark,
    onBackground = GlassPalette.TextPrimaryDark,
    onSurface = GlassPalette.TextPrimaryDark
)

private val LightScheme = lightColorScheme(
    primary = GlassPalette.AccentPrimary,
    secondary = GlassPalette.AccentSecondary,
    background = GlassPalette.BackdropLightTop,
    surface = GlassPalette.BackdropLightMid,
    error = GlassPalette.AccentDanger,
    onPrimary = GlassPalette.TextPrimaryLight,
    onBackground = GlassPalette.TextPrimaryLight,
    onSurface = GlassPalette.TextPrimaryLight
)

@Composable
fun GroceryPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val glassColors = if (darkTheme) {
        GlassColors(
            backdropTop = GlassPalette.BackdropDarkTop,
            backdropMid = GlassPalette.BackdropDarkMid,
            backdropBottom = GlassPalette.BackdropDarkBottom,
            surfaceFill = GlassPalette.GlassFillDark,
            surfaceFillStrong = GlassPalette.GlassFillDarkStrong,
            border = GlassPalette.GlassBorderDark,
            textPrimary = GlassPalette.TextPrimaryDark,
            textSecondary = GlassPalette.TextSecondaryDark,
            isDark = true
        )
    } else {
        GlassColors(
            backdropTop = GlassPalette.BackdropLightTop,
            backdropMid = GlassPalette.BackdropLightMid,
            backdropBottom = GlassPalette.BackdropLightBottom,
            surfaceFill = GlassPalette.GlassFillLight,
            surfaceFillStrong = GlassPalette.GlassFillLightStrong,
            border = GlassPalette.GlassBorderLight,
            textPrimary = GlassPalette.TextPrimaryLight,
            textSecondary = GlassPalette.TextSecondaryLight,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = GroceryPosTypography,
            content = content
        )
    }
}
