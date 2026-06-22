package com.nexus.grocerypos.data.repository

import com.nexus.grocerypos.data.local.dao.ProductDao
import com.nexus.grocerypos.data.local.dao.SaleDao
import com.nexus.grocerypos.domain.model.DailySalesPoint
import com.nexus.grocerypos.domain.model.InventoryValuation
import com.nexus.grocerypos.domain.model.SalesSummary
import com.nexus.grocerypos.domain.model.TopSellingProduct
import com.nexus.grocerypos.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val saleDao: SaleDao,
    private val productDao: ProductDao
) : ReportRepository {

    override suspend fun getSalesSummary(fromMillis: Long, toMillis: Long, periodLabel: String): SalesSummary {
        val row = saleDao.getSummaryRow(fromMillis, toMillis)
        val itemsSold = saleDao.getItemsSold(fromMillis, toMillis)
        return SalesSummary(
            periodLabel = periodLabel,
            revenue = row.revenue,
            cost = row.cost,
            profit = row.revenue - row.cost,
            transactionCount = row.transactionCount,
            itemsSold = itemsSold
        )
    }

    override suspend fun getDailySalesTrend(fromMillis: Long, toMillis: Long): List<DailySalesPoint> =
        saleDao.getDailyTrend(fromMillis, toMillis).map { DailySalesPoint(it.dateLabel, it.revenue, it.profit) }

    override suspend fun getTopSellingProducts(fromMillis: Long, toMillis: Long, limit: Int): List<TopSellingProduct> =
        saleDao.getTopSellingProducts(fromMillis, toMillis, limit).map {
            TopSellingProduct(it.productId, it.productName, it.quantitySold, it.revenue, it.profit)
        }

    override suspend fun getInventoryValuation(): InventoryValuation = InventoryValuation(
        totalCostValue = productDao.totalCostValue() ?: 0.0,
        totalRetailValue = productDao.totalRetailValue() ?: 0.0,
        totalUnits = productDao.totalUnits() ?: 0.0,
        distinctProducts = productDao.distinctProductCount()
    )
}
