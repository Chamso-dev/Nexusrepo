package com.nexus.grocerypos.domain.usecase.pos

import com.nexus.grocerypos.domain.model.DiscountType

data class CartLine(
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val unitCost: Double,
    val quantity: Double,
    val availableStock: Double,
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
}

data class CartTotals(
    val subtotal: Double,
    val discountTotal: Double,
    val taxableAmount: Double,
    val taxTotal: Double,
    val grandTotal: Double,
    val totalCost: Double
) {
    val profit: Double get() = (grandTotal - taxTotal) - totalCost
}
