package com.nexus.grocerypos.domain.repository

import com.nexus.grocerypos.domain.model.Customer
import com.nexus.grocerypos.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun observeCustomers(query: String = ""): Flow<List<Customer>>
    suspend fun getCustomerById(id: Long): Customer?
    suspend fun upsertCustomer(customer: Customer): Result<Long>
    suspend fun deleteCustomer(id: Long): Result<Unit>
    suspend fun adjustBalance(customerId: Long, delta: Double): Result<Unit>
}
