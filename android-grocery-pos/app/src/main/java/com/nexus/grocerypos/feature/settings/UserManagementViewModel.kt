package com.nexus.grocerypos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.Session
import com.nexus.grocerypos.domain.model.User
import com.nexus.grocerypos.domain.model.UserRole
import com.nexus.grocerypos.domain.repository.UserRepository
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val users: StateFlow<List<User>> = userRepository.observeUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val session: StateFlow<Session?> = userRepository.observeSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    fun createUser(fullName: String, username: String, password: String, pin: String?, role: UserRole) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null, successMessage = null)
            val result = userRepository.createUser(
                user = User(
                    fullName = fullName.trim(),
                    username = username.trim(),
                    passwordHash = "",
                    role = role
                ),
                rawPassword = password,
                rawPin = pin?.ifBlank { null }
            )
            when (result) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isSubmitting = false, successMessage = "User created")
                is Result.Error -> _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = result.message)
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null, successMessage = null)
            when (val result = userRepository.updateUser(user)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isSubmitting = false, successMessage = "User updated")
                is Result.Error -> _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = result.message)
            }
        }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null, successMessage = null)
            when (val result = userRepository.deleteUser(id)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isSubmitting = false, successMessage = "User deleted")
                is Result.Error -> _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = result.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
