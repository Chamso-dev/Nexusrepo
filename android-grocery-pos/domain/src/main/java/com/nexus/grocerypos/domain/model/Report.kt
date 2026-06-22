package com.nexus.grocerypos.domain.model

data class SalesSummary(
    val periodLabel: String,
    val revenue: Double,
    val cost: Double,
    val profit: Double,
    val transactionCount: Int,
    val itemsSold: Double
)

data class DailySalesPoint(
    val dateLabel: String,
    val revenue: Double,
    val profit: Double
)

data class TopSellingProduct(
    val productId: Long,
    val productName: String,
    val quantitySold: Double,
    val revenue: Double,
    val profit: Double
)

data class InventoryValuation(
    val totalCostValue: Double,
    val totalRetailValue: Double,
    val totalUnits: Double,
    val distinctProducts: Int
)
