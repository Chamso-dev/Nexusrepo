package com.nexus.grocerypos.feature.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.PurchaseOrder
import com.nexus.grocerypos.domain.repository.PurchaseRepository
import com.nexus.grocerypos.domain.usecase.purchase.ReceivePurchaseOrderUseCase
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiveLineItem(
    val productId: Long,
    val productName: String,
    val quantityOrdered: Double,
    val quantityReceived: Double,
    val receiveNowInput: String
)

data class PurchaseOrderReceiveUiState(
    val orderId: Long? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val order: PurchaseOrder? = null,
    val lineItems: List<ReceiveLineItem> = emptyList(),
    val errorMessage: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class PurchaseOrderReceiveViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val receivePurchaseOrderUseCase: ReceivePurchaseOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseOrderReceiveUiState())
    val uiState: StateFlow<PurchaseOrderReceiveUiState> = _uiState.asStateFlow()

    private var loadedForId: Long? = null

    fun loadOrder(id: Long) {
        if (loadedForId == id) return
        loadedForId = id

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val order = purchaseRepository.getPurchaseOrderById(id)
            _uiState.value = if (order != null) {
                PurchaseOrderReceiveUiState(
                    orderId = order.id,
                    isLoading = false,
                    order = order,
                    lineItems = order.items.map { item ->
                        val remaining = (item.quantityOrdered - item.quantityReceived).coerceAtLeast(0.0)
                        ReceiveLineItem(
                            productId = item.productId,
                            productName = item.productName,
                            quantityOrdered = item.quantityOrdered,
                            quantityReceived = item.quantityReceived,
                            receiveNowInput = formatQuantity(remaining)
                        )
                    }
                )
            } else {
                _uiState.value.copy(isLoading = false, errorMessage = "Purchase order not found")
            }
        }
    }

    fun onReceiveQuantityChange(productId: Long, value: String) {
        _uiState.value = _uiState.value.copy(
            lineItems = _uiState.value.lineItems.map {
                if (it.productId == productId) it.copy(receiveNowInput = value) else it
            }
        )
    }

    fun confirmReceive(actorUserId: Long) {
        val state = _uiState.value
        val orderId = state.orderId ?: return
        val quantities = state.lineItems.associate { it.productId to (it.receiveNowInput.toDoubleOrNull() ?: 0.0) }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val result = receivePurchaseOrderUseCase(orderId, quantities, actorUserId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, isComplete = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun formatQuantity(value: Double): String {
        return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
    }
}
