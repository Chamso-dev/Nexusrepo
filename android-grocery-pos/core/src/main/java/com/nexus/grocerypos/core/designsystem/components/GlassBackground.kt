package com.nexus.grocerypos.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

/**
 * App-wide animated mesh-gradient backdrop. Liquid Glass surfaces above it rely on this
 * colorful, slowly-drifting gradient to read as "frosted glass" rather than flat cards.
 */
@Composable
fun GlassBackground(content: @Composable () -> Unit) {
    val colors = LocalGlassColors.current
    val transition = rememberInfiniteTransition(label = "backdrop")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(18000), RepeatMode.Reverse),
        label = "backdropShift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.backdropTop, colors.backdropMid, colors.backdropBottom),
                    start = Offset(shift * 300f, 0f),
                    end = Offset(1000f - shift * 300f, 1200f)
                )
            )
    ) {
        content()
    }
}
