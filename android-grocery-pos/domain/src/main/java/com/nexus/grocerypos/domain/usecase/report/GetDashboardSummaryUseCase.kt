package com.nexus.grocerypos.domain.usecase.report

import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.model.SalesSummary
import com.nexus.grocerypos.domain.repository.ProductRepository
import com.nexus.grocerypos.domain.repository.ReportRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

data class DashboardSummary(
    val today: SalesSummary,
    val lowStockProducts: List<Product>
)

class GetDashboardSummaryUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): DashboardSummary {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24L * 60 * 60 * 1000 - 1

        val today = reportRepository.getSalesSummary(startOfDay, endOfDay, "Today")
        val lowStock = productRepository.observeLowStockProducts().first()

        return DashboardSummary(today = today, lowStockProducts = lowStock)
    }
}
