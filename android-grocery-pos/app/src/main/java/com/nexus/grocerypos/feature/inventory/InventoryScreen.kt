package com.nexus.grocerypos.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.EmptyState
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassButtonStyle
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassChip
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.components.StatusChip
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.GlassSpacing
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.InventoryTransaction
import com.nexus.grocerypos.domain.model.InventoryTransactionType
import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.model.ProductWithDetails
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun InventoryScreen(viewModel: InventoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(title = "Inventory", subtitle = "Stock movements and adjustments")
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(
                    label = "Low stock",
                    selected = uiState.selectedTab == InventoryTab.LOW_STOCK,
                    onClick = { viewModel.onTabSelected(InventoryTab.LOW_STOCK) }
                )
                GlassChip(
                    label = "Adjust stock",
                    selected = uiState.selectedTab == InventoryTab.ADJUST,
                    onClick = { viewModel.onTabSelected(InventoryTab.ADJUST) }
                )
                GlassChip(
                    label = "History",
                    selected = uiState.selectedTab == InventoryTab.HISTORY,
                    onClick = { viewModel.onTabSelected(InventoryTab.HISTORY) }
                )
            }
        }

        when (uiState.selectedTab) {
            InventoryTab.LOW_STOCK -> lowStockSection(
                products = uiState.lowStockProducts,
                onAdjust = viewModel::startAdjustFor
            )
            InventoryTab.ADJUST -> adjustStockSection(
                uiState = uiState,
                viewModel = viewModel
            )
            InventoryTab.HISTORY -> historySection(history = uiState.history)
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

private fun LazyListScope.lowStockSection(
    products: List<Product>,
    onAdjust: (Product) -> Unit
) {
    if (products.isEmpty()) {
        item {
            EmptyState(
                icon = Icons.Filled.Inventory2,
                title = "Stock levels look good",
                message = "No products are currently below their low-stock threshold."
            )
        }
    } else {
        items(products, key = { it.id }) { product ->
            LowStockRow(product = product, onAdjust = { onAdjust(product) })
        }
    }
}

@Composable
private fun LowStockRow(product: Product, onAdjust: () -> Unit) {
    val colors = LocalGlassColors.current
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                Text(text = "SKU ${product.sku}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Text(
                    text = "${product.stockQuantity} / ${product.lowStockThreshold} threshold",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassPalette.AccentWarning
                )
            }
            GlassButton(text = "Adjust", onClick = onAdjust, style = GlassButtonStyle.SECONDARY)
        }
    }
}

private fun LazyListScope.adjustStockSection(
    uiState: InventoryUiState,
    viewModel: InventoryViewModel
) {
    val selectedProduct = uiState.selectedProduct
    if (selectedProduct == null) {
        item {
            GlassTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = "Search products",
                placeholder = "Name, SKU or barcode",
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )
        }

        if (uiState.searchQuery.isNotBlank()) {
            if (uiState.searchResults.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Search,
                        title = "No matching products",
                        message = "Try a different name, SKU or barcode."
                    )
                }
            } else {
                items(uiState.searchResults, key = { it.product.id }) { item ->
                    ProductPickRow(item = item, onClick = { viewModel.onProductSelected(item.product) })
                }
            }
        }
    } else {
        item {
            SelectedProductCard(
                product = selectedProduct,
                onChangeProduct = viewModel::onClearSelectedProduct
            )
        }

        item {
            Text(text = "Transaction type", style = MaterialTheme.typography.titleSmall, color = LocalGlassColors.current.textPrimary)
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transactionTypeOptions, key = { it.first }) { (type, label) ->
                    GlassChip(
                        label = label,
                        selected = uiState.transactionType == type,
                        onClick = { viewModel.onTransactionTypeSelected(type) }
                    )
                }
            }
        }

        item {
            GlassTextField(
                value = uiState.quantityInput,
                onValueChange = viewModel::onQuantityChange,
                label = if (uiState.transactionType == InventoryTransactionType.ADJUSTMENT) "New stock quantity" else "Quantity",
                keyboardType = KeyboardType.Number,
                isError = uiState.submitError != null
            )
        }

        item {
            GlassTextField(
                value = uiState.reasonInput,
                onValueChange = viewModel::onReasonChange,
                label = "Reason (optional)"
            )
        }

        val submitError = uiState.submitError
        if (submitError != null) {
            item {
                Text(text = submitError, style = MaterialTheme.typography.bodySmall, color = GlassPalette.AccentDanger)
            }
        }

        if (uiState.submitSuccess) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GlassPalette.AccentSuccess)
                    Text(text = "Stock updated successfully", style = MaterialTheme.typography.bodySmall, color = GlassPalette.AccentSuccess)
                }
            }
        }

        item {
            GlassButton(
                text = "Submit",
                onClick = viewModel::submitAdjustment,
                enabled = !uiState.isSubmitting,
                loading = uiState.isSubmitting
            )
        }
    }
}

