package com.nexus.grocerypos.domain.repository

import com.nexus.grocerypos.domain.model.DailySalesPoint
import com.nexus.grocerypos.domain.model.InventoryValuation
import com.nexus.grocerypos.domain.model.SalesSummary
import com.nexus.grocerypos.domain.model.TopSellingProduct

interface ReportRepository {
    suspend fun getSalesSummary(fromMillis: Long, toMillis: Long, periodLabel: String): SalesSummary
    suspend fun getDailySalesTrend(fromMillis: Long, toMillis: Long): List<DailySalesPoint>
    suspend fun getTopSellingProducts(fromMillis: Long, toMillis: Long, limit: Int = 10): List<TopSellingProduct>
    suspend fun getInventoryValuation(): InventoryValuation
}
