package com.nexus.grocerypos.feature.products

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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
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
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassChip
import com.nexus.grocerypos.core.designsystem.components.GlassIconButton
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.ProductWithDetails

@Composable
fun ProductsScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(
                title = "Products",
                subtitle = "${uiState.products.size} in catalog",
                actions = {
                    GlassIconButton(onClick = onAddProduct) {
                        Icon(Icons.Filled.Add, contentDescription = "Add product", tint = GlassPalette.AccentPrimary)
                    }
                }
            )
        }

        item {
            GlassTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = "Search products",
                placeholder = "Name, SKU or barcode",
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )
        }

        if (uiState.categories.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        GlassChip(
                            label = "All",
                            selected = uiState.selectedCategoryId == null,
                            onClick = { viewModel.onCategorySelected(null) }
                        )
                    }
                    items(uiState.categories, key = { it.id }) { category ->
                        GlassChip(
                            label = category.name,
                            selected = uiState.selectedCategoryId == category.id,
                            onClick = { viewModel.onCategorySelected(category.id) }
                        )
                    }
                }
            }
        }

        if (uiState.products.isEmpty() && !uiState.isLoading) {
            item {
                val hasFilter = uiState.query.isNotBlank() || uiState.selectedCategoryId != null
                EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = if (hasFilter) "No matching products" else "No products yet",
                    message = if (hasFilter) {
                        "No products match your search or filter. Try adjusting them."
                    } else {
                        "Add your first product to start building your catalog."
                    },
                    actionLabel = if (hasFilter) null else "Add product",
                    onAction = if (hasFilter) null else onAddProduct
                )
            }
        } else {
            items(uiState.products, key = { it.product.id }) { item ->
                ProductRow(item = item, onClick = { onEditProduct(item.product.id) })
            }
        }
    }
}

@Composable
private fun ProductRow(item: ProductWithDetails, onClick: () -> Unit) {
    val colors = LocalGlassColors.current
    val product = item.product

    GlassCard(onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    val details = listOfNotNull(item.categoryName, item.brandName).joinToString(" • ")
                    Text(
                        text = if (details.isNotEmpty()) "SKU ${product.sku} • $details" else "SKU ${product.sku}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$%.2f".format(product.sellingPrice),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "%.0f%% margin".format(product.marginPercent),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.marginPercent >= 0) GlassPalette.AccentSuccess else GlassPalette.AccentDanger
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.stockQuantity} ${product.unit.name.lowercase()} in stock",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.isLowStock) GlassPalette.AccentWarning else colors.textSecondary
                )
                if (product.isLowStock) {
                    Text(text = "Low stock", style = MaterialTheme.typography.labelSmall, color = GlassPalette.AccentWarning)
                }
            }
        }
    }
}
