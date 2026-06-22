package com.nexus.grocerypos.domain.model

enum class StockUnit {
    PCS, KG, G, L, ML, BOX, PACK
}

data class Product(
    val id: Long = 0,
    val name: String,
    val sku: String,
    val barcode: String? = null,
    val categoryId: Long? = null,
    val brandId: Long? = null,
    val costPrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Double,
    val lowStockThreshold: Double = 5.0,
    val unit: StockUnit = StockUnit.PCS,
    val imageUri: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val profitPerUnit: Double get() = sellingPrice - costPrice
    val marginPercent: Double get() = if (sellingPrice == 0.0) 0.0 else (profitPerUnit / sellingPrice) * 100
    val isLowStock: Boolean get() = stockQuantity <= lowStockThreshold
}

/** Product joined with its category/brand names for list display, avoids N+1 lookups in the UI. */
data class ProductWithDetails(
    val product: Product,
    val categoryName: String?,
    val brandName: String?
)
