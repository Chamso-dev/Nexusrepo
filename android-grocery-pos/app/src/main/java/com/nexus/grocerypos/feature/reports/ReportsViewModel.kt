package com.nexus.grocerypos.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.BusinessSettings
import com.nexus.grocerypos.domain.model.DailySalesPoint
import com.nexus.grocerypos.domain.model.InventoryValuation
import com.nexus.grocerypos.domain.model.SalesSummary
import com.nexus.grocerypos.domain.model.TopSellingProduct
import com.nexus.grocerypos.domain.repository.ReportRepository
import com.nexus.grocerypos.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Period { TODAY, WEEK, MONTH }

data class ReportsUiState(
    val isLoading: Boolean = true,
    val period: Period = Period.TODAY,
    val summary: SalesSummary? = null,
    val dailyTrend: List<DailySalesPoint> = emptyList(),
    val topSellingProducts: List<TopSellingProduct> = emptyList(),
    val inventoryValuation: InventoryValuation? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    val settings: StateFlow<BusinessSettings> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BusinessSettings())

    init {
        loadInventoryValuation()
        loadPeriodData(Period.TODAY)
    }

    fun selectPeriod(period: Period) {
        if (period == _uiState.value.period && !_uiState.value.isLoading) return
        loadPeriodData(period)
    }

    fun refresh() {
        loadInventoryValuation()
        loadPeriodData(_uiState.value.period)
    }

    private fun loadPeriodData(period: Period) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, period = period)
            val (fromMillis, toMillis, label) = periodRange(period)
            val summary = reportRepository.getSalesSummary(fromMillis, toMillis, label)
            val trend = reportRepository.getDailySalesTrend(fromMillis, toMillis)
            val topSellers = reportRepository.getTopSellingProducts(fromMillis, toMillis)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                period = period,
                summary = summary,
                dailyTrend = trend,
                topSellingProducts = topSellers
            )
        }
    }

    private fun loadInventoryValuation() {
        viewModelScope.launch {
            val valuation = reportRepository.getInventoryValuation()
            _uiState.value = _uiState.value.copy(inventoryValuation = valuation)
        }
    }

    private fun periodRange(period: Period): Triple<Long, Long, String> {
        val now = Calendar.getInstance()
        val toMillis = now.timeInMillis
        val startOfToday = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return when (period) {
            Period.TODAY -> Triple(startOfToday.timeInMillis, toMillis, "Today")
            Period.WEEK -> {
                val weekStart = (startOfToday.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6) }
                Triple(weekStart.timeInMillis, toMillis, "This week")
            }
            Period.MONTH -> {
                val monthStart = (startOfToday.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                Triple(monthStart.timeInMillis, toMillis, "This month")
            }
        }
    }

    fun buildCsvReport(): String {
        val state = _uiState.value
        val symbol = settings.value.currencySymbol
        val builder = StringBuilder()

        builder.append("Report period,${state.summary?.periodLabel.orEmpty()}\n")
        builder.append("\n")
        builder.append("Metric,Value\n")
        builder.append("Revenue,$symbol%.2f\n".format(state.summary?.revenue ?: 0.0))
        builder.append("Cost,$symbol%.2f\n".format(state.summary?.cost ?: 0.0))
        builder.append("Profit,$symbol%.2f\n".format(state.summary?.profit ?: 0.0))
        builder.append("Transactions,${state.summary?.transactionCount ?: 0}\n")
        builder.append("Items sold,${state.summary?.itemsSold ?: 0.0}\n")
        builder.append("\n")
        builder.append("Rank,Product,Quantity Sold,Revenue,Profit\n")
        state.topSellingProducts.forEachIndexed { index, product ->
            val rank = index + 1
            val name = product.productName.replace(",", " ")
            val revenueText = "$symbol%.2f".format(product.revenue)
            val profitText = "$symbol%.2f".format(product.profit)
            builder.append("$rank,$name,${product.quantitySold},$revenueText,$profitText\n")
        }

        return builder.toString()
    }
}
