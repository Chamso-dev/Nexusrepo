package com.nexus.grocerypos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nexus.grocerypos.data.local.entity.InventoryTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query(
        """
        SELECT * FROM inventory_transactions
        WHERE :productId IS NULL OR productId = :productId
        ORDER BY createdAt DESC
        """
    )
    fun observeHistory(productId: Long?): Flow<List<InventoryTransactionEntity>>

    @Insert
    suspend fun insert(transaction: InventoryTransactionEntity): Long
}
