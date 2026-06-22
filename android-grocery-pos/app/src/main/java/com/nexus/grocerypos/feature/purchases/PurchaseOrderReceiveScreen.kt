package com.nexus.grocerypos.feature.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.GlassSpacing
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

@Composable
fun PurchaseOrderReceiveScreen(
    orderId: Long,
    actorUserId: Long,
    onDone: () -> Unit,
    viewModel: PurchaseOrderReceiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGlassColors.current

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onDone()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(
                title = "Receive order",
                subtitle = uiState.order?.orderNumber.orEmpty()
            )
        }

        if (uiState.order != null) {
            item {
                GlassCard {
                    Column {
                        Text(text = uiState.order!!.supplierName, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Text(
                            text = "Order ${uiState.order!!.orderNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        items(uiState.lineItems, key = { it.productId }) { line ->
            ReceiveLineRow(line = line, onQuantityChange = { viewModel.onReceiveQuantityChange(line.productId, it) })
        }

        if (uiState.errorMessage != null) {
            item {
                Text(text = uiState.errorMessage!!, style = MaterialTheme.typography.bodySmall, color = GlassPalette.AccentDanger)
            }
        }

        item {
            GlassButton(
                text = "Confirm receipt",
                onClick = { viewModel.confirmReceive(actorUserId) },
                loading = uiState.isSubmitting,
                enabled = !uiState.isSubmitting && uiState.lineItems.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReceiveLineRow(line: ReceiveLineItem, onQuantityChange: (String) -> Unit) {
    val colors = LocalGlassColors.current
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap8)) {
            Text(text = line.productName, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ordered ${formatQty(line.quantityOrdered)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                if (line.quantityReceived > 0) {
                    Text(
                        text = "Already received ${formatQty(line.quantityReceived)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
            GlassTextField(
                value = line.receiveNowInput,
                onValueChange = onQuantityChange,
                label = "Receive now",
                keyboardType = KeyboardType.Decimal
            )
        }
    }
}

private fun formatQty(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}
