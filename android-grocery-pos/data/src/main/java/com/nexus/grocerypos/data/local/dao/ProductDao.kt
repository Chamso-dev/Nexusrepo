package com.nexus.grocerypos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nexus.grocerypos.data.local.entity.ProductEntity
import com.nexus.grocerypos.data.local.entity.ProductWithDetailsRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query(
        """
        SELECT p.*, c.name AS categoryName, b.name AS brandName
        FROM products p
        LEFT JOIN categories c ON c.id = p.categoryId
        LEFT JOIN brands b ON b.id = p.brandId
        WHERE p.isActive = 1
          AND (:query = '' OR p.name LIKE '%' || :query || '%' OR p.sku LIKE '%' || :query || '%' OR p.barcode LIKE '%' || :query || '%')
          AND (:categoryId IS NULL OR p.categoryId = :categoryId)
        ORDER BY p.name ASC
        """
    )
    fun observeWithDetails(query: String, categoryId: Long?): Flow<List<ProductWithDetailsRow>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND stockQuantity <= lowStockThreshold ORDER BY stockQuantity ASC")
    fun observeLowStock(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("UPDATE products SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("UPDATE products SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :delta, updatedAt = :now WHERE id = :id")
    suspend fun adjustStock(id: Long, delta: Double, now: Long)

    @Query("SELECT SUM(costPrice * stockQuantity) FROM products WHERE isActive = 1")
    suspend fun totalCostValue(): Double?

    @Query("SELECT SUM(sellingPrice * stockQuantity) FROM products WHERE isActive = 1")
    suspend fun totalRetailValue(): Double?

    @Query("SELECT SUM(stockQuantity) FROM products WHERE isActive = 1")
    suspend fun totalUnits(): Double?

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    suspend fun distinctProductCount(): Int
}
