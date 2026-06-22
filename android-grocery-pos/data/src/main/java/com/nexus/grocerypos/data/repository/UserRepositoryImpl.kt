package com.nexus.grocerypos.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nexus.grocerypos.data.local.dao.UserDao
import com.nexus.grocerypos.data.local.db.toDomain
import com.nexus.grocerypos.data.local.db.toEntity
import com.nexus.grocerypos.data.security.PasswordHasher
import com.nexus.grocerypos.data.settings.sessionDataStore
import com.nexus.grocerypos.domain.model.Session
import com.nexus.grocerypos.domain.model.User
import com.nexus.grocerypos.domain.model.UserRole
import com.nexus.grocerypos.domain.repository.UserRepository
import com.nexus.grocerypos.domain.util.Result
import com.nexus.grocerypos.domain.util.resultOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private object SessionKeys {
    val USER_ID = longPreferencesKey("session_user_id")
    val FULL_NAME = stringPreferencesKey("session_full_name")
    val ROLE = stringPreferencesKey("session_role")
    val LOGIN_AT = longPreferencesKey("session_login_at")
    val LAST_ACTIVE_AT = longPreferencesKey("session_last_active_at")
}

class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher
) : UserRepository {

    override fun observeUsers() = userDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getUserById(id: Long) = userDao.getById(id)?.toDomain()

    override suspend fun getUserByUsername(username: String) = userDao.getByUsername(username)?.toDomain()

    override suspend fun createUser(user: User, rawPassword: String, rawPin: String?): Result<Long> = resultOf {
        val existing = userDao.getByUsername(user.username)
        require(existing == null) { "Username already exists" }
        val hashed = user.copy(
            passwordHash = passwordHasher.hash(rawPassword),
            pinHash = rawPin?.let { passwordHasher.hash(it) }
        )
        userDao.insert(hashed.toEntity())
    }

    override suspend fun updateUser(user: User): Result<Unit> = resultOf {
        userDao.update(user.toEntity())
    }

    override suspend fun deleteUser(id: Long): Result<Unit> = resultOf {
        userDao.getById(id)?.let { userDao.delete(it) }
    }

    override suspend fun authenticate(username: String, password: String): Result<User> {
        val entity = userDao.getByUsername(username)
            ?: return Result.Error("Invalid username or password")
        if (!entity.isActive) return Result.Error("This account has been deactivated")
        if (!passwordHasher.verify(password, entity.passwordHash)) {
            return Result.Error("Invalid username or password")
        }
        return Result.Success(entity.toDomain())
    }

    override suspend fun authenticateWithPin(userId: Long, pin: String): Result<User> {
        val entity = userDao.getById(userId) ?: return Result.Error("User not found")
        val pinHash = entity.pinHash ?: return Result.Error("PIN not set for this user")
        if (!entity.isActive) return Result.Error("This account has been deactivated")
        if (!passwordHasher.verify(pin, pinHash)) return Result.Error("Incorrect PIN")
        return Result.Success(entity.toDomain())
    }

    override fun observeSession() = context.sessionDataStore.data.map { prefs ->
        val userId = prefs[SessionKeys.USER_ID] ?: return@map null
        Session(
            userId = userId,
            fullName = prefs[SessionKeys.FULL_NAME] ?: "",
            role = UserRole.valueOf(prefs[SessionKeys.ROLE] ?: UserRole.CASHIER.name),
            loginAt = prefs[SessionKeys.LOGIN_AT] ?: 0L,
            lastActiveAt = prefs[SessionKeys.LAST_ACTIVE_AT] ?: 0L
        )
    }

    override suspend fun startSession(user: User) {
        val now = System.currentTimeMillis()
        context.sessionDataStore.edit { prefs ->
            prefs[SessionKeys.USER_ID] = user.id
            prefs[SessionKeys.FULL_NAME] = user.fullName
            prefs[SessionKeys.ROLE] = user.role.name
            prefs[SessionKeys.LOGIN_AT] = now
            prefs[SessionKeys.LAST_ACTIVE_AT] = now
        }
    }

    override suspend fun touchSession() {
        context.sessionDataStore.edit { prefs ->
            prefs[SessionKeys.LAST_ACTIVE_AT] = System.currentTimeMillis()
        }
    }

    override suspend fun endSession() {
        context.sessionDataStore.edit { it.clear() }
    }
}
