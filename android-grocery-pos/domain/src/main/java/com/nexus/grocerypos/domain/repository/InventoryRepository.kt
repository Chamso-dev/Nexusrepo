package com.nexus.grocerypos.domain.repository

import com.nexus.grocerypos.domain.model.InventoryTransaction
import com.nexus.grocerypos.domain.model.InventoryTransactionType
import com.nexus.grocerypos.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun observeHistory(productId: Long? = null): Flow<List<InventoryTransaction>>

    suspend fun adjustStock(
        productId: Long,
        type: InventoryTransactionType,
        quantityDelta: Double,
        reason: String?,
        referenceId: Long?,
        actorUserId: Long?
    ): Result<Unit>
}
