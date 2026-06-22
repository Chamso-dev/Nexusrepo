package com.nexus.grocerypos.domain.repository

import com.nexus.grocerypos.domain.model.PurchaseOrder
import com.nexus.grocerypos.domain.model.PurchaseOrderStatus
import com.nexus.grocerypos.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    fun observePurchaseOrders(status: PurchaseOrderStatus? = null): Flow<List<PurchaseOrder>>
    suspend fun getPurchaseOrderById(id: Long): PurchaseOrder?
    suspend fun upsertPurchaseOrder(order: PurchaseOrder): Result<Long>
    suspend fun deletePurchaseOrder(id: Long): Result<Unit>
    suspend fun nextOrderNumber(): String

    /** Marks line items received, increments stock, and logs PURCHASE_RECEIVE inventory transactions. */
    suspend fun receivePurchaseOrder(orderId: Long, receivedQuantities: Map<Long, Double>, actorUserId: Long?): Result<Unit>
}
