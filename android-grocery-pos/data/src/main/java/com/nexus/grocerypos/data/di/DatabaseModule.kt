package com.nexus.grocerypos.data.di

import android.content.Context
import androidx.room.Room
import com.nexus.grocerypos.data.local.dao.BrandDao
import com.nexus.grocerypos.data.local.dao.CategoryDao
import com.nexus.grocerypos.data.local.dao.CustomerDao
import com.nexus.grocerypos.data.local.dao.InventoryDao
import com.nexus.grocerypos.data.local.dao.ProductDao
import com.nexus.grocerypos.data.local.dao.PurchaseOrderDao
import com.nexus.grocerypos.data.local.dao.SaleDao
import com.nexus.grocerypos.data.local.dao.SupplierDao
import com.nexus.grocerypos.data.local.dao.UserDao
import com.nexus.grocerypos.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideBrandDao(db: AppDatabase): BrandDao = db.brandDao()
    @Provides fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()
    @Provides fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()
    @Provides fun provideSupplierDao(db: AppDatabase): SupplierDao = db.supplierDao()
    @Provides fun provideSaleDao(db: AppDatabase): SaleDao = db.saleDao()
    @Provides fun provideInventoryDao(db: AppDatabase): InventoryDao = db.inventoryDao()
    @Provides fun providePurchaseOrderDao(db: AppDatabase): PurchaseOrderDao = db.purchaseOrderDao()
}
