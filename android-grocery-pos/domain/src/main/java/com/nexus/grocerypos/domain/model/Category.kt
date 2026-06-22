package com.nexus.grocerypos.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val colorHex: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Brand(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
