package com.nexus.grocerypos.feature.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.Supplier
import com.nexus.grocerypos.domain.repository.SupplierRepository
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupplierEditUiState(
    val supplierId: Long? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val balanceOwed: Double = 0.0,
    val paymentAmount: String = "",
    val nameError: String? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class SupplierEditViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierEditUiState())
    val uiState: StateFlow<SupplierEditUiState> = _uiState.asStateFlow()

    private var loadedForId: Long? = -1L

    fun loadSupplier(supplierId: Long?) {
        if (loadedForId == supplierId) return
        loadedForId = supplierId

        if (supplierId == null) {
            _uiState.value = SupplierEditUiState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val supplier = supplierRepository.getSupplierById(supplierId)
            _uiState.value = if (supplier != null) {
                SupplierEditUiState(
                    supplierId = supplier.id,
                    isLoading = false,
                    name = supplier.name,
                    contactPerson = supplier.contactPerson.orEmpty(),
                    phone = supplier.phone.orEmpty(),
                    email = supplier.email.orEmpty(),
                    address = supplier.address.orEmpty(),
                    notes = supplier.notes.orEmpty(),
                    balanceOwed = supplier.balanceOwed
                )
            } else {
                _uiState.value.copy(isLoading = false, errorMessage = "Supplier not found")
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onContactPersonChange(value: String) {
        _uiState.value = _uiState.value.copy(contactPerson = value)
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
        val supplierId = _uiState.value.supplierId ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = supplierRepository.adjustBalanceOwed(supplierId, -amount)) {
                is Result.Success -> {
                    val refreshed = supplierRepository.getSupplierById(supplierId)
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        balanceOwed = refreshed?.balanceOwed ?: _uiState.value.balanceOwed,
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
            val supplier = Supplier(
                id = state.supplierId ?: 0,
                name = state.name.trim(),
                contactPerson = state.contactPerson.trim().ifBlank { null },
                phone = state.phone.trim().ifBlank { null },
                email = state.email.trim().ifBlank { null },
                address = state.address.trim().ifBlank { null },
                balanceOwed = state.balanceOwed,
                notes = state.notes.trim().ifBlank { null }
            )
            when (val result = supplierRepository.upsertSupplier(supplier)) {
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
        val supplierId = _uiState.value.supplierId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = supplierRepository.deleteSupplier(supplierId)) {
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
