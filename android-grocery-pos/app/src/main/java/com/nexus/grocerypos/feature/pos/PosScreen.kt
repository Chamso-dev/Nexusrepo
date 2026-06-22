package com.nexus.grocerypos.feature.pos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.nexus.grocerypos.core.designsystem.components.GlassChip
import com.nexus.grocerypos.core.designsystem.components.GlassIconButton
import com.nexus.grocerypos.core.designsystem.components.GlassSurface
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.Customer
import com.nexus.grocerypos.domain.model.DiscountType
import com.nexus.grocerypos.domain.model.PaymentMethod
import com.nexus.grocerypos.domain.model.ProductWithDetails
import com.nexus.grocerypos.domain.model.SalePayment
import com.nexus.grocerypos.domain.usecase.pos.CartLine
import com.nexus.grocerypos.domain.usecase.pos.CartTotals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    cashierId: Long,
    onCheckoutComplete: (Long) -> Unit,
    viewModel: PosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGlassColors.current
    var showScanner by remember { mutableStateOf(false) }
    val symbol = uiState.settings.currencySymbol

    LaunchedEffect(uiState.completedSaleId) {
        val saleId = uiState.completedSaleId
        if (saleId != null) {
            onCheckoutComplete(saleId)
            viewModel.consumeCompletedSale()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassTopBar(title = "Point of sale", subtitle = "${uiState.cartLines.size} items in cart")
            }

            item {
                GlassTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    label = "Search products",
                    placeholder = "Name, SKU or barcode",
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        GlassIconButton(onClick = { showScanner = true }) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan barcode", tint = GlassPalette.AccentPrimary)
                        }
                    }
                )
            }

            if (uiState.searchResults.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.searchResults.take(6).forEach { result ->
                            SearchResultRow(item = result, symbol = symbol, onClick = { viewModel.addProductToCart(result) })
                        }
                    }
                }
            }

            item {
                Text(text = "Cart", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            }

            if (uiState.cartLines.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.ShoppingCart,
                        title = "Cart is empty",
                        message = "Search or scan a product to add it to this sale."
                    )
                }
            } else {
                items(uiState.cartLines, key = { it.productId }) { line ->
                    CartLineRow(
                        line = line,
                        symbol = symbol,
                        onQuantityChange = { qty -> viewModel.updateQuantity(line.productId, qty) },
                        onDiscountChange = { type, value -> viewModel.updateDiscount(line.productId, type, value) },
                        onRemove = { viewModel.removeLine(line.productId) }
                    )
                }
            }

            item {
                CustomerSection(
                    selectedCustomer = uiState.selectedCustomer,
                    customerQuery = uiState.customerQuery,
                    customerResults = uiState.customerResults,
                    onQueryChange = viewModel::onCustomerQueryChange,
                    onSelect = viewModel::selectCustomer
                )
            }

            item { Spacer(modifier = Modifier.height(180.dp)) }
        }

        BottomSummaryBar(
            totals = uiState.totals,
            symbol = symbol,
            errorMessage = uiState.errorMessage,
            enabled = uiState.cartLines.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter),
            onCharge = viewModel::openPaymentSheet
        )
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onResult = { code ->
                showScanner = false
                viewModel.onBarcodeScanned(code)
            },
            onDismiss = { showScanner = false }
        )
    }

    uiState.scannerError?.let { message ->
        ConfirmDialog(
            title = "Product not found",
            message = message,
            confirmLabel = "OK",
            dismissLabel = "Scan again",
            onConfirm = viewModel::dismissScannerError,
            onDismiss = {
                viewModel.dismissScannerError()
                showScanner = true
            }
        )
    }

    if (uiState.showPaymentSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = viewModel::closePaymentSheet, sheetState = sheetState) {
            PaymentSheetContent(
                totals = uiState.totals,
                symbol = symbol,
                payments = uiState.payments,
                isProcessing = uiState.isProcessing,
                onAddPayment = viewModel::addPayment,
                onRemovePayment = viewModel::removePayment,
                onConfirm = { viewModel.checkout(cashierId) }
            )
        }
    }
}

