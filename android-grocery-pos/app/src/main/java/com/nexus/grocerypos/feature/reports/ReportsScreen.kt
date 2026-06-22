package com.nexus.grocerypos.feature.reports

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.EmptyState
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassChip
import com.nexus.grocerypos.core.designsystem.components.GlassIconButton
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.components.StatCard
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.GlassSpacing
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.DailySalesPoint
import com.nexus.grocerypos.domain.model.TopSellingProduct
import java.io.File
import java.io.FileWriter

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val colors = LocalGlassColors.current
    val symbol = settings.currencySymbol
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(
                title = "Reports",
                subtitle = "Sales & inventory analytics",
                actions = {
                    GlassIconButton(
                        onClick = {
                            val csv = viewModel.buildCsvReport()
                            val uri = writeCsvAndGetUri(context, csv)
                            shareCsv(context, uri)
                        }
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Export CSV", tint = GlassPalette.AccentPrimary)
                    }
                }
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassChip(
                    label = "Today",
                    selected = uiState.period == Period.TODAY,
                    onClick = { viewModel.selectPeriod(Period.TODAY) }
                )
                GlassChip(
                    label = "This week",
                    selected = uiState.period == Period.WEEK,
                    onClick = { viewModel.selectPeriod(Period.WEEK) }
                )
                GlassChip(
                    label = "This month",
                    selected = uiState.period == Period.MONTH,
                    onClick = { viewModel.selectPeriod(Period.MONTH) }
                )
            }
        }

        item {
            val summary = uiState.summary
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Revenue",
                    value = "$symbol%.2f".format(summary?.revenue ?: 0.0),
                    icon = Icons.Filled.AttachMoney,
                    accentColor = GlassPalette.AccentSuccess
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Cost",
                    value = "$symbol%.2f".format(summary?.cost ?: 0.0),
                    icon = Icons.Filled.MoneyOff,
                    accentColor = GlassPalette.AccentWarning
                )
            }
        }
        item {
            val summary = uiState.summary
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Profit",
                    value = "$symbol%.2f".format(summary?.profit ?: 0.0),
                    icon = Icons.Filled.TrendingUp,
                    accentColor = GlassPalette.AccentPrimary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Transactions",
                    value = "${summary?.transactionCount ?: 0}",
                    icon = Icons.Filled.Receipt,
                    accentColor = GlassPalette.AccentInfo
                )
            }
        }
        item {
            StatCard(
                label = "Items sold",
                value = "%.2f".format(uiState.summary?.itemsSold ?: 0.0),
                icon = Icons.Filled.ShoppingCart,
                accentColor = GlassPalette.AccentSecondary
            )
        }

        item {
            Text(text = "Sales trend", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
        item {
            if (uiState.dailyTrend.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.BarChart,
                    title = "No sales data",
                    message = "There is no sales activity in this period yet."
                )
            } else {
                SalesTrendChart(points = uiState.dailyTrend, currencySymbol = symbol)
            }
        }

        item {
            Text(text = "Top selling products", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
        if (uiState.topSellingProducts.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    title = "No sales yet",
                    message = "Top-selling products will appear here once sales are recorded for this period."
                )
            }
        } else {
            items(uiState.topSellingProducts, key = { it.productId }) { product ->
                val rank = uiState.topSellingProducts.indexOf(product) + 1
                TopSellingProductRow(rank = rank, product = product, currencySymbol = symbol)
            }
        }

        item {
            Text(text = "Inventory valuation", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
        item {
            val valuation = uiState.inventoryValuation
            GlassCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ValuationRow(label = "Total cost value", value = "$symbol%.2f".format(valuation?.totalCostValue ?: 0.0))
                    Spacer(modifier = Modifier.height(GlassSpacing.gap8))
                    ValuationRow(label = "Total retail value", value = "$symbol%.2f".format(valuation?.totalRetailValue ?: 0.0))
                    Spacer(modifier = Modifier.height(GlassSpacing.gap8))
                    ValuationRow(label = "Total units", value = "%.2f".format(valuation?.totalUnits ?: 0.0))
                    Spacer(modifier = Modifier.height(GlassSpacing.gap8))
                    ValuationRow(label = "Distinct products", value = "${valuation?.distinctProducts ?: 0}")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun SalesTrendChart(points: List<DailySalesPoint>, currencySymbol: String) {
    val colors = LocalGlassColors.current
    val maxRevenue = points.maxOf { it.revenue }.coerceAtLeast(0.01)
    val barColor = GlassPalette.AccentPrimary

    GlassCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Peak: $currencySymbol%.2f".format(maxRevenue),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(GlassSpacing.gap8))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val barCount = points.size
                val gap = 6.dp.toPx()
                val barWidth = (size.width - gap * (barCount - 1)) / barCount
                points.forEachIndexed { index, point ->
                    val ratio = (point.revenue / maxRevenue).toFloat().coerceIn(0f, 1f)
                    val barHeight = size.height * ratio
                    val left = index * (barWidth + gap)
                    drawRect(
                        color = barColor,
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
            Spacer(modifier = Modifier.height(GlassSpacing.gap8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                points.forEach { point ->
                    Text(
                        text = point.dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopSellingProductRow(rank: Int, product: TopSellingProduct, currencySymbol: String) {
    val colors = LocalGlassColors.current
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleMedium,
                    color = GlassPalette.AccentPrimary
                )
                Spacer(modifier = Modifier.width(GlassSpacing.gap12))
                Column {
                    Text(text = product.productName, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    Text(
                        text = "${product.quantitySold} sold",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currencySymbol%.2f".format(product.revenue),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = "+$currencySymbol%.2f".format(product.profit),
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassPalette.AccentSuccess
                )
            }
        }
    }
}

@Composable
private fun ValuationRow(label: String, value: String) {
    val colors = LocalGlassColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
    }
}

private fun writeCsvAndGetUri(context: Context, csv: String): Uri {
    val exportDir = File(context.cacheDir, "receipts").apply { mkdirs() }
    val file = File(exportDir, "report_${System.currentTimeMillis()}.csv")
    FileWriter(file).use { writer -> writer.write(csv) }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun shareCsv(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share report"))
}
