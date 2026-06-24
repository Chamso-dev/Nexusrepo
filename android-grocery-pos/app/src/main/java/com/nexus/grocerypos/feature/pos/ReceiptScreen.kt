package com.nexus.grocerypos.feature.pos

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.EmptyState
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassButtonStyle
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassIconButton
import com.nexus.grocerypos.core.designsystem.components.GlassSurface
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.GlassShapes
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.printing.PrinterDevice

@Composable
fun ReceiptScreen(
    saleId: Long,
    onDone: () -> Unit,
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    LaunchedEffect(saleId) { viewModel.load(saleId) }
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGlassColors.current
    val context = LocalContext.current

    val printPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.openPrinterPicker()
    }

    fun requestPrint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            printPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            viewModel.openPrinterPicker()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassTopBar(
            title = "Receipt",
            subtitle = uiState.sale?.let { "#${it.receiptNumber}" },
            actions = {
                if (uiState.receiptText.isNotBlank()) {
                    GlassIconButton(onClick = { requestPrint() }) {
                        Icon(Icons.Filled.Print, contentDescription = "Print receipt", tint = GlassPalette.AccentPrimary)
                    }
                    GlassIconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, uiState.receiptText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share receipt"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share receipt", tint = GlassPalette.AccentPrimary)
                    }
                }
            }
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                uiState.isLoading -> Unit
                uiState.errorMessage != null -> EmptyState(
                    icon = Icons.Filled.Receipt,
                    title = "Receipt unavailable",
                    message = uiState.errorMessage.orEmpty()
                )
                else -> GlassCard(strong = true, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = uiState.receiptText,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textPrimary,
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    )
                }
            }
        }

        uiState.statusMessage?.let { message ->
            GlassCard(onClick = { viewModel.consumeStatusMessage() }) {
                Text(text = message, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(
                text = if (uiState.isPrinting) "Printing…" else "Print",
                onClick = { requestPrint() },
                style = GlassButtonStyle.SECONDARY,
                enabled = uiState.receiptText.isNotBlank() && !uiState.isPrinting,
                loading = uiState.isPrinting,
                modifier = Modifier.weight(1f)
            )
            GlassButton(
                text = "Done",
                onClick = onDone,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (uiState.showPrinterPicker) {
        PrinterPickerDialog(
            printers = uiState.printers,
            onSelect = viewModel::print,
            onDismiss = viewModel::dismissPrinterPicker
        )
    }
}

@Composable
private fun PrinterPickerDialog(
    printers: List<PrinterDevice>,
    onSelect: (PrinterDevice) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalGlassColors.current
    Dialog(onDismissRequest = onDismiss) {
        GlassSurface(strong = true, shape = GlassShapes.cardLarge) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Select printer",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                printers.forEach { printer ->
                    GlassCard(onClick = { onSelect(printer) }) {
                        Column {
                            Text(
                                text = printer.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textPrimary
                            )
                            Text(
                                text = printer.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp, Arrangement.End)) {
                    GlassButton(text = "Cancel", onClick = onDismiss, style = GlassButtonStyle.SECONDARY)
                }
            }
        }
    }
}
