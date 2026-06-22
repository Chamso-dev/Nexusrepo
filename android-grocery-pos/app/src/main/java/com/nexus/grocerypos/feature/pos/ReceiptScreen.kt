package com.nexus.grocerypos.feature.pos

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.EmptyState
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassIconButton
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors

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

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassTopBar(
            title = "Receipt",
            subtitle = uiState.sale?.let { "#${it.receiptNumber}" },
            actions = {
                if (uiState.receiptText.isNotBlank()) {
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

        GlassButton(text = "Done", onClick = onDone, modifier = Modifier.fillMaxWidth())
    }
}
