package com.nexus.grocerypos.feature.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.Customer
import com.nexus.grocerypos.domain.model.Sale
import com.nexus.grocerypos.domain.repository.CustomerRepository
import com.nexus.grocerypos.domain.repository.SalesRepository
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerEditUiState(
    val customerId: Long? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val balance: Double = 0.0,
    val paymentAmount: String = "",
    val purchaseHistory: List<Sale> = emptyList(),
    val nameError: String? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class CustomerEditViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerEditUiState())
    val uiState: StateFlow<CustomerEditUiState> = _uiState.asStateFlow()

    private var loadedForId: Long? = -1L

    fun loadCustomer(customerId: Long?) {
        if (loadedForId == customerId) return
        loadedForId = customerId

        if (customerId == null) {
            _uiState.value = CustomerEditUiState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val customer = customerRepository.getCustomerById(customerId)
            val history = salesRepository.getSalesForCustomer(customerId)
            _uiState.value = if (customer != null) {
                CustomerEditUiState(
                    customerId = customer.id,
                    isLoading = false,
                    name = customer.name,
                    phone = customer.phone.orEmpty(),
                    email = customer.email.orEmpty(),
                    address = customer.address.orEmpty(),
                    notes = customer.notes.orEmpty(),
                    balance = customer.balance,
                    purchaseHistory = history.sortedByDescending { it.createdAt }
                )
            } else {
                _uiState.value.copy(isLoading = false, errorMessage = "Customer not found")
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(phone = value)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onAddressChange(value: String) {
        _uiState.value = _uiState.value.copy(address = value)
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun onPaymentAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(paymentAmount = value)
    }

    fun recordPayment(amount: Double) {
        val customerId = _uiState.value.customerId ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = customerRepository.adjustBalance(customerId, -amount)) {
                is Result.Success -> {
                    val refreshed = customerRepository.getCustomerById(customerId)
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        balance = refreshed?.balance ?: _uiState.value.balance,
                        paymentAmount = ""
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = "Name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val customer = Customer(
                id = state.customerId ?: 0,
                name = state.name.trim(),
                phone = state.phone.trim().ifBlank { null },
                email = state.email.trim().ifBlank { null },
                address = state.address.trim().ifBlank { null },
                balance = state.balance,
                notes = state.notes.trim().ifBlank { null }
            )
            when (val result = customerRepository.upsertCustomer(customer)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    fun delete() {
        val customerId = _uiState.value.customerId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = customerRepository.deleteCustomer(customerId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, isDeleted = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }
}
