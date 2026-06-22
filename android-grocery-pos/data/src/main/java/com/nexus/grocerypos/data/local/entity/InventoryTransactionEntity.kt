package com.nexus.grocerypos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nexus.grocerypos.domain.model.InventoryTransactionType

@Entity(
    tableName = "inventory_transactions",
    indices = [Index("productId"), Index("createdAt")]
)
data class InventoryTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: InventoryTransactionType,
    val quantityDelta: Double,
    val resultingQuantity: Double,
    val reason: String?,
    val referenceId: Long?,
    val actorUserId: Long?,
    val createdAt: Long
)
