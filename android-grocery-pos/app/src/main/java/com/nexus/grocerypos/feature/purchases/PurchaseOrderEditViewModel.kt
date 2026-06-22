package com.nexus.grocerypos.feature.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.model.PurchaseOrder
import com.nexus.grocerypos.domain.model.PurchaseOrderLineItem
import com.nexus.grocerypos.domain.model.PurchaseOrderStatus
import com.nexus.grocerypos.domain.model.Supplier
import com.nexus.grocerypos.domain.repository.ProductRepository
import com.nexus.grocerypos.domain.repository.PurchaseRepository
import com.nexus.grocerypos.domain.repository.SupplierRepository
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PurchaseOrderLineItemDraft(
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val quantityOrdered: String,
    val quantityReceived: Double = 0.0,
    val unitCost: String
)

data class PurchaseOrderEditUiState(
    val orderId: Long? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val orderNumber: String = "",
    val status: PurchaseOrderStatus = PurchaseOrderStatus.DRAFT,
    val supplier: Supplier? = null,
    val supplierQuery: String = "",
    val supplierResults: List<Supplier> = emptyList(),
    val isPickingSupplier: Boolean = false,
    val invoiceNumber: String = "",
    val notes: String = "",
    val lineItems: List<PurchaseOrderLineItemDraft> = emptyList(),
    val isPickingProduct: Boolean = false,
    val productQuery: String = "",
    val productResults: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
) {
    val isFinalized: Boolean get() = status == PurchaseOrderStatus.RECEIVED || status == PurchaseOrderStatus.CANCELLED
    val isExistingDraft: Boolean get() = orderId != null && status == PurchaseOrderStatus.DRAFT
    val totalAmount: Double get() = lineItems.sumOf {
        (it.quantityOrdered.toDoubleOrNull() ?: 0.0) * (it.unitCost.toDoubleOrNull() ?: 0.0)
    }
}

@HiltViewModel
class PurchaseOrderEditViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseOrderEditUiState())
    val uiState: StateFlow<PurchaseOrderEditUiState> = _uiState.asStateFlow()

    private val supplierQuery = MutableStateFlow("")
    private val productQuery = MutableStateFlow("")

    private var loadedForId: Long? = -1L
    private var cachedNewOrderNumber: String? = null

    init {
        viewModelScope.launch {
            supplierQuery.flatMapLatest { q -> supplierRepository.observeSuppliers(query = q) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
                .collect { results ->
                    _uiState.value = _uiState.value.copy(supplierResults = results)
                }
        }
        viewModelScope.launch {
            productQuery.flatMapLatest { q -> productRepository.observeProducts(query = q) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
                .collect { results ->
                    _uiState.value = _uiState.value.copy(productResults = results.map { it.product })
                }
        }
    }

    fun loadOrder(orderId: Long?) {
        if (loadedForId == orderId) return
        loadedForId = orderId

        if (orderId == null) {
            _uiState.value = PurchaseOrderEditUiState()
            viewModelScope.launch {
                val number = cachedNewOrderNumber ?: purchaseRepository.nextOrderNumber().also { cachedNewOrderNumber = it }
                _uiState.value = _uiState.value.copy(orderNumber = number)
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val order = purchaseRepository.getPurchaseOrderById(orderId)
            _uiState.value = if (order != null) {
                PurchaseOrderEditUiState(
                    orderId = order.id,
                    isLoading = false,
                    orderNumber = order.orderNumber,
                    status = order.status,
                    supplier = Supplier(id = order.supplierId, name = order.supplierName),
                    invoiceNumber = order.invoiceNumber.orEmpty(),
                    notes = order.notes.orEmpty(),
                    lineItems = order.items.map { item ->
                        PurchaseOrderLineItemDraft(
                            id = item.id,
                            productId = item.productId,
                            productName = item.productName,
                            quantityOrdered = formatQuantity(item.quantityOrdered),
                            quantityReceived = item.quantityReceived,
                            unitCost = formatQuantity(item.unitCost)
                        )
                    }
                )
            } else {
                _uiState.value.copy(isLoading = false, errorMessage = "Purchase order not found")
            }
        }
    }

    fun onSupplierQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(supplierQuery = value, isPickingSupplier = true)
        supplierQuery.value = value
    }

    fun openSupplierPicker() {
        _uiState.value = _uiState.value.copy(isPickingSupplier = true)
        supplierQuery.value = _uiState.value.supplierQuery
    }

    fun onSupplierSelected(supplier: Supplier) {
        _uiState.value = _uiState.value.copy(
            supplier = supplier,
            supplierQuery = "",
            isPickingSupplier = false
        )
    }

    fun onInvoiceNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(invoiceNumber = value)
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun openProductPicker() {
        _uiState.value = _uiState.value.copy(isPickingProduct = true)
        productQuery.value = _uiState.value.productQuery
    }

    fun closeProductPicker() {
        _uiState.value = _uiState.value.copy(isPickingProduct = false, productQuery = "")
        productQuery.value = ""
    }

    fun onProductQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(productQuery = value)
        productQuery.value = value
    }

    fun onProductSelected(product: Product) {
        val existing = _uiState.value.lineItems
        if (existing.any { it.productId == product.id }) {
            closeProductPicker()
            return
        }
        val newItem = PurchaseOrderLineItemDraft(
            productId = product.id,
            productName = product.name,
            quantityOrdered = "1",
            unitCost = formatQuantity(product.costPrice)
        )
        _uiState.value = _uiState.value.copy(
            lineItems = existing + newItem,
            isPickingProduct = false,
            productQuery = ""
        )
        productQuery.value = ""
    }

    fun onLineQuantityChange(productId: Long, value: String) {
        _uiState.value = _uiState.value.copy(
            lineItems = _uiState.value.lineItems.map {
                if (it.productId == productId) it.copy(quantityOrdered = value) else it
            }
        )
    }

    fun onLineUnitCostChange(productId: Long, value: String) {
        _uiState.value = _uiState.value.copy(
            lineItems = _uiState.value.lineItems.map {
                if (it.productId == productId) it.copy(unitCost = value) else it
            }
        )
    }

    fun removeLineItem(productId: Long) {
        _uiState.value = _uiState.value.copy(
            lineItems = _uiState.value.lineItems.filterNot { it.productId == productId }
        )
    }

    fun save() {
        val state = _uiState.value
        val supplier = state.supplier
        if (supplier == null) {
            _uiState.value = state.copy(errorMessage = "Select a supplier")
            return
        }
        val validItems = state.lineItems.filter { (it.quantityOrdered.toDoubleOrNull() ?: 0.0) > 0 }
        if (validItems.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Add at least one line item with a quantity greater than 0")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            val order = PurchaseOrder(
                id = state.orderId ?: 0,
                orderNumber = state.orderNumber,
                supplierId = supplier.id,
                supplierName = supplier.name,
                status = state.status,
                items = validItems.map { item ->
                    PurchaseOrderLineItem(
                        id = item.id,
                        productId = item.productId,
                        productName = item.productName,
                        quantityOrdered = item.quantityOrdered.toDoubleOrNull() ?: 0.0,
                        quantityReceived = item.quantityReceived,
                        unitCost = item.unitCost.toDoubleOrNull() ?: 0.0
                    )
                },
                invoiceNumber = state.invoiceNumber.trim().ifBlank { null },
                notes = state.notes.trim().ifBlank { null }
            )
            when (val result = purchaseRepository.upsertPurchaseOrder(order)) {
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
        val orderId = _uiState.value.orderId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = purchaseRepository.deletePurchaseOrder(orderId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, isDeleted = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun formatQuantity(value: Double): String {
        return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
    }
}
