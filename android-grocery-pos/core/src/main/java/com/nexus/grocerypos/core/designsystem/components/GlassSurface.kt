package com.nexus.grocerypos.core.designsystem.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexus.grocerypos.core.designsystem.theme.GlassShapes
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

/**
 * The base frosted-glass surface used by every elevated container in the app:
 * translucent gradient fill, hairline border, soft shadow, and (on API 31+) a
 * subtle self-blur so edges read as diffused glass rather than a flat tint.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = GlassShapes.card,
    strong: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colors = LocalGlassColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val fill = if (strong) colors.surfaceFillStrong else colors.surfaceFill

    val pressableModifier = if (onClick != null) {
        Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = if (pressed) 4.dp else 16.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(shape)
            .then(if (Build.VERSION.SDK_INT >= 31) Modifier.blur(0.6.dp) else Modifier)
            .background(
                Brush.linearGradient(
                    colors = listOf(fill, fill.copy(alpha = fill.alpha * 0.6f))
                )
            )
            .border(width = 1.dp, color = colors.border, shape = shape)
            .then(pressableModifier)
    ) {
        content()
    }
}
