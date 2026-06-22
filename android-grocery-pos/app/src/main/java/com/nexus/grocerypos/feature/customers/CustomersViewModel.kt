package com.nexus.grocerypos.feature.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.Customer
import com.nexus.grocerypos.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CustomersUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val customers: List<Customer> = emptyList()
)

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    private val filteredCustomers = query.flatMapLatest { q ->
        customerRepository.observeCustomers(query = q)
    }

    val uiState: StateFlow<CustomersUiState> = combine(query, filteredCustomers) { q, customers ->
        CustomersUiState(isLoading = false, query = q, customers = customers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomersUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }
}
