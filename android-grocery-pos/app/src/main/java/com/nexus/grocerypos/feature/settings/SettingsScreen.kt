package com.nexus.grocerypos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

@Composable
fun SettingsScreen(
    onManageUsers: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGlassColors.current
    var backupToRestore by remember { mutableStateOf<BackupFile?>(null) }

    if (backupToRestore != null) {
        ConfirmDialog(
            title = "Restore this backup?",
            message = "This will overwrite all current data with the contents of \"${backupToRestore!!.name}\". This cannot be undone.",
            confirmLabel = "Restore",
            isDestructive = true,
            onConfirm = {
                viewModel.restoreBackup(backupToRestore!!.path)
                backupToRestore = null
            },
            onDismiss = { backupToRestore = null }
        )
    }

    if (uiState.restoreComplete) {
        ConfirmDialog(
            title = "Restore complete",
            message = "The backup was restored successfully. Close and reopen the app for the restored data to take effect.",
            confirmLabel = "OK",
            dismissLabel = "OK",
            onConfirm = viewModel::dismissRestoreComplete,
            onDismiss = viewModel::dismissRestoreComplete
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(title = "Settings", subtitle = "Business, tax, receipts and data")
        }

        item {
            GlassCard {
                Column {
                    SectionHeader(icon = Icons.Filled.Storefront, title = "Business info")
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassTextField(value = uiState.form.businessName, onValueChange = viewModel::onBusinessNameChange, label = "Business name")
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(value = uiState.form.address, onValueChange = viewModel::onAddressChange, label = "Address")
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(value = uiState.form.phone, onValueChange = viewModel::onPhoneChange, label = "Phone", keyboardType = KeyboardType.Phone)
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(value = uiState.form.email, onValueChange = viewModel::onEmailChange, label = "Email", keyboardType = KeyboardType.Email)
                }
            }
        }

        item {
            GlassCard {
                Column {
                    SectionHeader(icon = Icons.Filled.Receipt, title = "Tax & currency")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassTextField(
                            value = uiState.form.currencyCode,
                            onValueChange = viewModel::onCurrencyCodeChange,
                            label = "Currency code",
                            modifier = Modifier.weight(1f)
                        )
                        GlassTextField(
                            value = uiState.form.currencySymbol,
                            onValueChange = viewModel::onCurrencySymbolChange,
                            label = "Symbol",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(
                        value = if (uiState.form.taxRatePercent == 0.0) "" else uiState.form.taxRatePercent.toString(),
                        onValueChange = viewModel::onTaxRateChange,
                        label = "Tax rate (%)",
                        placeholder = "0.0",
                        keyboardType = KeyboardType.Decimal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Tax inclusive pricing", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                            Text(
                                text = "Prices already include tax",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Switch(checked = uiState.form.taxInclusive, onCheckedChange = viewModel::onTaxInclusiveChange)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(
                        value = if (uiState.form.lowStockThresholdDefault == 0.0) "" else uiState.form.lowStockThresholdDefault.toString(),
                        onValueChange = viewModel::onLowStockThresholdChange,
                        label = "Default low stock threshold",
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }
        }

        item {
            GlassCard {
                Column {
                    SectionHeader(icon = Icons.Filled.Receipt, title = "Receipt")
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassTextField(
                        value = uiState.form.receiptFooterMessage,
                        onValueChange = viewModel::onReceiptFooterChange,
                        label = "Receipt footer message",
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Show logo on receipt", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        Switch(checked = uiState.form.receiptShowLogo, onCheckedChange = viewModel::onReceiptShowLogoChange)
                    }
                }
            }
        }

        item {
            Column {
                if (uiState.saveError != null) {
                    Text(text = uiState.saveError!!, color = GlassPalette.AccentDanger, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (uiState.saveMessage != null) {
                    Text(text = uiState.saveMessage!!, color = GlassPalette.AccentSuccess, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                GlassButton(
                    text = "Save settings",
                    onClick = viewModel::save,
                    loading = uiState.isSaving,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            GlassCard(onClick = onManageUsers) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Filled.People, contentDescription = null, tint = GlassPalette.AccentPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Team", style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                            Text(text = "Manage user accounts and roles", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                    }
                }
            }
        }

        item {
            GlassCard {
                Column {
                    SectionHeader(icon = Icons.Filled.Backup, title = "Backup & restore")
                    Spacer(modifier = Modifier.height(16.dp))
                    if (uiState.exportError != null) {
                        Text(text = uiState.exportError!!, color = GlassPalette.AccentDanger, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (uiState.exportMessage != null) {
                        Text(text = uiState.exportMessage!!, color = GlassPalette.AccentSuccess, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    GlassButton(
                        text = "Export backup",
                        onClick = viewModel::exportBackup,
                        loading = uiState.isExporting,
                        enabled = !uiState.isExporting,
                        style = GlassButtonStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Available backups", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.restoreError != null) {
                        Text(text = uiState.restoreError!!, color = GlassPalette.AccentDanger, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (uiState.backups.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.Restore,
                            title = "No backups yet",
                            message = "Export a backup to see it listed here for future restores."
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.backups.forEach { backup ->
                                BackupRow(
                                    backup = backup,
                                    isBusy = uiState.isRestoring,
                                    onRestore = { backupToRestore = backup }
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    val colors = LocalGlassColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.Icon(icon, contentDescription = null, tint = GlassPalette.AccentPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
    }
}

@Composable
private fun BackupRow(backup: BackupFile, isBusy: Boolean, onRestore: () -> Unit) {
    val colors = LocalGlassColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = backup.name, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
        GlassButton(
            text = "Restore",
            onClick = onRestore,
            enabled = !isBusy,
            style = GlassButtonStyle.NEUTRAL
        )
    }
}
