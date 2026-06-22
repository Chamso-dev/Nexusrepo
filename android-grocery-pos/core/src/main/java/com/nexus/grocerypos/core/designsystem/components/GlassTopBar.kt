package com.nexus.grocerypos.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexus.grocerypos.core.designsystem.theme.GlassShapes
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    val colors = LocalGlassColors.current
    GlassSurface(modifier = modifier.fillMaxWidth(), shape = GlassShapes.cardLarge) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (navigationIcon != null) {
                    Box(modifier = Modifier.padding(end = 12.dp)) { navigationIcon() }
                }
                androidx.compose.foundation.layout.Column {
                    Text(text = title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    if (subtitle != null) {
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) { actions() }
        }
    }
}
