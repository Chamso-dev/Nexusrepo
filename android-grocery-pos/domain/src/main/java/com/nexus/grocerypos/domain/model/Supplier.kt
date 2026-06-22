package com.nexus.grocerypos.domain.model

data class Supplier(
    val id: Long = 0,
    val name: String,
    val contactPerson: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val balanceOwed: Double = 0.0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
