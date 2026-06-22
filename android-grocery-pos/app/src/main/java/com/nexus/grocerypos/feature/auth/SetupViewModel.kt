package com.nexus.grocerypos.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.User
import com.nexus.grocerypos.domain.model.UserRole
import com.nexus.grocerypos.domain.repository.UserRepository
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupUiState(
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val pin: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) { _uiState.value = _uiState.value.copy(fullName = value, errorMessage = null) }
    fun onUsernameChange(value: String) { _uiState.value = _uiState.value.copy(username = value, errorMessage = null) }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) { _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null) }
    fun onPinChange(value: String) { _uiState.value = _uiState.value.copy(pin = value.filter { it.isDigit() }.take(6), errorMessage = null) }

    fun createOwnerAccount() {
        val state = _uiState.value
        if (state.fullName.isBlank() || state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "All fields are required")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Password must be at least 6 characters")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Passwords do not match")
            return
        }
        if (state.pin.isNotEmpty() && state.pin.length < 4) {
            _uiState.value = state.copy(errorMessage = "PIN must be at least 4 digits")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = userRepository.createUser(
                user = User(
                    fullName = state.fullName.trim(),
                    username = state.username.trim(),
                    passwordHash = "",
                    role = UserRole.OWNER
                ),
                rawPassword = state.password,
                rawPin = state.pin.ifBlank { null }
            )
            when (result) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isComplete = true)
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
