package com.nexus.grocerypos.core.designsystem.theme

import androidx.compose.ui.graphics.Color

object GlassPalette {
    // Backdrop gradients — Liquid Glass relies on a colorful backdrop showing through.
    val BackdropDarkTop = Color(0xFF12152B)
    val BackdropDarkMid = Color(0xFF1E2350)
    val BackdropDarkBottom = Color(0xFF2B1E4D)

    val BackdropLightTop = Color(0xFFEFF1FB)
    val BackdropLightMid = Color(0xFFE3E8FB)
    val BackdropLightBottom = Color(0xFFEDE7FA)

    val AccentPrimary = Color(0xFF6C7BFF)
    val AccentSecondary = Color(0xFF9B6CFF)
    val AccentSuccess = Color(0xFF35D29A)
    val AccentWarning = Color(0xFFFFB454)
    val AccentDanger = Color(0xFFFF6B81)
    val AccentInfo = Color(0xFF4FC3F7)

    // Glass surface tints
    val GlassFillDark = Color(0xFFFFFFFF).copy(alpha = 0.08f)
    val GlassFillDarkStrong = Color(0xFFFFFFFF).copy(alpha = 0.14f)
    val GlassBorderDark = Color(0xFFFFFFFF).copy(alpha = 0.16f)

    val GlassFillLight = Color(0xFFFFFFFF).copy(alpha = 0.55f)
    val GlassFillLightStrong = Color(0xFFFFFFFF).copy(alpha = 0.75f)
    val GlassBorderLight = Color(0xFFFFFFFF).copy(alpha = 0.9f)

    val TextPrimaryDark = Color(0xFFF5F6FF)
    val TextSecondaryDark = Color(0xFFB7BBDD)
    val TextPrimaryLight = Color(0xFF1A1B2E)
    val TextSecondaryLight = Color(0xFF5C5F7A)
}
