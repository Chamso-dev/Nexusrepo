package com.nexus.grocerypos.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.EmptyState
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.components.StatCard
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.GlassSpacing
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

@Composable
fun DashboardScreen(
    onNavigateToProducts: () -> Unit,
    onNavigateToPos: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToReports: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val colors = LocalGlassColors.current
    val symbol = settings.currencySymbol

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(title = "Dashboard", subtitle = settings.businessName.ifBlank { "Today's overview" })
        }

        item {
            val summary = uiState.todaySummary
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Revenue today",
                    value = "$symbol%.2f".format(summary?.revenue ?: 0.0),
                    icon = Icons.Filled.AttachMoney,
                    accentColor = GlassPalette.AccentSuccess
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Profit today",
                    value = "$symbol%.2f".format(summary?.profit ?: 0.0),
                    icon = Icons.Filled.TrendingUp,
                    accentColor = GlassPalette.AccentPrimary
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Transactions",
                    value = "${uiState.todaySummary?.transactionCount ?: 0}",
                    icon = Icons.Filled.Receipt,
                    accentColor = GlassPalette.AccentInfo
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Low stock items",
                    value = "${uiState.lowStockProducts.size}",
                    icon = Icons.Filled.Warning,
                    accentColor = GlassPalette.AccentWarning
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassCard(modifier = Modifier.weight(1f), onClick = onNavigateToPos) {
                    QuickAction(label = "New sale", icon = Icons.Filled.PointOfSale)
                }
                GlassCard(modifier = Modifier.weight(1f), onClick = onNavigateToProducts) {
                    QuickAction(label = "Manage products", icon = Icons.Filled.Inventory2)
                }
                GlassCard(modifier = Modifier.weight(1f), onClick = onNavigateToReports) {
                    QuickAction(label = "View reports", icon = Icons.Filled.Receipt)
                }
            }
        }

        item {
            Text(text = "Low stock alerts", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }

        if (uiState.lowStockProducts.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "Stock levels look good",
                    message = "No products are currently below their low-stock threshold."
                )
            }
        } else {
            items(uiState.lowStockProducts, key = { it.id }) { product ->
                GlassCard(onClick = onNavigateToInventory) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = product.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                            Text(text = "SKU ${product.sku}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                        Text(
                            text = "${product.stockQuantity} left",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassPalette.AccentWarning
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val colors = LocalGlassColors.current
    Column {
        androidx.compose.material3.Icon(icon, contentDescription = null, tint = GlassPalette.AccentPrimary)
        Spacer(modifier = Modifier.height(GlassSpacing.gap8))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
    }
}
