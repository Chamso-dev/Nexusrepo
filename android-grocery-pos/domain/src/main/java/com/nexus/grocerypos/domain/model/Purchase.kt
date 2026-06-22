package com.nexus.grocerypos.domain.model

enum class PurchaseOrderStatus {
    DRAFT, ORDERED, PARTIALLY_RECEIVED, RECEIVED, CANCELLED
}

data class PurchaseOrderLineItem(
    val id: Long = 0,
    val purchaseOrderId: Long = 0,
    val productId: Long,
    val productName: String,
    val quantityOrdered: Double,
    val quantityReceived: Double = 0.0,
    val unitCost: Double
) {
    val lineTotal: Double get() = quantityOrdered * unitCost
}

data class PurchaseOrder(
    val id: Long = 0,
    val orderNumber: String,
    val supplierId: Long,
    val supplierName: String,
    val status: PurchaseOrderStatus = PurchaseOrderStatus.DRAFT,
    val items: List<PurchaseOrderLineItem> = emptyList(),
    val invoiceNumber: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val receivedAt: Long? = null
) {
    val totalAmount: Double get() = items.sumOf { it.lineTotal }
}
