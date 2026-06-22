package com.nexus.grocerypos.feature.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.Category
import com.nexus.grocerypos.domain.model.ProductWithDetails
import com.nexus.grocerypos.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProductsUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val products: List<ProductWithDetails> = emptyList()
)

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategoryId = MutableStateFlow<Long?>(null)

    private val filteredProducts = combine(query, selectedCategoryId) { q, categoryId ->
        q to categoryId
    }.flatMapLatest { (q, categoryId) ->
        productRepository.observeProducts(query = q, categoryId = categoryId)
    }

    val uiState: StateFlow<ProductsUiState> = combine(
        query,
        selectedCategoryId,
        filteredProducts,
        productRepository.observeCategories()
    ) { q, categoryId, products, categories ->
        ProductsUiState(
            isLoading = false,
            query = q,
            selectedCategoryId = categoryId,
            categories = categories,
            products = products
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductsUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategorySelected(categoryId: Long?) {
        selectedCategoryId.value = if (selectedCategoryId.value == categoryId) null else categoryId
    }
}
