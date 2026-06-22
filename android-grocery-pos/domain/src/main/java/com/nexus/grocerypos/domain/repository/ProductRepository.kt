package com.nexus.grocerypos.domain.repository

import com.nexus.grocerypos.domain.model.Brand
import com.nexus.grocerypos.domain.model.Category
import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.model.ProductWithDetails
import com.nexus.grocerypos.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(query: String = "", categoryId: Long? = null): Flow<List<ProductWithDetails>>
    fun observeLowStockProducts(): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun upsertProduct(product: Product): Result<Long>
    suspend fun deleteProduct(id: Long): Result<Unit>
    suspend fun setActive(id: Long, isActive: Boolean): Result<Unit>

    fun observeCategories(): Flow<List<Category>>
    suspend fun upsertCategory(category: Category): Result<Long>
    suspend fun deleteCategory(id: Long): Result<Unit>

    fun observeBrands(): Flow<List<Brand>>
    suspend fun upsertBrand(brand: Brand): Result<Long>
    suspend fun deleteBrand(id: Long): Result<Unit>
}
