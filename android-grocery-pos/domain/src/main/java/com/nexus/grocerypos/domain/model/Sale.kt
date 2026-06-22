package com.nexus.grocerypos.domain.model

enum class PaymentMethod {
    CASH, CARD, MOBILE_MONEY, STORE_CREDIT, SPLIT
}

enum class DiscountType {
    NONE, PERCENT, FIXED
}

data class SaleLineItem(
    val id: Long = 0,
    val saleId: Long = 0,
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val unitCost: Double,
    val quantity: Double,
    val discountType: DiscountType = DiscountType.NONE,
    val discountValue: Double = 0.0
) {
    val grossTotal: Double get() = unitPrice * quantity
    val discountAmount: Double get() = when (discountType) {
        DiscountType.NONE -> 0.0
        DiscountType.PERCENT -> grossTotal * (discountValue / 100.0)
        DiscountType.FIXED -> discountValue
    }
    val netTotal: Double get() = (grossTotal - discountAmount).coerceAtLeast(0.0)
    val totalCost: Double get() = unitCost * quantity
    val profit: Double get() = netTotal - totalCost
}

data class SalePayment(
    val id: Long = 0,
    val saleId: Long = 0,
    val method: PaymentMethod,
    val amount: Double,
    val tenderedAmount: Double = amount,
    val changeDue: Double = 0.0
)

data class Sale(
    val id: Long = 0,
    val receiptNumber: String,
    val customerId: Long? = null,
    val cashierId: Long,
    val items: List<SaleLineItem> = emptyList(),
    val payments: List<SalePayment> = emptyList(),
    val subtotal: Double,
    val discountTotal: Double = 0.0,
    val taxTotal: Double = 0.0,
    val grandTotal: Double,
    val totalCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val isVoided: Boolean = false
) {
    val profit: Double get() = grandTotal - totalCost
}
