package com.nexus.grocerypos.domain.usecase.pos

import javax.inject.Inject

class CalculateCartTotalsUseCase @Inject constructor() {
    operator fun invoke(
        lines: List<CartLine>,
        taxRatePercent: Double,
        taxInclusive: Boolean,
        orderDiscountPercent: Double = 0.0
    ): CartTotals {
        val subtotal = lines.sumOf { it.grossTotal }
        val lineDiscounts = lines.sumOf { it.discountAmount }
        val afterLineDiscounts = (subtotal - lineDiscounts).coerceAtLeast(0.0)
        val orderDiscount = afterLineDiscounts * (orderDiscountPercent / 100.0)
        val taxableAmount = (afterLineDiscounts - orderDiscount).coerceAtLeast(0.0)
        val totalCost = lines.sumOf { it.unitCost * it.quantity }

        val tax: Double
        val grandTotal: Double
        if (taxInclusive) {
            // Price already contains tax; back it out for reporting but keep the charged total unchanged.
            tax = taxableAmount - (taxableAmount / (1 + taxRatePercent / 100.0))
            grandTotal = taxableAmount
        } else {
            tax = taxableAmount * (taxRatePercent / 100.0)
            grandTotal = taxableAmount + tax
        }

        return CartTotals(
            subtotal = subtotal,
            discountTotal = lineDiscounts + orderDiscount,
            taxableAmount = taxableAmount,
            taxTotal = tax,
            grandTotal = grandTotal,
            totalCost = totalCost
        )
    }
}
