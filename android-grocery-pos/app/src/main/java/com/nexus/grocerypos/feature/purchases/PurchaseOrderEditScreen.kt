package com.nexus.grocerypos.feature.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.ConfirmDialog
import com.nexus.grocerypos.core.designsystem.components.EmptyState
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassButtonStyle
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassIconButton
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.components.StatusChip
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.GlassSpacing
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.model.PurchaseOrderStatus
import com.nexus.grocerypos.domain.model.Supplier

@Composable
fun PurchaseOrderEditScreen(
    orderId: Long?,
    onDone: () -> Unit,
    viewModel: PurchaseOrderEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGlassColors.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
        if (uiState.isSaved || uiState.isDeleted) onDone()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(
                title = if (orderId == null) "New purchase order" else uiState.orderNumber,
                subtitle = if (orderId == null) "Create a draft order" else statusLabel(uiState.status)
            )
        }

        if (uiState.isFinalized) {
            item {
                FinalizedSummary(uiState = uiState)
            }
        } else {
            item {
                SupplierSection(uiState = uiState, viewModel = viewModel)
            }

            item {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap12)) {
                        GlassTextField(
                            value = uiState.invoiceNumber,
                            onValueChange = viewModel::onInvoiceNumberChange,
                            label = "Invoice number (optional)"
                        )
                        GlassTextField(
                            value = uiState.notes,
                            onValueChange = viewModel::onNotesChange,
                            label = "Notes (optional)",
                            singleLine = false
                        )
                    }
                }
            }

            item {
                LineItemsSection(uiState = uiState, viewModel = viewModel)
            }

            if (uiState.errorMessage != null) {
                item {
                    Text(text = uiState.errorMessage!!, style = MaterialTheme.typography.bodySmall, color = GlassPalette.AccentDanger)
                }
            }

            item {
                GlassButton(
                    text = "Save draft",
                    onClick = viewModel::save,
                    loading = uiState.isSaving,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.isExistingDraft) {
                item {
                    GlassButton(
                        text = "Delete",
                        onClick = { showDeleteConfirm = true },
                        style = GlassButtonStyle.DANGER,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete purchase order",
            message = "This will permanently remove ${uiState.orderNumber} and cannot be undone.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirm = false
                viewModel.delete()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun FinalizedSummary(uiState: PurchaseOrderEditUiState) {
    val colors = LocalGlassColors.current
    Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap16)) {
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap8)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = uiState.supplier?.name.orEmpty(), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    StatusChip(label = statusLabel(uiState.status), color = statusColor(uiState.status))
                }
                if (uiState.invoiceNumber.isNotBlank()) {
                    Text(text = "Invoice: ${uiState.invoiceNumber}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                if (uiState.notes.isNotBlank()) {
                    Text(text = uiState.notes, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
            }
        }

        Text(text = "Line items", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)

        uiState.lineItems.forEach { item ->
            GlassCard {
                Column {
                    Text(text = item.productName, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ordered ${item.quantityOrdered} - Received ${item.quantityReceived}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "$%.2f each".format(item.unitCost.toDoubleOrNull() ?: 0.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Total", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Text(text = "$%.2f".format(uiState.totalAmount), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
    }
}

@Composable
private fun SupplierSection(uiState: PurchaseOrderEditUiState, viewModel: PurchaseOrderEditViewModel) {
    val colors = LocalGlassColors.current

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap12)) {
            Text(text = "Supplier", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)

            if (uiState.supplier != null && !uiState.isPickingSupplier) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = uiState.supplier.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    GlassButton(text = "Change", onClick = viewModel::openSupplierPicker, style = GlassButtonStyle.SECONDARY)
                }
            } else {
                GlassTextField(
                    value = uiState.supplierQuery,
                    onValueChange = viewModel::onSupplierQueryChange,
                    label = "Search suppliers",
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                )
                if (uiState.supplierResults.isEmpty()) {
                    Text(text = "No suppliers found", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap8)) {
                        uiState.supplierResults.forEach { supplier ->
                            SupplierResultRow(supplier = supplier, onClick = { viewModel.onSupplierSelected(supplier) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierResultRow(supplier: Supplier, onClick: () -> Unit) {
    val colors = LocalGlassColors.current
    GlassCard(onClick = onClick) {
        Text(text = supplier.name, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
    }
}

@Composable
private fun LineItemsSection(uiState: PurchaseOrderEditUiState, viewModel: PurchaseOrderEditViewModel) {
    val colors = LocalGlassColors.current

    Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap12)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Line items", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            GlassIconButton(onClick = viewModel::openProductPicker) {
                Icon(Icons.Filled.Add, contentDescription = "Add item", tint = GlassPalette.AccentPrimary)
            }
        }

        if (uiState.isPickingProduct) {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap12)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Pick a product", style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                        GlassIconButton(onClick = viewModel::closeProductPicker) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.textSecondary)
                        }
                    }
                    GlassTextField(
                        value = uiState.productQuery,
                        onValueChange = viewModel::onProductQueryChange,
                        label = "Search products",
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                    )
                    if (uiState.productResults.isEmpty()) {
                        Text(text = "No products found", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap8)) {
                            uiState.productResults.forEach { product ->
                                ProductResultRow(product = product, onClick = { viewModel.onProductSelected(product) })
                            }
                        }
                    }
                }
            }
        }

        if (uiState.lineItems.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Add,
                title = "No items yet",
                message = "Add at least one product to this purchase order."
            )
        } else {
            uiState.lineItems.forEach { item ->
                LineItemRow(item = item, viewModel = viewModel)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Total", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Text(text = "$%.2f".format(uiState.totalAmount), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
    }
}

@Composable
private fun ProductResultRow(product: Product, onClick: () -> Unit) {
    val colors = LocalGlassColors.current
    GlassCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = product.name, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text(text = "$%.2f cost".format(product.costPrice), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
    }
}

@Composable
private fun LineItemRow(item: PurchaseOrderLineItemDraft, viewModel: PurchaseOrderEditViewModel) {
    val colors = LocalGlassColors.current
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap8)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.productName, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
                GlassIconButton(onClick = { viewModel.removeLineItem(item.productId) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove item", tint = GlassPalette.AccentDanger)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GlassSpacing.gap12)
            ) {
                GlassTextField(
                    value = item.quantityOrdered,
                    onValueChange = { viewModel.onLineQuantityChange(item.productId, it) },
                    label = "Quantity",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                GlassTextField(
                    value = item.unitCost,
                    onValueChange = { viewModel.onLineUnitCostChange(item.productId, it) },
                    label = "Unit cost",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }
            val lineTotal = (item.quantityOrdered.toDoubleOrNull() ?: 0.0) * (item.unitCost.toDoubleOrNull() ?: 0.0)
            Text(
                text = "Line total $%.2f".format(lineTotal),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

private fun statusLabel(status: PurchaseOrderStatus): String = when (status) {
    PurchaseOrderStatus.DRAFT -> "Draft"
    PurchaseOrderStatus.ORDERED -> "Ordered"
    PurchaseOrderStatus.PARTIALLY_RECEIVED -> "Partially received"
    PurchaseOrderStatus.RECEIVED -> "Received"
    PurchaseOrderStatus.CANCELLED -> "Cancelled"
}

private fun statusColor(status: PurchaseOrderStatus): androidx.compose.ui.graphics.Color = when (status) {
    PurchaseOrderStatus.DRAFT -> GlassPalette.AccentInfo
    PurchaseOrderStatus.ORDERED -> GlassPalette.AccentInfo
    PurchaseOrderStatus.PARTIALLY_RECEIVED -> GlassPalette.AccentWarning
    PurchaseOrderStatus.RECEIVED -> GlassPalette.AccentSuccess
    PurchaseOrderStatus.CANCELLED -> GlassPalette.AccentDanger
}
