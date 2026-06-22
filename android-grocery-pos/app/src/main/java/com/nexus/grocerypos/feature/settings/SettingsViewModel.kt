package com.nexus.grocerypos.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.grocerypos.domain.model.BusinessSettings
import com.nexus.grocerypos.domain.repository.SettingsRepository
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val form: BusinessSettings = BusinessSettings(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
    val saveError: String? = null,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val exportError: String? = null,
    val backups: List<BackupFile> = emptyList(),
    val isRestoring: Boolean = false,
    val restoreError: String? = null,
    val restoreComplete: Boolean = false
)

data class BackupFile(
    val path: String,
    val name: String,
    val lastModified: Long
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var seeded = false

    init {
        viewModelScope.launch {
            val initial = settingsRepository.observeSettings().first()
            if (!seeded) {
                seeded = true
                _uiState.value = _uiState.value.copy(form = initial, isLoading = false)
            }
        }
        refreshBackups()
    }

    fun onBusinessNameChange(value: String) { updateForm { copy(businessName = value) } }
    fun onAddressChange(value: String) { updateForm { copy(address = value) } }
    fun onPhoneChange(value: String) { updateForm { copy(phone = value) } }
    fun onEmailChange(value: String) { updateForm { copy(email = value) } }
    fun onCurrencyCodeChange(value: String) { updateForm { copy(currencyCode = value) } }
    fun onCurrencySymbolChange(value: String) { updateForm { copy(currencySymbol = value) } }
    fun onTaxRateChange(value: String) { updateForm { copy(taxRatePercent = value.toDoubleOrNull() ?: taxRatePercent) } }
    fun onTaxInclusiveChange(value: Boolean) { updateForm { copy(taxInclusive = value) } }
    fun onReceiptFooterChange(value: String) { updateForm { copy(receiptFooterMessage = value) } }
    fun onReceiptShowLogoChange(value: Boolean) { updateForm { copy(receiptShowLogo = value) } }
    fun onLowStockThresholdChange(value: String) { updateForm { copy(lowStockThresholdDefault = value.toDoubleOrNull() ?: lowStockThresholdDefault) } }

    private inline fun updateForm(transform: BusinessSettings.() -> BusinessSettings) {
        _uiState.value = _uiState.value.copy(
            form = _uiState.value.form.transform(),
            saveMessage = null,
            saveError = null
        )
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveMessage = null, saveError = null)
            when (val result = settingsRepository.updateSettings(_uiState.value.form)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isSaving = false, saveMessage = "Settings saved")
                is Result.Error -> _uiState.value = _uiState.value.copy(isSaving = false, saveError = result.message)
            }
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportMessage = null, exportError = null)
            when (val result = settingsRepository.exportBackup()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isExporting = false, exportMessage = "Backup saved to ${result.data}")
                    refreshBackups()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isExporting = false, exportError = result.message)
            }
        }
    }

    fun refreshBackups() {
        val backupDir = context.getExternalFilesDir(null)?.resolve("backups")
        val files = backupDir?.takeIf { it.exists() }
            ?.listFiles { file -> file.isFile && file.extension.equals("zip", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?.map { BackupFile(path = it.absolutePath, name = it.name, lastModified = it.lastModified()) }
            ?: emptyList()
        _uiState.value = _uiState.value.copy(backups = files)
    }

    fun restoreBackup(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true, restoreError = null)
            when (val result = settingsRepository.restoreBackup(path)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isRestoring = false, restoreComplete = true)
                is Result.Error -> _uiState.value = _uiState.value.copy(isRestoring = false, restoreError = result.message)
            }
        }
    }

    fun dismissRestoreComplete() {
        _uiState.value = _uiState.value.copy(restoreComplete = false)
    }

    fun clearExportMessage() {
        _uiState.value = _uiState.value.copy(exportMessage = null, exportError = null)
    }
}
