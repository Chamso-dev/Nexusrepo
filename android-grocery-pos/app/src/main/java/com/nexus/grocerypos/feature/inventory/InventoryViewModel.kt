package com.nexus.grocerypos.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.InventoryTransaction
import com.nexus.grocerypos.domain.model.InventoryTransactionType
import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.model.ProductWithDetails
import com.nexus.grocerypos.domain.repository.InventoryRepository
import com.nexus.grocerypos.domain.repository.ProductRepository
import com.nexus.grocerypos.domain.repository.UserRepository
import com.nexus.grocerypos.domain.usecase.inventory.AdjustStockUseCase
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InventoryTab { LOW_STOCK, ADJUST, HISTORY }

data class InventoryUiState(
    val selectedTab: InventoryTab = InventoryTab.LOW_STOCK,
    val lowStockProducts: List<Product> = emptyList(),
    val history: List<InventoryTransaction> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<ProductWithDetails> = emptyList(),
    val selectedProduct: Product? = null,
    val transactionType: InventoryTransactionType = InventoryTransactionType.STOCK_IN,
    val quantityInput: String = "",
    val reasonInput: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val adjustStockUseCase: AdjustStockUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    private val actorUserId: StateFlow<Long?> = userRepository.observeSession()
        .map { it?.userId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val lowStockProducts: StateFlow<List<Product>> = productRepository.observeLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val history: StateFlow<List<InventoryTransaction>> = inventoryRepository.observeHistory(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val searchResults: StateFlow<List<ProductWithDetails>> = searchQuery
        .flatMapLatest { query -> productRepository.observeProducts(query = query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            combine(lowStockProducts, history, searchResults) { low, hist, results ->
                _uiState.value = _uiState.value.copy(
                    lowStockProducts = low,
                    history = hist,
                    searchResults = results
                )
            }.collect()
        }
    }

    fun onTabSelected(tab: InventoryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchQuery.value = query
    }

    fun onProductSelected(product: Product) {
        _uiState.value = _uiState.value.copy(
            selectedProduct = product,
            searchQuery = "",
            searchResults = emptyList(),
            quantityInput = "",
            reasonInput = "",
            submitError = null,
            submitSuccess = false
        )
        searchQuery.value = ""
    }

    fun startAdjustFor(product: Product) {
        onProductSelected(product)
        _uiState.value = _uiState.value.copy(selectedTab = InventoryTab.ADJUST)
    }

    fun onClearSelectedProduct() {
        _uiState.value = _uiState.value.copy(selectedProduct = null)
    }

    fun onTransactionTypeSelected(type: InventoryTransactionType) {
        _uiState.value = _uiState.value.copy(transactionType = type, submitError = null)
    }

    fun onQuantityChange(value: String) {
        _uiState.value = _uiState.value.copy(quantityInput = value, submitError = null)
    }

    fun onReasonChange(value: String) {
        _uiState.value = _uiState.value.copy(reasonInput = value)
    }

    fun submitAdjustment() {
        val state = _uiState.value
        val product = state.selectedProduct
        if (product == null) {
            _uiState.value = state.copy(submitError = "Select a product first")
            return
        }

        val enteredQuantity = state.quantityInput.toDoubleOrNull()
        if (enteredQuantity == null || enteredQuantity < 0) {
            _uiState.value = state.copy(submitError = "Enter a valid quantity")
            return
        }

        val delta = when (state.transactionType) {
            InventoryTransactionType.STOCK_IN, InventoryTransactionType.RETURN -> enteredQuantity
            InventoryTransactionType.STOCK_OUT -> -enteredQuantity
            InventoryTransactionType.ADJUSTMENT -> enteredQuantity - product.stockQuantity
            InventoryTransactionType.SALE, InventoryTransactionType.PURCHASE_RECEIVE -> enteredQuantity
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null, submitSuccess = false)
            val result = adjustStockUseCase(
                productId = product.id,
                type = state.transactionType,
                quantityDelta = delta,
                reason = state.reasonInput.trim().ifBlank { null },
                actorUserId = actorUserId.value
            )
            when (result) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitSuccess = true,
                    selectedProduct = null,
                    quantityInput = "",
                    reasonInput = ""
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitError = result.message
                )
            }
        }
    }

    fun dismissSubmitSuccess() {
        _uiState.value = _uiState.value.copy(submitSuccess = false)
    }
}
