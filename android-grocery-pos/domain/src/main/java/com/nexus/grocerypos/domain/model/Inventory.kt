package com.nexus.grocerypos.domain.model

enum class InventoryTransactionType {
    STOCK_IN, STOCK_OUT, ADJUSTMENT, SALE, PURCHASE_RECEIVE, RETURN
}

data class InventoryTransaction(
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: InventoryTransactionType,
    val quantityDelta: Double,
    val resultingQuantity: Double,
    val reason: String? = null,
    val referenceId: Long? = null,
    val actorUserId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
