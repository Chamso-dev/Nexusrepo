package com.nexus.grocerypos.feature.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.Supplier
import com.nexus.grocerypos.domain.repository.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SuppliersUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val suppliers: List<Supplier> = emptyList()
)

@HiltViewModel
class SuppliersViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    private val filteredSuppliers = query.flatMapLatest { q ->
        supplierRepository.observeSuppliers(query = q)
    }

    val uiState: StateFlow<SuppliersUiState> = combine(query, filteredSuppliers) { q, suppliers ->
        SuppliersUiState(isLoading = false, query = q, suppliers = suppliers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SuppliersUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }
}
