package com.nexus.grocerypos.domain.repository

import com.nexus.grocerypos.domain.model.Supplier
import com.nexus.grocerypos.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {
    fun observeSuppliers(query: String = ""): Flow<List<Supplier>>
    suspend fun getSupplierById(id: Long): Supplier?
    suspend fun upsertSupplier(supplier: Supplier): Result<Long>
    suspend fun deleteSupplier(id: Long): Result<Unit>
    suspend fun adjustBalanceOwed(supplierId: Long, delta: Double): Result<Unit>
}
