package com.nexus.grocerypos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nexus.grocerypos.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val username: String,
    val passwordHash: String,
    val pinHash: String?,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: Long
)
