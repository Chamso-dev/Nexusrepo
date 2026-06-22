package com.nexus.grocerypos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nexus.grocerypos.domain.model.StockUnit

@Entity(
    tableName = "products",
    indices = [Index("sku", unique = true), Index("barcode"), Index("categoryId"), Index("brandId")],
    foreignKeys = [
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = BrandEntity::class, parentColumns = ["id"], childColumns = ["brandId"], onDelete = ForeignKey.SET_NULL)
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String,
    val barcode: String?,
    val categoryId: Long?,
    val brandId: Long?,
    val costPrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Double,
    val lowStockThreshold: Double,
    val unit: StockUnit,
    val imageUri: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class ProductWithDetailsRow(
    val id: Long,
    val name: String,
    val sku: String,
    val barcode: String?,
    val categoryId: Long?,
    val brandId: Long?,
    val costPrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Double,
    val lowStockThreshold: Double,
    val unit: StockUnit,
    val imageUri: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val categoryName: String?,
    val brandName: String?
)
