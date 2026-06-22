package com.nexus.grocerypos.core.designsystem.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class GlassColors(
    val backdropTop: Color,
    val backdropMid: Color,
    val backdropBottom: Color,
    val surfaceFill: Color,
    val surfaceFillStrong: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val isDark: Boolean
)

object GlassShapes {
    val card = RoundedCornerShape(24.dp)
    val cardLarge = RoundedCornerShape(28.dp)
    val pill = RoundedCornerShape(50)
    val field = RoundedCornerShape(18.dp)
    val chip = RoundedCornerShape(14.dp)
}

object GlassSpacing {
    val screenPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    val cardPadding = PaddingValues(20.dp)
    val gap4 = 4.dp
    val gap8 = 8.dp
    val gap12 = 12.dp
    val gap16 = 16.dp
    val gap20 = 20.dp
    val gap24 = 24.dp
}

object GlassMotion {
    const val DURATION_FAST = 160
    const val DURATION_MEDIUM = 280
    const val DURATION_SLOW = 420
}

val LocalGlassColors = staticCompositionLocalOf {
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
}
