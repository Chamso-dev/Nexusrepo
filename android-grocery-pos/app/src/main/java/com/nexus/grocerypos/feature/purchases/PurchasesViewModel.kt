package com.nexus.grocerypos.feature.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.PurchaseOrder
import com.nexus.grocerypos.domain.model.PurchaseOrderStatus
import com.nexus.grocerypos.domain.repository.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PurchasesUiState(
    val isLoading: Boolean = true,
    val statusFilter: PurchaseOrderStatus? = null,
    val orders: List<PurchaseOrder> = emptyList()
)

@HiltViewModel
class PurchasesViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val statusFilter = MutableStateFlow<PurchaseOrderStatus?>(null)

    private val filteredOrders = statusFilter.flatMapLatest { status ->
        purchaseRepository.observePurchaseOrders(status)
    }

    val uiState: StateFlow<PurchasesUiState> = combine(statusFilter, filteredOrders) { status, orders ->
        PurchasesUiState(isLoading = false, statusFilter = status, orders = orders)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PurchasesUiState())

    fun onStatusFilterChange(status: PurchaseOrderStatus?) {
        statusFilter.value = status
    }
}
