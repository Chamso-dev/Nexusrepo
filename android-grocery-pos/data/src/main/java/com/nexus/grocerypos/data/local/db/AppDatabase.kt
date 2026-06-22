package com.nexus.grocerypos.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nexus.grocerypos.data.local.dao.BrandDao
import com.nexus.grocerypos.data.local.dao.CategoryDao
import com.nexus.grocerypos.data.local.dao.CustomerDao
import com.nexus.grocerypos.data.local.dao.InventoryDao
import com.nexus.grocerypos.data.local.dao.ProductDao
import com.nexus.grocerypos.data.local.dao.PurchaseOrderDao
import com.nexus.grocerypos.data.local.dao.SaleDao
import com.nexus.grocerypos.data.local.dao.SupplierDao
import com.nexus.grocerypos.data.local.dao.UserDao
import com.nexus.grocerypos.data.local.entity.BrandEntity
import com.nexus.grocerypos.data.local.entity.CategoryEntity
import com.nexus.grocerypos.data.local.entity.CustomerEntity
import com.nexus.grocerypos.data.local.entity.InventoryTransactionEntity
import com.nexus.grocerypos.data.local.entity.ProductEntity
import com.nexus.grocerypos.data.local.entity.PurchaseOrderEntity
import com.nexus.grocerypos.data.local.entity.PurchaseOrderLineItemEntity
import com.nexus.grocerypos.data.local.entity.SaleEntity
import com.nexus.grocerypos.data.local.entity.SaleLineItemEntity
import com.nexus.grocerypos.data.local.entity.SalePaymentEntity
import com.nexus.grocerypos.data.local.entity.SupplierEntity
import com.nexus.grocerypos.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        BrandEntity::class,
        ProductEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        SaleEntity::class,
        SaleLineItemEntity::class,
        SalePaymentEntity::class,
        InventoryTransactionEntity::class,
        PurchaseOrderEntity::class,
        PurchaseOrderLineItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun brandDao(): BrandDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao

    companion object {
        const val DATABASE_NAME = "grocery_pos.db"
    }
}
