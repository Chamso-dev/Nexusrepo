package com.nexus.grocerypos.feature.customers

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
import androidx.compose.material.icons.filled.People
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
import com.nexus.grocerypos.domain.model.Customer

@Composable
fun CustomersScreen(
    onAddCustomer: () -> Unit,
    onEditCustomer: (Long) -> Unit,
    viewModel: CustomersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(
                title = "Customers",
                subtitle = "${uiState.customers.size} on file",
                actions = {
                    GlassIconButton(onClick = onAddCustomer) {
                        Icon(Icons.Filled.Add, contentDescription = "Add customer", tint = GlassPalette.AccentPrimary)
                    }
                }
            )
        }

        item {
            GlassTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = "Search customers",
                placeholder = "Name, phone or email",
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )
        }

        if (uiState.customers.isEmpty() && !uiState.isLoading) {
            item {
                val hasFilter = uiState.query.isNotBlank()
                EmptyState(
                    icon = Icons.Filled.People,
                    title = if (hasFilter) "No matching customers" else "No customers yet",
                    message = if (hasFilter) {
                        "No customers match your search. Try a different name, phone or email."
                    } else {
                        "Add your first customer to start tracking their purchases and balance."
                    },
                    actionLabel = if (hasFilter) null else "Add customer",
                    onAction = if (hasFilter) null else onAddCustomer
                )
            }
        } else {
            items(uiState.customers, key = { it.id }) { customer ->
                CustomerRow(customer = customer, onClick = { onEditCustomer(customer.id) })
            }
        }
    }
}

@Composable
private fun CustomerRow(customer: Customer, onClick: () -> Unit) {
    val colors = LocalGlassColors.current

    GlassCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = customer.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                Text(
                    text = customer.phone ?: "No phone on file",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Text(
                text = "$%.2f".format(customer.balance),
                style = MaterialTheme.typography.bodyLarge,
                color = if (customer.balance > 0) GlassPalette.AccentWarning else colors.textPrimary
            )
        }
    }
}
