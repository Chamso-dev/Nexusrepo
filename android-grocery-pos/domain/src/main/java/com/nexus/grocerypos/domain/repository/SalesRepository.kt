package com.nexus.grocerypos.domain.repository

import com.nexus.grocerypos.domain.model.Sale
import com.nexus.grocerypos.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface SalesRepository {
    fun observeSales(fromMillis: Long? = null, toMillis: Long? = null): Flow<List<Sale>>
    suspend fun getSaleById(id: Long): Sale?
    suspend fun getSalesForCustomer(customerId: Long): List<Sale>

    /** Persists the sale, decrements stock for every line item, and writes inventory transactions atomically. */
    suspend fun recordSale(sale: Sale): Result<Long>
    suspend fun voidSale(id: Long): Result<Unit>
    suspend fun nextReceiptNumber(): String
}
