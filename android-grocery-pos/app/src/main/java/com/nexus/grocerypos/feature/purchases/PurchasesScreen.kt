package com.nexus.grocerypos.feature.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.EmptyState
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassButtonStyle
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassChip
import com.nexus.grocerypos.core.designsystem.components.GlassIconButton
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.components.StatusChip
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.GlassSpacing
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.PurchaseOrder
import com.nexus.grocerypos.domain.model.PurchaseOrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PurchasesScreen(
    onAddOrder: () -> Unit,
    onOpenOrder: (Long) -> Unit,
    onReceiveOrder: (Long) -> Unit,
    viewModel: PurchasesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(
                title = "Purchases",
                subtitle = "${uiState.orders.size} purchase orders",
                actions = {
                    GlassIconButton(onClick = onAddOrder) {
                        Icon(Icons.Filled.Add, contentDescription = "New purchase order", tint = GlassPalette.AccentPrimary)
                    }
                }
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(GlassSpacing.gap8)) {
                items(statusFilters, key = { it.label }) { filter ->
                    GlassChip(
                        label = filter.label,
                        selected = uiState.statusFilter == filter.status,
                        onClick = { viewModel.onStatusFilterChange(filter.status) }
                    )
                }
            }
        }

        if (uiState.orders.isEmpty() && !uiState.isLoading) {
            item {
                val hasFilter = uiState.statusFilter != null
                EmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    title = if (hasFilter) "No matching orders" else "No purchase orders yet",
                    message = if (hasFilter) {
                        "No purchase orders have this status yet."
                    } else {
                        "Create a purchase order to start restocking from a supplier."
                    },
                    actionLabel = if (hasFilter) null else "New purchase order",
                    onAction = if (hasFilter) null else onAddOrder
                )
            }
        } else {
            items(uiState.orders, key = { it.id }) { order ->
                PurchaseOrderRow(
                    order = order,
                    onClick = { onOpenOrder(order.id) },
                    onReceive = { onReceiveOrder(order.id) }
                )
            }
        }
    }
}

private data class StatusFilter(val label: String, val status: PurchaseOrderStatus?)

private val statusFilters = listOf(
    StatusFilter("All", null),
    StatusFilter("Draft", PurchaseOrderStatus.DRAFT),
    StatusFilter("Ordered", PurchaseOrderStatus.ORDERED),
    StatusFilter("Partially received", PurchaseOrderStatus.PARTIALLY_RECEIVED),
    StatusFilter("Received", PurchaseOrderStatus.RECEIVED),
    StatusFilter("Cancelled", PurchaseOrderStatus.CANCELLED)
)

private fun statusColor(status: PurchaseOrderStatus): androidx.compose.ui.graphics.Color = when (status) {
    PurchaseOrderStatus.DRAFT -> GlassPalette.AccentInfo
    PurchaseOrderStatus.ORDERED -> GlassPalette.AccentInfo
    PurchaseOrderStatus.PARTIALLY_RECEIVED -> GlassPalette.AccentWarning
    PurchaseOrderStatus.RECEIVED -> GlassPalette.AccentSuccess
    PurchaseOrderStatus.CANCELLED -> GlassPalette.AccentDanger
}

private fun statusLabel(status: PurchaseOrderStatus): String = when (status) {
    PurchaseOrderStatus.DRAFT -> "Draft"
    PurchaseOrderStatus.ORDERED -> "Ordered"
    PurchaseOrderStatus.PARTIALLY_RECEIVED -> "Partially received"
    PurchaseOrderStatus.RECEIVED -> "Received"
    PurchaseOrderStatus.CANCELLED -> "Cancelled"
}

private val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

@Composable
private fun PurchaseOrderRow(order: PurchaseOrder, onClick: () -> Unit, onReceive: () -> Unit) {
    val colors = LocalGlassColors.current
    val canReceive = order.status == PurchaseOrderStatus.ORDERED || order.status == PurchaseOrderStatus.PARTIALLY_RECEIVED

    GlassCard(onClick = onClick) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = order.orderNumber, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    Text(text = order.supplierName, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                StatusChip(label = statusLabel(order.status), color = statusColor(order.status))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatter.format(Date(order.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Text(
                    text = "$%.2f".format(order.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
            }
            if (canReceive) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    GlassButton(
                        text = "Receive",
                        onClick = onReceive,
                        style = GlassButtonStyle.SECONDARY,
                        leadingIcon = { Icon(Icons.Filled.Inventory, contentDescription = null, tint = colors.textPrimary) }
                    )
                }
            }
        }
    }
}
