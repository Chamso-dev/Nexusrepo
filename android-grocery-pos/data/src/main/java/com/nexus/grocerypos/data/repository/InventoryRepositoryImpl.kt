package com.nexus.grocerypos.data.repository

import androidx.room.withTransaction
import com.nexus.grocerypos.data.local.dao.InventoryDao
import com.nexus.grocerypos.data.local.dao.ProductDao
import com.nexus.grocerypos.data.local.db.AppDatabase
import com.nexus.grocerypos.data.local.db.toDomain
import com.nexus.grocerypos.data.local.entity.InventoryTransactionEntity
import com.nexus.grocerypos.domain.model.InventoryTransactionType
import com.nexus.grocerypos.domain.repository.InventoryRepository
import com.nexus.grocerypos.domain.util.Result
import com.nexus.grocerypos.domain.util.resultOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val inventoryDao: InventoryDao,
    private val productDao: ProductDao
) : InventoryRepository {

    override fun observeHistory(productId: Long?) =
        inventoryDao.observeHistory(productId).map { list -> list.map { it.toDomain() } }

    override suspend fun adjustStock(
        productId: Long,
        type: InventoryTransactionType,
        quantityDelta: Double,
        reason: String?,
        referenceId: Long?,
        actorUserId: Long?
    ): Result<Unit> = resultOf {
        database.withTransaction {
            val product = productDao.getById(productId) ?: error("Product not found")
            val now = System.currentTimeMillis()
            productDao.adjustStock(productId, quantityDelta, now)
            inventoryDao.insert(
                InventoryTransactionEntity(
                    productId = productId,
                    productName = product.name,
                    type = type,
                    quantityDelta = quantityDelta,
                    resultingQuantity = product.stockQuantity + quantityDelta,
                    reason = reason,
                    referenceId = referenceId,
                    actorUserId = actorUserId,
                    createdAt = now
                )
            )
        }
    }
}
