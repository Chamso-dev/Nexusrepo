package com.nexus.grocerypos.domain.usecase.auth

import com.nexus.grocerypos.domain.model.User
import com.nexus.grocerypos.domain.repository.UserRepository
import com.nexus.grocerypos.domain.util.Result
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank() || password.isBlank()) {
            return Result.Error("Username and password are required")
        }
        return userRepository.authenticate(username.trim(), password).onSuccess { user ->
            userRepository.startSession(user)
        }
    }
}
