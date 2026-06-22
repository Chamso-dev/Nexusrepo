package com.nexus.grocerypos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.grocerypos.data.local.entity.CustomerEntity
import com.nexus.grocerypos.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE (:query = '' OR name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name ASC")
    fun observeAll(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(customer: CustomerEntity): Long

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE customers SET balance = balance + :delta WHERE id = :id")
    suspend fun adjustBalance(id: Long, delta: Double)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers WHERE (:query = '' OR name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name ASC")
    fun observeAll(query: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(supplier: SupplierEntity): Long

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE suppliers SET balanceOwed = balanceOwed + :delta WHERE id = :id")
    suspend fun adjustBalanceOwed(id: Long, delta: Double)
}
