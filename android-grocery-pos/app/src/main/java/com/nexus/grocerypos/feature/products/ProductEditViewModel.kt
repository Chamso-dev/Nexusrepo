package com.nexus.grocerypos.feature.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.Brand
import com.nexus.grocerypos.domain.model.Category
import com.nexus.grocerypos.domain.model.Product
import com.nexus.grocerypos.domain.model.StockUnit
import com.nexus.grocerypos.domain.repository.ProductRepository
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductEditUiState(
    val productId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val name: String = "",
    val sku: String = "",
    val barcode: String = "",
    val categoryId: Long? = null,
    val brandId: Long? = null,
    val costPrice: String = "",
    val sellingPrice: String = "",
    val stockQuantity: String = "",
    val lowStockThreshold: String = "5",
    val unit: StockUnit = StockUnit.PCS,
    val isActive: Boolean = true,
    val nameError: String? = null,
    val skuError: String? = null,
    val costPriceError: String? = null,
    val sellingPriceError: String? = null,
    val stockQuantityError: String? = null,
    val errorMessage: String? = null
) {
    val profitPerUnit: Double
        get() {
            val cost = costPrice.toDoubleOrNull() ?: 0.0
            val selling = sellingPrice.toDoubleOrNull() ?: 0.0
            return selling - cost
        }

    val marginPercent: Double
        get() {
            val selling = sellingPrice.toDoubleOrNull() ?: 0.0
            if (selling == 0.0) return 0.0
            return (profitPerUnit / selling) * 100
        }
}

@HiltViewModel
class ProductEditViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<Category>> = productRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val brands: StateFlow<List<Brand>> = productRepository.observeBrands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var loadedForId: Long? = -1L
    private var hasLoadedOnce = false

    fun loadProduct(id: Long?) {
        if (hasLoadedOnce && loadedForId == id) return
        hasLoadedOnce = true
        loadedForId = id

        if (id == null) {
            _uiState.value = ProductEditUiState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val product = productRepository.getProductById(id)
            if (product == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Product not found")
                return@launch
            }
            _uiState.value = ProductEditUiState(
                productId = product.id,
                createdAt = product.createdAt,
                isLoading = false,
                name = product.name,
                sku = product.sku,
                barcode = product.barcode.orEmpty(),
                categoryId = product.categoryId,
                brandId = product.brandId,
                costPrice = product.costPrice.toString(),
                sellingPrice = product.sellingPrice.toString(),
                stockQuantity = product.stockQuantity.toString(),
                lowStockThreshold = product.lowStockThreshold.toString(),
                unit = product.unit,
                isActive = product.isActive
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onSkuChange(value: String) {
        _uiState.value = _uiState.value.copy(sku = value, skuError = null)
    }

    fun onBarcodeChange(value: String) {
        _uiState.value = _uiState.value.copy(barcode = value)
    }

    fun onCategorySelected(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(categoryId = categoryId)
    }

    fun onBrandSelected(brandId: Long?) {
        _uiState.value = _uiState.value.copy(brandId = brandId)
    }

    fun onCostPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(costPrice = value, costPriceError = null)
    }

    fun onSellingPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(sellingPrice = value, sellingPriceError = null)
    }

    fun onStockQuantityChange(value: String) {
        _uiState.value = _uiState.value.copy(stockQuantity = value, stockQuantityError = null)
    }

    fun onLowStockThresholdChange(value: String) {
        _uiState.value = _uiState.value.copy(lowStockThreshold = value)
    }

    fun onUnitSelected(unit: StockUnit) {
        _uiState.value = _uiState.value.copy(unit = unit)
    }

    fun onActiveChange(isActive: Boolean) {
        _uiState.value = _uiState.value.copy(isActive = isActive)
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            when (val result = productRepository.upsertCategory(Category(name = name.trim()))) {
                is Result.Success -> onCategorySelected(result.data)
                is Result.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun addBrand(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            when (val result = productRepository.upsertBrand(Brand(name = name.trim()))) {
                is Result.Success -> onBrandSelected(result.data)
                is Result.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun save() {
        val state = _uiState.value
        val costPrice = state.costPrice.toDoubleOrNull()
        val sellingPrice = state.sellingPrice.toDoubleOrNull()
        val stockQuantity = state.stockQuantity.toDoubleOrNull()
        val lowStockThreshold = state.lowStockThreshold.toDoubleOrNull() ?: 5.0

        val nameError = if (state.name.isBlank()) "Name is required" else null
        val skuError = if (state.sku.isBlank()) "SKU is required" else null
        val costPriceError = if (costPrice == null || costPrice < 0) "Enter a valid cost price" else null
        val sellingPriceError = if (sellingPrice == null || sellingPrice < 0) "Enter a valid selling price" else null
        val stockQuantityError = if (stockQuantity == null || stockQuantity < 0) "Enter a valid stock quantity" else null

        if (nameError != null || skuError != null || costPriceError != null || sellingPriceError != null || stockQuantityError != null) {
            _uiState.value = state.copy(
                nameError = nameError,
                skuError = skuError,
                costPriceError = costPriceError,
                sellingPriceError = sellingPriceError,
                stockQuantityError = stockQuantityError
            )
            return
        }

        val product = Product(
            id = state.productId ?: 0,
            name = state.name.trim(),
            sku = state.sku.trim(),
            barcode = state.barcode.trim().ifBlank { null },
            categoryId = state.categoryId,
            brandId = state.brandId,
            costPrice = costPrice!!,
            sellingPrice = sellingPrice!!,
            stockQuantity = stockQuantity!!,
            lowStockThreshold = lowStockThreshold,
            unit = state.unit,
            isActive = state.isActive,
            createdAt = state.createdAt
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = productRepository.upsertProduct(product)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                is Result.Error -> _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
            }
        }
    }

    fun delete() {
        val id = _uiState.value.productId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = productRepository.deleteProduct(id)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isSaving = false, isDeleted = true)
                is Result.Error -> _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
            }
        }
    }
}
