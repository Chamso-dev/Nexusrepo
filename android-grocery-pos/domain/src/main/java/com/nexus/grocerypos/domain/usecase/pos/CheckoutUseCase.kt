package com.nexus.grocerypos.domain.usecase.pos

import com.nexus.grocerypos.domain.model.Sale
import com.nexus.grocerypos.domain.model.SaleLineItem
import com.nexus.grocerypos.domain.model.SalePayment
import com.nexus.grocerypos.domain.repository.SalesRepository
import com.nexus.grocerypos.domain.util.Result
import javax.inject.Inject

class CheckoutUseCase @Inject constructor(
    private val salesRepository: SalesRepository,
    private val calculateCartTotals: CalculateCartTotalsUseCase
) {
    suspend operator fun invoke(
        lines: List<CartLine>,
        payments: List<SalePayment>,
        cashierId: Long,
        customerId: Long?,
        taxRatePercent: Double,
        taxInclusive: Boolean,
        orderDiscountPercent: Double = 0.0
    ): Result<Long> {
        if (lines.isEmpty()) return Result.Error("Cart is empty")

        val overSold = lines.firstOrNull { it.quantity > it.availableStock }
        if (overSold != null) {
            return Result.Error("Not enough stock for ${overSold.productName}")
        }

        val totals = calculateCartTotals(lines, taxRatePercent, taxInclusive, orderDiscountPercent)
        val amountPaid = payments.sumOf { it.amount }
        if (amountPaid < totals.grandTotal - 0.01) {
            return Result.Error("Payment amount is less than the total due")
        }

        val saleItems = lines.map {
            SaleLineItem(
                productId = it.productId,
                productName = it.productName,
                unitPrice = it.unitPrice,
                unitCost = it.unitCost,
                quantity = it.quantity,
                discountType = it.discountType,
                discountValue = it.discountValue
            )
        }

        val receiptNumber = salesRepository.nextReceiptNumber()
        val sale = Sale(
            receiptNumber = receiptNumber,
            customerId = customerId,
            cashierId = cashierId,
            items = saleItems,
            payments = payments,
            subtotal = totals.subtotal,
            discountTotal = totals.discountTotal,
            taxTotal = totals.taxTotal,
            grandTotal = totals.grandTotal,
            totalCost = totals.totalCost
        )

        return salesRepository.recordSale(sale)
    }
}
