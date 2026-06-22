package com.nexus.grocerypos.domain.usecase.inventory

import com.nexus.grocerypos.domain.model.InventoryTransactionType
import com.nexus.grocerypos.domain.repository.InventoryRepository
import com.nexus.grocerypos.domain.repository.ProductRepository
import com.nexus.grocerypos.domain.util.Result
import javax.inject.Inject

class AdjustStockUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(
        productId: Long,
        type: InventoryTransactionType,
        quantityDelta: Double,
        reason: String?,
        actorUserId: Long?
    ): Result<Unit> {
        val product = productRepository.getProductById(productId)
            ?: return Result.Error("Product not found")

        val resulting = product.stockQuantity + quantityDelta
        if (resulting < 0) {
            return Result.Error("Resulting stock cannot be negative")
        }

        return inventoryRepository.adjustStock(
            productId = productId,
            type = type,
            quantityDelta = quantityDelta,
            reason = reason,
            referenceId = null,
            actorUserId = actorUserId
        )
    }
}
