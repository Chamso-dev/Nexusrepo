package com.nexus.grocerypos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nexus.grocerypos.data.local.entity.PurchaseOrderEntity
import com.nexus.grocerypos.data.local.entity.PurchaseOrderLineItemEntity
import com.nexus.grocerypos.data.local.entity.PurchaseOrderWithItems
import com.nexus.grocerypos.domain.model.PurchaseOrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseOrderDao {

    @Transaction
    @Query("SELECT * FROM purchase_orders WHERE (:status IS NULL OR status = :status) ORDER BY createdAt DESC")
    fun observeOrders(status: PurchaseOrderStatus?): Flow<List<PurchaseOrderWithItems>>

    @Transaction
    @Query("SELECT * FROM purchase_orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PurchaseOrderWithItems?

    @Insert
    suspend fun insertOrder(order: PurchaseOrderEntity): Long

    @Update
    suspend fun updateOrder(order: PurchaseOrderEntity)

    @Insert
    suspend fun insertLineItems(items: List<PurchaseOrderLineItemEntity>)

    @Update
    suspend fun updateLineItem(item: PurchaseOrderLineItemEntity)

    @Query("DELETE FROM purchase_order_line_items WHERE purchaseOrderId = :orderId")
    suspend fun deleteLineItemsForOrder(orderId: Long)

    @Query("DELETE FROM purchase_orders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM purchase_orders")
    suspend fun count(): Int
}
