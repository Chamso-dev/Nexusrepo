package com.nexus.grocerypos.feature.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassButtonStyle
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassChip
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.components.LoadingOverlay
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.Brand
import com.nexus.grocerypos.domain.model.Category
import com.nexus.grocerypos.domain.model.StockUnit

@Composable
fun ProductEditScreen(
    productId: Long?,
    onDone: () -> Unit,
    viewModel: ProductEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val brands by viewModel.brands.collectAsState()
    val colors = LocalGlassColors.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
        if (uiState.isSaved || uiState.isDeleted) onDone()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassTopBar(
                    title = if (productId == null) "New product" else "Edit product",
                    subtitle = if (productId == null) "Add a product to your catalog" else uiState.name
                )
            }

            item {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassTextField(
                            value = uiState.name,
                            onValueChange = viewModel::onNameChange,
                            label = "Name",
                            isError = uiState.nameError != null,
                            supportingText = uiState.nameError
                        )
                        GlassTextField(
                            value = uiState.sku,
                            onValueChange = viewModel::onSkuChange,
                            label = "SKU",
                            isError = uiState.skuError != null,
                            supportingText = uiState.skuError
                        )
                        GlassTextField(
                            value = uiState.barcode,
                            onValueChange = viewModel::onBarcodeChange,
                            label = "Barcode (optional)",
                            supportingText = "Can also be scanned later from the POS screen",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }

            item {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Category", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        OptionDropdown(
                            items = categories,
                            selectedId = uiState.categoryId,
                            label = { it.name },
                            placeholder = "No category",
                            onSelected = viewModel::onCategorySelected
                        )
                        AddOptionRow(placeholder = "New category name", onAdd = viewModel::addCategory)
                    }
                }
            }

            item {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Brand", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        OptionDropdown(
                            items = brands,
                            selectedId = uiState.brandId,
                            label = { it.name },
                            placeholder = "No brand",
                            onSelected = viewModel::onBrandSelected
                        )
                        AddOptionRow(placeholder = "New brand name", onAdd = viewModel::addBrand)
                    }
                }
            }

            item {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassTextField(
                            value = uiState.costPrice,
                            onValueChange = viewModel::onCostPriceChange,
                            label = "Cost price",
                            isError = uiState.costPriceError != null,
                            supportingText = uiState.costPriceError,
                            keyboardType = KeyboardType.Decimal
                        )
                        GlassTextField(
                            value = uiState.sellingPrice,
                            onValueChange = viewModel::onSellingPriceChange,
                            label = "Selling price",
                            isError = uiState.sellingPriceError != null,
                            supportingText = uiState.sellingPriceError,
                            keyboardType = KeyboardType.Decimal
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Profit per unit: $%.2f".format(uiState.profitPerUnit),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "Margin: %.1f%%".format(uiState.marginPercent),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.marginPercent >= 0) GlassPalette.AccentSuccess else GlassPalette.AccentDanger
                            )
                        }
                    }
                }
            }

            item {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassTextField(
                            value = uiState.stockQuantity,
                            onValueChange = viewModel::onStockQuantityChange,
                            label = "Stock quantity",
                            isError = uiState.stockQuantityError != null,
                            supportingText = uiState.stockQuantityError,
                            keyboardType = KeyboardType.Decimal
                        )
                        GlassTextField(
                            value = uiState.lowStockThreshold,
                            onValueChange = viewModel::onLowStockThresholdChange,
                            label = "Low stock threshold",
                            keyboardType = KeyboardType.Decimal
                        )
                        Text(text = "Unit", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StockUnit.values().forEach { unit ->
                                GlassChip(
                                    label = unit.name,
                                    selected = uiState.unit == unit,
                                    onClick = { viewModel.onUnitSelected(unit) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Active", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                            Text(
                                text = "Inactive products are hidden from the POS screen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = uiState.isActive,
                            onCheckedChange = viewModel::onActiveChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = GlassPalette.AccentPrimary)
                        )
                    }
                }
            }

            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassPalette.AccentDanger
                    )
                }
            }

            item {
                GlassButton(
                    text = "Save product",
                    onClick = viewModel::save,
                    enabled = !uiState.isSaving,
                    loading = uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (productId != null) {
                item {
                    GlassButton(
                        text = "Delete product",
                        onClick = { showDeleteDialog = true },
                        style = GlassButtonStyle.DANGER,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (uiState.isLoading) {
            LoadingOverlay()
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete product?",
            message = "This will permanently remove \"${uiState.name}\" from your catalog. This cannot be undone.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

private fun idOf(item: Any): Long = when (item) {
    is Category -> item.id
    is Brand -> item.id
    else -> -1L
}

@Composable
private fun <T : Any> OptionDropdown(
    items: List<T>,
    selectedId: Long?,
    label: (T) -> String,
    placeholder: String,
    onSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = items.firstOrNull { idOf(it) == selectedId }?.let(label) ?: placeholder

    Box(modifier = Modifier.fillMaxWidth()) {
        GlassTextField(
            value = selectedLabel,
            onValueChange = {},
            label = "Selection",
            enabled = false,
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(placeholder) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(label(item)) },
                    onClick = {
                        onSelected(idOf(item))
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AddOptionRow(placeholder: String, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassTextField(
            value = text,
            onValueChange = { text = it },
            label = placeholder,
            modifier = Modifier.weight(1f)
        )
        GlassButton(
            text = "Add",
            style = GlassButtonStyle.SECONDARY,
            onClick = {
                onAdd(text)
                text = ""
            }
        )
    }
}
