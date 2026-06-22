package com.nexus.grocerypos.domain.usecase.purchase

import com.nexus.grocerypos.domain.repository.PurchaseRepository
import com.nexus.grocerypos.domain.util.Result
import javax.inject.Inject

class ReceivePurchaseOrderUseCase @Inject constructor(private val purchaseRepository: PurchaseRepository) {
    suspend operator fun invoke(
        orderId: Long,
        receivedQuantities: Map<Long, Double>,
        actorUserId: Long?
    ): Result<Unit> {
        if (receivedQuantities.values.all { it <= 0 }) {
            return Result.Error("Enter a received quantity for at least one item")
        }
        return purchaseRepository.receivePurchaseOrder(orderId, receivedQuantities, actorUserId)
    }
}
