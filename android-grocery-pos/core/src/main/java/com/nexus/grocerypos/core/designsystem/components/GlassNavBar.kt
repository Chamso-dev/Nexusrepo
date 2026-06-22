package com.nexus.grocerypos.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.GlassShapes
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

data class GlassNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun GlassBottomNavBar(
    items: List<GlassNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(modifier = modifier, shape = GlassShapes.pill, strong = true) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                GlassNavBarEntry(
                    item = item,
                    selected = item.route == selectedRoute,
                    onClick = { onItemSelected(item.route) }
                )
            }
        }
    }
}

@Composable
fun GlassNavRail(
    items: List<GlassNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(modifier = modifier, shape = GlassShapes.cardLarge, strong = true) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items.forEach { item ->
                GlassNavBarEntry(
                    item = item,
                    selected = item.route == selectedRoute,
                    onClick = { onItemSelected(item.route) }
                )
            }
        }
    }
}

@Composable
private fun GlassNavBarEntry(
    item: GlassNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalGlassColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val tint = if (selected) GlassPalette.AccentPrimary else colors.textSecondary

    Column(
        modifier = Modifier
            .clip(GlassShapes.chip)
            .then(
                if (selected) Modifier.background(GlassPalette.AccentPrimary.copy(alpha = 0.16f)) else Modifier
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.icon,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier.size(24.dp).clip(CircleShape)
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            textAlign = TextAlign.Center
        )
    }
}
