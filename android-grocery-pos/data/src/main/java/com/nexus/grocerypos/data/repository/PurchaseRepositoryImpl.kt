package com.nexus.grocerypos.data.repository

import androidx.room.withTransaction
import com.nexus.grocerypos.data.local.dao.InventoryDao
import com.nexus.grocerypos.data.local.dao.ProductDao
import com.nexus.grocerypos.data.local.dao.PurchaseOrderDao
import com.nexus.grocerypos.data.local.db.AppDatabase
import com.nexus.grocerypos.data.local.db.toDomain
import com.nexus.grocerypos.data.local.db.toEntity
import com.nexus.grocerypos.data.local.entity.InventoryTransactionEntity
import com.nexus.grocerypos.data.local.entity.PurchaseOrderEntity
import com.nexus.grocerypos.domain.model.InventoryTransactionType
import com.nexus.grocerypos.domain.model.PurchaseOrder
import com.nexus.grocerypos.domain.model.PurchaseOrderStatus
import com.nexus.grocerypos.domain.repository.PurchaseRepository
import com.nexus.grocerypos.domain.util.Result
import com.nexus.grocerypos.domain.util.resultOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PurchaseRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val purchaseOrderDao: PurchaseOrderDao,
    private val productDao: ProductDao,
    private val inventoryDao: InventoryDao
) : PurchaseRepository {

    override fun observePurchaseOrders(status: PurchaseOrderStatus?) =
        purchaseOrderDao.observeOrders(status).map { list -> list.map { it.toDomain() } }

    override suspend fun getPurchaseOrderById(id: Long) = purchaseOrderDao.getById(id)?.toDomain()

    override suspend fun upsertPurchaseOrder(order: PurchaseOrder): Result<Long> = resultOf {
        require(order.items.isNotEmpty()) { "Add at least one line item" }
        database.withTransaction {
            val orderId = if (order.id == 0L) {
                purchaseOrderDao.insertOrder(
                    PurchaseOrderEntity(
                        orderNumber = order.orderNumber,
                        supplierId = order.supplierId,
                        supplierName = order.supplierName,
                        status = order.status,
                        invoiceNumber = order.invoiceNumber,
                        notes = order.notes,
                        createdAt = order.createdAt,
                        receivedAt = order.receivedAt
                    )
                )
            } else {
                purchaseOrderDao.updateOrder(
                    PurchaseOrderEntity(
                        id = order.id,
                        orderNumber = order.orderNumber,
                        supplierId = order.supplierId,
                        supplierName = order.supplierName,
                        status = order.status,
                        invoiceNumber = order.invoiceNumber,
                        notes = order.notes,
                        createdAt = order.createdAt,
                        receivedAt = order.receivedAt
                    )
                )
                purchaseOrderDao.deleteLineItemsForOrder(order.id)
                order.id
            }
            purchaseOrderDao.insertLineItems(order.items.map { it.toEntity(orderId) })
            orderId
        }
    }

    override suspend fun deletePurchaseOrder(id: Long): Result<Unit> = resultOf {
        purchaseOrderDao.deleteById(id)
    }

    override suspend fun nextOrderNumber(): String {
        val count = purchaseOrderDao.count() + 1
        return "PO-%05d".format(count)
    }

    override suspend fun receivePurchaseOrder(
        orderId: Long,
        receivedQuantities: Map<Long, Double>,
        actorUserId: Long?
    ): Result<Unit> = resultOf {
        database.withTransaction {
            val orderWithItems = purchaseOrderDao.getById(orderId) ?: error("Purchase order not found")
            val now = System.currentTimeMillis()

            orderWithItems.items.forEach { item ->
                val receivedQty = receivedQuantities[item.id] ?: 0.0
                if (receivedQty <= 0) return@forEach

                purchaseOrderDao.updateLineItem(item.copy(quantityReceived = item.quantityReceived + receivedQty))
                productDao.adjustStock(item.productId, receivedQty, now)
                val resulting = productDao.getById(item.productId)?.stockQuantity ?: 0.0

                inventoryDao.insert(
                    InventoryTransactionEntity(
                        productId = item.productId,
                        productName = item.productName,
                        type = InventoryTransactionType.PURCHASE_RECEIVE,
                        quantityDelta = receivedQty,
                        resultingQuantity = resulting,
                        reason = "Received PO #${orderWithItems.order.orderNumber}",
                        referenceId = orderId,
                        actorUserId = actorUserId,
                        createdAt = now
                    )
                )
            }

            val refreshed = purchaseOrderDao.getById(orderId)!!
            val fullyReceived = refreshed.items.all { it.quantityReceived >= it.quantityOrdered }
            val newStatus = if (fullyReceived) PurchaseOrderStatus.RECEIVED else PurchaseOrderStatus.PARTIALLY_RECEIVED
            purchaseOrderDao.updateOrder(
                refreshed.order.copy(
                    status = newStatus,
                    receivedAt = if (fullyReceived) now else refreshed.order.receivedAt
                )
            )
        }
    }
}
