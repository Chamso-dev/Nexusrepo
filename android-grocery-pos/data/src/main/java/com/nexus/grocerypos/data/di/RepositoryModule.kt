package com.nexus.grocerypos.data.di

import com.nexus.grocerypos.data.repository.CustomerRepositoryImpl
import com.nexus.grocerypos.data.repository.InventoryRepositoryImpl
import com.nexus.grocerypos.data.repository.ProductRepositoryImpl
import com.nexus.grocerypos.data.repository.PurchaseRepositoryImpl
import com.nexus.grocerypos.data.repository.ReportRepositoryImpl
import com.nexus.grocerypos.data.repository.SalesRepositoryImpl
import com.nexus.grocerypos.data.repository.SupplierRepositoryImpl
import com.nexus.grocerypos.data.repository.UserRepositoryImpl
import com.nexus.grocerypos.data.settings.SettingsRepositoryImpl
import com.nexus.grocerypos.domain.repository.CustomerRepository
import com.nexus.grocerypos.domain.repository.InventoryRepository
import com.nexus.grocerypos.domain.repository.ProductRepository
import com.nexus.grocerypos.domain.repository.PurchaseRepository
import com.nexus.grocerypos.domain.repository.ReportRepository
import com.nexus.grocerypos.domain.repository.SalesRepository
import com.nexus.grocerypos.domain.repository.SettingsRepository
import com.nexus.grocerypos.domain.repository.SupplierRepository
import com.nexus.grocerypos.domain.repository.UserRepository
import com.nexus.grocerypos.domain.util.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
    @Binds @Singleton abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository
    @Binds @Singleton abstract fun bindSupplierRepository(impl: SupplierRepositoryImpl): SupplierRepository
    @Binds @Singleton abstract fun bindSalesRepository(impl: SalesRepositoryImpl): SalesRepository
    @Binds @Singleton abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository
    @Binds @Singleton abstract fun bindPurchaseRepository(impl: PurchaseRepositoryImpl): PurchaseRepository
    @Binds @Singleton abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds @Singleton abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository
    @Binds @Singleton abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}
