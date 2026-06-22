package com.nexus.grocerypos.data.repository

import com.nexus.grocerypos.data.local.dao.BrandDao
import com.nexus.grocerypos.data.local.dao.CategoryDao
import com.nexus.grocerypos.data.local.dao.ProductDao
import com.nexus.grocerypos.data.local.db.toDomain
import com.nexus.grocerypos.data.local.db.toEntity
import com.nexus.grocerypos.domain.model.Brand
import com.nexus.grocerypos.domain.model.Category
import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.repository.ProductRepository
import com.nexus.grocerypos.domain.util.Result
import com.nexus.grocerypos.domain.util.resultOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val brandDao: BrandDao
) : ProductRepository {

    override fun observeProducts(query: String, categoryId: Long?) =
        productDao.observeWithDetails(query.trim(), categoryId).map { rows -> rows.map { it.toDomain() } }

    override fun observeLowStockProducts() = productDao.observeLowStock().map { list -> list.map { it.toDomain() } }

    override suspend fun getProductById(id: Long) = productDao.getById(id)?.toDomain()

    override suspend fun getProductByBarcode(barcode: String) = productDao.getByBarcode(barcode)?.toDomain()

    override suspend fun upsertProduct(product: Product): Result<Long> = resultOf {
        require(product.name.isNotBlank()) { "Product name is required" }
        require(product.sku.isNotBlank()) { "SKU is required" }
        require(product.sellingPrice >= 0 && product.costPrice >= 0) { "Prices cannot be negative" }
        val entity = product.copy(updatedAt = System.currentTimeMillis()).toEntity()
        productDao.upsert(entity)
    }

    override suspend fun deleteProduct(id: Long): Result<Unit> = resultOf {
        productDao.softDelete(id)
    }

    override suspend fun setActive(id: Long, isActive: Boolean): Result<Unit> = resultOf {
        productDao.setActive(id, isActive)
    }

    override fun observeCategories() = categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsertCategory(category: Category): Result<Long> = resultOf {
        require(category.name.isNotBlank()) { "Category name is required" }
        categoryDao.upsert(category.toEntity())
    }

    override suspend fun deleteCategory(id: Long): Result<Unit> = resultOf {
        categoryDao.deleteById(id)
    }

    override fun observeBrands() = brandDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsertBrand(brand: Brand): Result<Long> = resultOf {
        require(brand.name.isNotBlank()) { "Brand name is required" }
        brandDao.upsert(brand.toEntity())
    }

    override suspend fun deleteBrand(id: Long): Result<Unit> = resultOf {
        brandDao.deleteById(id)
    }
}
