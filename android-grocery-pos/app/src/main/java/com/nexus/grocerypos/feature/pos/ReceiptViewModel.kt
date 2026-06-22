package com.nexus.grocerypos.feature.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.BusinessSettings
import com.nexus.grocerypos.domain.model.Sale
import com.nexus.grocerypos.domain.repository.SalesRepository
import com.nexus.grocerypos.domain.repository.SettingsRepository
import com.nexus.grocerypos.domain.usecase.pos.GenerateReceiptTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptUiState(
    val isLoading: Boolean = true,
    val sale: Sale? = null,
    val receiptText: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val settingsRepository: SettingsRepository,
    private val generateReceiptText: GenerateReceiptTextUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    private var loadedSaleId: Long? = null

    fun load(saleId: Long) {
        if (loadedSaleId == saleId) return
        loadedSaleId = saleId
        viewModelScope.launch {
            _uiState.value = ReceiptUiState(isLoading = true)
            val sale = salesRepository.getSaleById(saleId)
            val settings: BusinessSettings = settingsRepository.observeSettings().first()
            _uiState.value = if (sale != null) {
                ReceiptUiState(isLoading = false, sale = sale, receiptText = generateReceiptText(sale, settings))
            } else {
                ReceiptUiState(isLoading = false, errorMessage = "Receipt not found")
            }
        }
    }
}
