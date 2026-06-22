package com.nexus.grocerypos.feature.suppliers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
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
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassButtonStyle
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.GlassSpacing
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

@Composable
fun SupplierEditScreen(
    supplierId: Long?,
    onDone: () -> Unit,
    viewModel: SupplierEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGlassColors.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(supplierId) {
        viewModel.loadSupplier(supplierId)
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
                title = if (supplierId == null) "New supplier" else "Edit supplier",
                subtitle = if (supplierId == null) "Add a new supplier" else uiState.name
            )
        }

        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(GlassSpacing.gap12)) {
                    GlassTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Name",
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError
                    )
                    GlassTextField(
                        value = uiState.contactPerson,
                        onValueChange = viewModel::onContactPersonChange,
                        label = "Contact person"
                    )
                    GlassTextField(
                        value = uiState.phone,
                        onValueChange = viewModel::onPhoneChange,
                        label = "Phone",
                        keyboardType = KeyboardType.Phone
                    )
                    GlassTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email",
                        keyboardType = KeyboardType.Email
                    )
                    GlassTextField(
                        value = uiState.address,
                        onValueChange = viewModel::onAddressChange,
                        label = "Address"
                    )
                    GlassTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::onNotesChange,
                        label = "Notes",
                        singleLine = false
                    )
                }
            }
        }

        if (supplierId != null) {
            item {
                GlassCard {
                    Column {
                        Text(text = "Balance owed to supplier", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(GlassSpacing.gap8))
                        Text(
                            text = "$%.2f".format(uiState.balanceOwed),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (uiState.balanceOwed > 0) GlassPalette.AccentWarning else colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(GlassSpacing.gap16))
                        Text(text = "Record payment", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(GlassSpacing.gap8))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(GlassSpacing.gap12),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassTextField(
                                value = uiState.paymentAmount,
                                onValueChange = viewModel::onPaymentAmountChange,
                                label = "Amount",
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f)
                            )
                            GlassButton(
                                text = "Apply",
                                onClick = {
                                    val amount = uiState.paymentAmount.toDoubleOrNull()
                                    if (amount != null && amount > 0) viewModel.recordPayment(amount)
                                },
                                enabled = !uiState.isSaving && uiState.paymentAmount.toDoubleOrNull() != null
                            )
                        }
                    }
                }
            }
        }

        if (uiState.errorMessage != null) {
            item {
                Text(text = uiState.errorMessage!!, style = MaterialTheme.typography.bodySmall, color = GlassPalette.AccentDanger)
            }
        }

        item {
            GlassButton(
                text = "Save supplier",
                onClick = viewModel::save,
                loading = uiState.isSaving,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (supplierId != null) {
            item {
                GlassButton(
                    text = "Delete supplier",
                    onClick = { showDeleteConfirm = true },
                    style = GlassButtonStyle.DANGER,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete supplier",
            message = "This will permanently remove ${uiState.name} and cannot be undone.",
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
