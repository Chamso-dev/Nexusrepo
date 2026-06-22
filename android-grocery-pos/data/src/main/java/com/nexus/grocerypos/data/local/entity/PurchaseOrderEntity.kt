package com.nexus.grocerypos.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.nexus.grocerypos.domain.model.PurchaseOrderStatus

@Entity(
    tableName = "purchase_orders",
    indices = [Index("orderNumber", unique = true), Index("supplierId")]
)
data class PurchaseOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val supplierId: Long,
    val supplierName: String,
    val status: PurchaseOrderStatus,
    val invoiceNumber: String?,
    val notes: String?,
    val createdAt: Long,
    val receivedAt: Long?
)

@Entity(
    tableName = "purchase_order_line_items",
    indices = [Index("purchaseOrderId"), Index("productId")],
    foreignKeys = [
        ForeignKey(entity = PurchaseOrderEntity::class, parentColumns = ["id"], childColumns = ["purchaseOrderId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PurchaseOrderLineItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseOrderId: Long,
    val productId: Long,
    val productName: String,
    val quantityOrdered: Double,
    val quantityReceived: Double,
    val unitCost: Double
)

data class PurchaseOrderWithItems(
    @Embedded val order: PurchaseOrderEntity,
    @Relation(parentColumn = "id", entityColumn = "purchaseOrderId")
    val items: List<PurchaseOrderLineItemEntity>
)
