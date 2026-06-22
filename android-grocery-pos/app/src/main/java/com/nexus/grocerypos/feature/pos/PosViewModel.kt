package com.nexus.grocerypos.feature.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.BusinessSettings
import com.nexus.grocerypos.domain.model.Customer
import com.nexus.grocerypos.domain.model.DiscountType
import com.nexus.grocerypos.domain.model.PaymentMethod
import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.model.ProductWithDetails
import com.nexus.grocerypos.domain.model.SalePayment
import com.nexus.grocerypos.domain.repository.CustomerRepository
import com.nexus.grocerypos.domain.repository.ProductRepository
import com.nexus.grocerypos.domain.repository.SettingsRepository
import com.nexus.grocerypos.domain.usecase.pos.CalculateCartTotalsUseCase
import com.nexus.grocerypos.domain.usecase.pos.CartLine
import com.nexus.grocerypos.domain.usecase.pos.CartTotals
import com.nexus.grocerypos.domain.usecase.pos.CheckoutUseCase
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PosUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val searchResults: List<ProductWithDetails> = emptyList(),
    val cartLines: List<CartLine> = emptyList(),
    val totals: CartTotals = CartTotals(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    val orderDiscountPercent: Double = 0.0,
    val settings: BusinessSettings = BusinessSettings(),
    val customerQuery: String = "",
    val customerResults: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val showPaymentSheet: Boolean = false,
    val payments: List<SalePayment> = emptyList(),
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val scannerError: String? = null,
    val completedSaleId: Long? = null
)

@HiltViewModel
class PosViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateCartTotals: CalculateCartTotalsUseCase,
    private val checkoutUseCase: CheckoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    init {
        settingsRepository.observeSettings()
            .onEach { settings -> _uiState.update { it.copy(settings = settings, isLoading = false) }; recalcTotals() }
            .launchIn(viewModelScope)

        _uiState.map { it.searchQuery }.distinctUntilChanged()
            .flatMapLatest { query -> if (query.isBlank()) flowOf(emptyList()) else productRepository.observeProducts(query = query) }
            .onEach { results -> _uiState.update { it.copy(searchResults = results) } }
            .launchIn(viewModelScope)

        _uiState.map { it.customerQuery }.distinctUntilChanged()
            .flatMapLatest { query -> if (query.isBlank()) flowOf(emptyList()) else customerRepository.observeCustomers(query = query) }
            .onEach { results -> _uiState.update { it.copy(customerResults = results) } }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
    }

    fun onCustomerQueryChange(value: String) {
        _uiState.update { it.copy(customerQuery = value) }
    }

    fun addProductToCart(item: ProductWithDetails) = addProductToCart(item.product)

    fun addProductToCart(product: Product) {
        _uiState.update { state ->
            val existing = state.cartLines.find { it.productId == product.id }
            val updatedLines = if (existing != null) {
                val newQuantity = (existing.quantity + 1.0).coerceAtMost(product.stockQuantity)
                state.cartLines.map { if (it.productId == product.id) it.copy(quantity = newQuantity) else it }
            } else {
                state.cartLines + CartLine(
                    productId = product.id,
                    productName = product.name,
                    unitPrice = product.sellingPrice,
                    unitCost = product.costPrice,
                    quantity = if (product.stockQuantity > 0) 1.0 else 0.0,
                    availableStock = product.stockQuantity
                )
            }
            state.copy(cartLines = updatedLines, searchQuery = "", searchResults = emptyList())
        }
        recalcTotals()
    }

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            val product = productRepository.getProductByBarcode(barcode)
            if (product != null) {
                addProductToCart(product)
            } else {
                _uiState.update { it.copy(scannerError = "No product found for barcode $barcode") }
            }
        }
    }

    fun dismissScannerError() {
        _uiState.update { it.copy(scannerError = null) }
    }

    fun updateQuantity(productId: Long, quantity: Double) {
        _uiState.update { state ->
            val updated = state.cartLines.mapNotNull { line ->
                if (line.productId != productId) return@mapNotNull line
                val coerced = quantity.coerceIn(0.0, line.availableStock)
                if (coerced <= 0.0) null else line.copy(quantity = coerced)
            }
            state.copy(cartLines = updated)
        }
        recalcTotals()
    }

    fun updateDiscount(productId: Long, type: DiscountType, value: Double) {
        _uiState.update { state ->
            val updated = state.cartLines.map { line ->
                if (line.productId == productId) line.copy(discountType = type, discountValue = value.coerceAtLeast(0.0)) else line
            }
            state.copy(cartLines = updated)
        }
        recalcTotals()
    }

    fun removeLine(productId: Long) {
        _uiState.update { state -> state.copy(cartLines = state.cartLines.filter { it.productId != productId }) }
        recalcTotals()
    }

    fun updateOrderDiscountPercent(value: Double) {
        _uiState.update { it.copy(orderDiscountPercent = value.coerceAtLeast(0.0)) }
        recalcTotals()
    }

    fun selectCustomer(customer: Customer?) {
        _uiState.update { it.copy(selectedCustomer = customer, customerQuery = "", customerResults = emptyList()) }
    }

    fun openPaymentSheet() {
        _uiState.update { it.copy(showPaymentSheet = true, errorMessage = null) }
    }

    fun closePaymentSheet() {
        _uiState.update { it.copy(showPaymentSheet = false) }
    }

    fun addPayment(method: PaymentMethod, amount: Double, tenderedAmount: Double) {
        if (amount <= 0.0) return
        val changeDue = (tenderedAmount - amount).coerceAtLeast(0.0)
        _uiState.update { state ->
            state.copy(payments = state.payments + SalePayment(method = method, amount = amount, tenderedAmount = tenderedAmount, changeDue = changeDue))
        }
    }

    fun removePayment(index: Int) {
        _uiState.update { state -> state.copy(payments = state.payments.filterIndexed { i, _ -> i != index }) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeCompletedSale() {
        _uiState.update { it.copy(completedSaleId = null) }
    }

    fun checkout(cashierId: Long) {
        val state = _uiState.value
        if (state.isProcessing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            val result = checkoutUseCase(
                lines = state.cartLines,
                payments = state.payments,
                cashierId = cashierId,
                customerId = state.selectedCustomer?.id,
                taxRatePercent = state.settings.taxRatePercent,
                taxInclusive = state.settings.taxInclusive,
                orderDiscountPercent = state.orderDiscountPercent
            )
            when (result) {
                is Result.Success -> _uiState.update {
                    PosUiState(isLoading = false, settings = it.settings, completedSaleId = result.data)
                }
                is Result.Error -> _uiState.update { it.copy(isProcessing = false, errorMessage = result.message) }
            }
        }
    }

    private fun recalcTotals() {
        val state = _uiState.value
        val totals = calculateCartTotals(
            lines = state.cartLines,
            taxRatePercent = state.settings.taxRatePercent,
            taxInclusive = state.settings.taxInclusive,
            orderDiscountPercent = state.orderDiscountPercent
        )
        _uiState.update { it.copy(totals = totals) }
    }
}
