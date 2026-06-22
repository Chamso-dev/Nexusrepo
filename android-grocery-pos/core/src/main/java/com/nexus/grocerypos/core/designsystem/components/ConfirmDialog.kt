package com.nexus.grocerypos.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nexus.grocerypos.core.designsystem.theme.GlassShapes
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalGlassColors.current
    Dialog(onDismissRequest = onDismiss) {
        GlassSurface(strong = true, shape = GlassShapes.cardLarge) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = message, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp, Arrangement.End)) {
                    GlassButton(text = dismissLabel, onClick = onDismiss, style = GlassButtonStyle.SECONDARY)
                    GlassButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        style = if (isDestructive) GlassButtonStyle.DANGER else GlassButtonStyle.PRIMARY
                    )
                }
            }
        }
    }
}