private val transactionTypeOptions = listOf(
    InventoryTransactionType.STOCK_IN to "Stock in",
    InventoryTransactionType.STOCK_OUT to "Stock out",
    InventoryTransactionType.ADJUSTMENT to "Adjustment",
    InventoryTransactionType.RETURN to "Return"
)

@Composable
private fun ProductPickRow(item: ProductWithDetails, onClick: () -> Unit) {
    val colors = LocalGlassColors.current
    val product = item.product
    GlassCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = product.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                Text(text = "SKU ${product.sku}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Text(
                text = "${product.stockQuantity} ${product.unit.name.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun SelectedProductCard(product: Product, onChangeProduct: () -> Unit) {
    val colors = LocalGlassColors.current
    GlassCard(strong = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = product.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                Text(
                    text = "Current stock: ${product.stockQuantity} ${product.unit.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            GlassButton(text = "Change", onClick = onChangeProduct, style = GlassButtonStyle.NEUTRAL)
        }
    }
}

private fun LazyListScope.historySection(history: List<InventoryTransaction>) {
    if (history.isEmpty()) {
        item {
            EmptyState(
                icon = Icons.Filled.History,
                title = "No stock movements yet",
                message = "Stock-in, stock-out, adjustments and returns will show up here."
            )
        }
    } else {
        items(history, key = { it.id }) { transaction ->
            HistoryRow(transaction = transaction)
        }
    }
}

@Composable
private fun HistoryRow(transaction: InventoryTransaction) {
    val colors = LocalGlassColors.current
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    GlassCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = transaction.productName, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    Text(text = dateFormat.format(transaction.createdAt), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                StatusChip(label = transactionTypeLabel(transaction.type), color = transactionTypeColor(transaction.type))
            }
            Spacer(modifier = Modifier.height(GlassSpacing.gap8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%+.2f".format(transaction.quantityDelta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (transaction.quantityDelta >= 0) GlassPalette.AccentSuccess else GlassPalette.AccentDanger
                )
                Text(
                    text = "Resulting: ${transaction.resultingQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            val reason = transaction.reason
            if (!reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(GlassSpacing.gap4))
                Text(text = reason, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
    }
}

private fun transactionTypeLabel(type: InventoryTransactionType): String = when (type) {
    InventoryTransactionType.STOCK_IN -> "Stock in"
    InventoryTransactionType.STOCK_OUT -> "Stock out"
    InventoryTransactionType.ADJUSTMENT -> "Adjustment"
    InventoryTransactionType.SALE -> "Sale"
    InventoryTransactionType.PURCHASE_RECEIVE -> "Purchase"
    InventoryTransactionType.RETURN -> "Return"
}

private fun transactionTypeColor(type: InventoryTransactionType) = when (type) {
    InventoryTransactionType.STOCK_IN -> GlassPalette.AccentSuccess
    InventoryTransactionType.STOCK_OUT -> GlassPalette.AccentDanger
    InventoryTransactionType.ADJUSTMENT -> GlassPalette.AccentWarning
    InventoryTransactionType.SALE -> GlassPalette.AccentInfo
    InventoryTransactionType.PURCHASE_RECEIVE -> GlassPalette.AccentPrimary
    InventoryTransactionType.RETURN -> GlassPalette.AccentSecondary
}
