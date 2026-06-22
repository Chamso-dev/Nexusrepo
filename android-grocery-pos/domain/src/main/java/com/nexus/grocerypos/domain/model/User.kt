package com.nexus.grocerypos.domain.model

enum class UserRole {
    OWNER, MANAGER, CASHIER
}

data class User(
    val id: Long = 0,
    val fullName: String,
    val username: String,
    val passwordHash: String,
    val pinHash: String? = null,
    val role: UserRole = UserRole.CASHIER,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Session(
    val userId: Long,
    val fullName: String,
    val role: UserRole,
    val loginAt: Long,
    val lastActiveAt: Long
)
