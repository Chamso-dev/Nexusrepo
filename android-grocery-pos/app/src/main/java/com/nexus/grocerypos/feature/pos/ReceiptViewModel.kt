package com.nexus.grocerypos.feature.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.BusinessSettings
import com.nexus.grocerypos.domain.model.Sale
import com.nexus.grocerypos.domain.printing.PrinterDevice
import com.nexus.grocerypos.domain.printing.ReceiptPrinter
import com.nexus.grocerypos.domain.repository.SalesRepository
import com.nexus.grocerypos.domain.repository.SettingsRepository
import com.nexus.grocerypos.domain.usecase.pos.GenerateReceiptTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptUiState(
    val isLoading: Boolean = true,
    val sale: Sale? = null,
    val receiptText: String = "",
    val errorMessage: String? = null,
    val printers: List<PrinterDevice> = emptyList(),
    val showPrinterPicker: Boolean = false,
    val isPrinting: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val settingsRepository: SettingsRepository,
    private val generateReceiptText: GenerateReceiptTextUseCase,
    private val receiptPrinter: ReceiptPrinter
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

    /** Called once the runtime Bluetooth permission has been granted. */
    fun openPrinterPicker() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPrinting = true, statusMessage = null) }
            receiptPrinter.pairedPrinters()
                .onSuccess { printers ->
                    if (printers.isEmpty()) {
                        _uiState.update {
                            it.copy(isPrinting = false, statusMessage = "No paired printers. Pair one in Bluetooth settings.")
                        }
                    } else {
                        _uiState.update { it.copy(isPrinting = false, printers = printers, showPrinterPicker = true) }
                    }
                }
                .onError { message, _ ->
                    _uiState.update { it.copy(isPrinting = false, statusMessage = message) }
                }
        }
    }

    fun dismissPrinterPicker() = _uiState.update { it.copy(showPrinterPicker = false) }

    fun print(device: PrinterDevice) {
        val text = _uiState.value.receiptText
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(showPrinterPicker = false, isPrinting = true, statusMessage = null) }
            receiptPrinter.print(device, text)
                .onSuccess {
                    _uiState.update { it.copy(isPrinting = false, statusMessage = "Receipt sent to ${device.name}") }
                }
                .onError { message, _ ->
                    _uiState.update { it.copy(isPrinting = false, statusMessage = message) }
                }
        }
    }

    fun consumeStatusMessage() = _uiState.update { it.copy(statusMessage = null) }
}
