package com.nexus.grocerypos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val balance: Double,
    val notes: String?,
    val createdAt: Long
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contactPerson: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val balanceOwed: Double,
    val notes: String?,
    val createdAt: Long
)
