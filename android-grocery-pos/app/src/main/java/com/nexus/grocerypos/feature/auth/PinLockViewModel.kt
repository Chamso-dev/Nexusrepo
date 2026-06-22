package com.nexus.grocerypos.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.User
import com.nexus.grocerypos.domain.repository.UserRepository
import com.nexus.grocerypos.domain.usecase.auth.LogoutUseCase
import com.nexus.grocerypos.domain.usecase.auth.PinLoginUseCase
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PinLockUiState(
    val userFullName: String = "",
    val pin: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val unlockedUser: User? = null
)

@HiltViewModel
class PinLockViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pinLoginUseCase: PinLoginUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private var userId: Long = -1L

    private val _uiState = MutableStateFlow(PinLockUiState())
    val uiState: StateFlow<PinLockUiState> = _uiState.asStateFlow()

    fun loadUser(id: Long) {
        if (userId == id) return
        userId = id
        viewModelScope.launch {
            val user = userRepository.getUserById(id)
            _uiState.value = _uiState.value.copy(userFullName = user?.fullName.orEmpty())
        }
    }

    fun onPinChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(6)
        _uiState.value = _uiState.value.copy(pin = digits, errorMessage = null)
        if (digits.length >= 4) submit(digits)
    }

    private fun submit(pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = pinLoginUseCase(userId, pin)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, unlockedUser = result.data)
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message, pin = "")
            }
        }
    }

    fun signOutInstead() {
        viewModelScope.launch { logoutUseCase() }
    }
}
