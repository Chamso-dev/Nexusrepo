package com.nexus.grocerypos.data.repository

import com.nexus.grocerypos.data.local.dao.SupplierDao
import com.nexus.grocerypos.data.local.db.toDomain
import com.nexus.grocerypos.data.local.db.toEntity
import com.nexus.grocerypos.domain.model.Supplier
import com.nexus.grocerypos.domain.repository.SupplierRepository
import com.nexus.grocerypos.domain.util.Result
import com.nexus.grocerypos.domain.util.resultOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SupplierRepositoryImpl @Inject constructor(
    private val supplierDao: SupplierDao
) : SupplierRepository {

    override fun observeSuppliers(query: String) =
        supplierDao.observeAll(query.trim()).map { list -> list.map { it.toDomain() } }

    override suspend fun getSupplierById(id: Long) = supplierDao.getById(id)?.toDomain()

    override suspend fun upsertSupplier(supplier: Supplier): Result<Long> = resultOf {
        require(supplier.name.isNotBlank()) { "Supplier name is required" }
        supplierDao.upsert(supplier.toEntity())
    }

    override suspend fun deleteSupplier(id: Long): Result<Unit> = resultOf {
        supplierDao.deleteById(id)
    }

    override suspend fun adjustBalanceOwed(supplierId: Long, delta: Double): Result<Unit> = resultOf {
        supplierDao.adjustBalanceOwed(supplierId, delta)
    }
}
