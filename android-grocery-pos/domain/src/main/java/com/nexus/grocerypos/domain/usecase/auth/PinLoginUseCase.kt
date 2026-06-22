package com.nexus.grocerypos.domain.usecase.auth

import com.nexus.grocerypos.domain.model.User
import com.nexus.grocerypos.domain.repository.UserRepository
import com.nexus.grocerypos.domain.util.Result
import javax.inject.Inject

class PinLoginUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(userId: Long, pin: String): Result<User> {
        if (pin.length < 4) {
            return Result.Error("PIN must be at least 4 digits")
        }
        return userRepository.authenticateWithPin(userId, pin).onSuccess { user ->
            userRepository.startSession(user)
        }
    }
}

class LogoutUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke() = userRepository.endSession()
}
