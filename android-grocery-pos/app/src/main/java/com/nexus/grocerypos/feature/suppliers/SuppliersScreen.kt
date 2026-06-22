package com.nexus.grocerypos.feature.suppliers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
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
import com.nexus.grocerypos.core.designsystem.components.GlassIconButton
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.Supplier

@Composable
fun SuppliersScreen(
    onAddSupplier: () -> Unit,
    onEditSupplier: (Long) -> Unit,
    viewModel: SuppliersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(
                title = "Suppliers",
                subtitle = "${uiState.suppliers.size} on file",
                actions = {
                    GlassIconButton(onClick = onAddSupplier) {
                        Icon(Icons.Filled.Add, contentDescription = "Add supplier", tint = GlassPalette.AccentPrimary)
                    }
                }
            )
        }

        item {
            GlassTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = "Search suppliers",
                placeholder = "Name, phone or email",
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )
        }

        if (uiState.suppliers.isEmpty() && !uiState.isLoading) {
            item {
                val hasFilter = uiState.query.isNotBlank()
                EmptyState(
                    icon = Icons.Filled.LocalShipping,
                    title = if (hasFilter) "No matching suppliers" else "No suppliers yet",
                    message = if (hasFilter) {
                        "No suppliers match your search. Try a different name, phone or email."
                    } else {
                        "Add your first supplier to start tracking purchases and balances owed."
                    },
                    actionLabel = if (hasFilter) null else "Add supplier",
                    onAction = if (hasFilter) null else onAddSupplier
                )
            }
        } else {
            items(uiState.suppliers, key = { it.id }) { supplier ->
                SupplierRow(supplier = supplier, onClick = { onEditSupplier(supplier.id) })
            }
        }
    }
}

@Composable
private fun SupplierRow(supplier: Supplier, onClick: () -> Unit) {
    val colors = LocalGlassColors.current

    GlassCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = supplier.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                Text(
                    text = supplier.phone ?: "No phone on file",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Text(
                text = "$%.2f".format(supplier.balanceOwed),
                style = MaterialTheme.typography.bodyLarge,
                color = if (supplier.balanceOwed > 0) GlassPalette.AccentWarning else colors.textPrimary
            )
        }
    }
}