@Composable
private fun SearchResultRow(item: ProductWithDetails, symbol: String, onClick: () -> Unit) {
    val colors = LocalGlassColors.current
    val product = item.product
    GlassCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                Text(text = "SKU ${product.sku} • ${product.stockQuantity} in stock", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Text(text = "$symbol%.2f".format(product.sellingPrice), style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        }
    }
}

private fun formatQuantity(quantity: Double): String =
    if (quantity == quantity.toLong().toDouble()) quantity.toLong().toString() else "%.2f".format(quantity)

@Composable
private fun CartLineRow(
    line: CartLine,
    symbol: String,
    onQuantityChange: (Double) -> Unit,
    onDiscountChange: (DiscountType, Double) -> Unit,
    onRemove: () -> Unit
) {
    val colors = LocalGlassColors.current
    var discountText by remember(line.productId) { mutableStateOf(if (line.discountValue == 0.0) "" else formatQuantity(line.discountValue)) }

    GlassCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = line.productName, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    Text(text = "$symbol%.2f each".format(line.unitPrice), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                GlassIconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Remove, contentDescription = "Remove item", tint = GlassPalette.AccentDanger)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassIconButton(onClick = { onQuantityChange(line.quantity - 1.0) }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity", tint = colors.textPrimary)
                }
                GlassTextField(
                    value = formatQuantity(line.quantity),
                    onValueChange = { text -> text.toDoubleOrNull()?.let(onQuantityChange) },
                    label = "Qty",
                    modifier = Modifier.width(90.dp),
                    keyboardType = KeyboardType.Decimal
                )
                GlassIconButton(onClick = { onQuantityChange(line.quantity + 1.0) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase quantity", tint = colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                GlassChip(
                    label = "%",
                    selected = line.discountType == DiscountType.PERCENT,
                    onClick = { onDiscountChange(DiscountType.PERCENT, discountText.toDoubleOrNull() ?: 0.0) }
                )
                GlassChip(
                    label = symbol,
                    selected = line.discountType == DiscountType.FIXED,
                    onClick = { onDiscountChange(DiscountType.FIXED, discountText.toDoubleOrNull() ?: 0.0) }
                )
                GlassTextField(
                    value = discountText,
                    onValueChange = { text ->
                        discountText = text
                        text.toDoubleOrNull()?.let { onDiscountChange(line.discountType, it) }
                    },
                    label = "Disc",
                    modifier = Modifier.width(90.dp),
                    keyboardType = KeyboardType.Decimal
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$symbol%.2f".format(line.netTotal),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CustomerSection(
    selectedCustomer: Customer?,
    customerQuery: String,
    customerResults: List<Customer>,
    onQueryChange: (String) -> Unit,
    onSelect: (Customer?) -> Unit
) {
    val colors = LocalGlassColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Customer", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        if (selectedCustomer != null) {
            GlassCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = GlassPalette.AccentPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = selectedCustomer.name, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    }
                    GlassIconButton(onClick = { onSelect(null) }) {
                        Icon(Icons.Filled.PersonOff, contentDescription = "Remove customer", tint = colors.textSecondary)
                    }
                }
            }
        } else {
            GlassTextField(
                value = customerQuery,
                onValueChange = onQueryChange,
                label = "Walk-in customer",
                placeholder = "Search customer by name or phone (optional)",
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
            )
            if (customerResults.isNotEmpty()) {
                customerResults.take(5).forEach { customer ->
                    GlassCard(onClick = { onSelect(customer) }) {
                        Text(text = customer.name, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSummaryBar(
    totals: CartTotals,
    symbol: String,
    errorMessage: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onCharge: () -> Unit
) {
    val colors = LocalGlassColors.current
    GlassSurface(strong = true, modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SummaryRow(label = "Subtotal", value = "$symbol%.2f".format(totals.subtotal), color = colors.textSecondary)
            if (totals.discountTotal > 0) {
                SummaryRow(label = "Discount", value = "-$symbol%.2f".format(totals.discountTotal), color = GlassPalette.AccentWarning)
            }
            if (totals.taxTotal > 0) {
                SummaryRow(label = "Tax", value = "$symbol%.2f".format(totals.taxTotal), color = colors.textSecondary)
            }
            SummaryRow(label = "Total", value = "$symbol%.2f".format(totals.grandTotal), color = colors.textPrimary, emphasize = true)
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, style = MaterialTheme.typography.bodySmall, color = GlassPalette.AccentDanger)
            }
            Spacer(modifier = Modifier.height(12.dp))
            GlassButton(
                text = "Charge $symbol%.2f".format(totals.grandTotal),
                onClick = onCharge,
                enabled = enabled,
                style = GlassButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: androidx.compose.ui.graphics.Color, emphasize: Boolean = false) {
    val colors = LocalGlassColors.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
private fun PaymentSheetContent(
    totals: CartTotals,
    symbol: String,
    payments: List<SalePayment>,
    isProcessing: Boolean,
    onAddPayment: (PaymentMethod, Double, Double) -> Unit,
    onRemovePayment: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    val colors = LocalGlassColors.current
    val amountPaid = payments.sumOf { it.amount }
    val remaining = (totals.grandTotal - amountPaid).coerceAtLeast(0.0)

    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var amountText by remember { mutableStateOf("") }
    var tenderedText by remember { mutableStateOf("") }

    LaunchedEffect(remaining) {
        if (amountText.isBlank() || amountText.toDoubleOrNull() == null) {
            amountText = if (remaining > 0) "%.2f".format(remaining) else ""
            tenderedText = amountText
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "Payment", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        Text(text = "Total due: $symbol%.2f".format(totals.grandTotal), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)

        if (payments.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                payments.forEachIndexed { index, payment ->
                    GlassCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(text = payment.method.name.replace('_', ' '), style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                                if (payment.changeDue > 0) {
                                    Text(text = "Change: $symbol%.2f".format(payment.changeDue), style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "$symbol%.2f".format(payment.amount), style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                                GlassIconButton(onClick = { onRemovePayment(index) }) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Remove payment", tint = GlassPalette.AccentDanger)
                                }
                            }
                        }
                    }
                }
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PaymentMethod.entries.filter { it != PaymentMethod.SPLIT }) { method ->
                GlassChip(
                    label = method.name.replace('_', ' '),
                    selected = selectedMethod == method,
                    onClick = { selectedMethod = method }
                )
            }
        }

        GlassTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = "Amount to apply",
            keyboardType = KeyboardType.Decimal
        )

        if (selectedMethod == PaymentMethod.CASH) {
            GlassTextField(
                value = tenderedText,
                onValueChange = { tenderedText = it },
                label = "Cash tendered",
                supportingText = "Amount the customer handed over",
                keyboardType = KeyboardType.Decimal
            )
        }

        GlassButton(
            text = "Add payment",
            style = GlassButtonStyle.SECONDARY,
            onClick = {
                val amount = amountText.toDoubleOrNull() ?: return@GlassButton
                val tendered = if (selectedMethod == PaymentMethod.CASH) tenderedText.toDoubleOrNull() ?: amount else amount
                onAddPayment(selectedMethod, amount, tendered)
                amountText = ""
                tenderedText = ""
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (remaining > 0.01) {
            Text(text = "Remaining: $symbol%.2f".format(remaining), style = MaterialTheme.typography.bodyMedium, color = GlassPalette.AccentWarning)
        }

        GlassButton(
            text = if (isProcessing) "Processing..." else "Complete sale",
            onClick = onConfirm,
            enabled = remaining <= 0.01 && !isProcessing,
            loading = isProcessing,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}
