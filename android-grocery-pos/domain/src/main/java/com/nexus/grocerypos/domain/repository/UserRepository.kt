package com.nexus.grocerypos.domain.repository

import com.nexus.grocerypos.domain.model.Session
import com.nexus.grocerypos.domain.model.User
import com.nexus.grocerypos.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUsers(): Flow<List<User>>
    suspend fun getUserById(id: Long): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun createUser(user: User, rawPassword: String, rawPin: String? = null): Result<Long>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(id: Long): Result<Unit>
    suspend fun authenticate(username: String, password: String): Result<User>
    suspend fun authenticateWithPin(userId: Long, pin: String): Result<User>

    fun observeSession(): Flow<Session?>
    suspend fun startSession(user: User)
    suspend fun touchSession()
    suspend fun endSession()
}
