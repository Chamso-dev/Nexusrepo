package com.nexus.grocerypos.data.repository

import com.nexus.grocerypos.data.local.dao.CustomerDao
import com.nexus.grocerypos.data.local.db.toDomain
import com.nexus.grocerypos.data.local.db.toEntity
import com.nexus.grocerypos.domain.model.Customer
import com.nexus.grocerypos.domain.repository.CustomerRepository
import com.nexus.grocerypos.domain.util.Result
import com.nexus.grocerypos.domain.util.resultOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override fun observeCustomers(query: String) =
        customerDao.observeAll(query.trim()).map { list -> list.map { it.toDomain() } }

    override suspend fun getCustomerById(id: Long) = customerDao.getById(id)?.toDomain()

    override suspend fun upsertCustomer(customer: Customer): Result<Long> = resultOf {
        require(customer.name.isNotBlank()) { "Customer name is required" }
        customerDao.upsert(customer.toEntity())
    }

    override suspend fun deleteCustomer(id: Long): Result<Unit> = resultOf {
        customerDao.deleteById(id)
    }

    override suspend fun adjustBalance(customerId: Long, delta: Double): Result<Unit> = resultOf {
        customerDao.adjustBalance(customerId, delta)
    }
}
