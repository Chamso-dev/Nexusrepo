package com.nexus.grocerypos.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nexus.grocerypos.data.backup.BackupManager
import com.nexus.grocerypos.domain.model.BusinessSettings
import com.nexus.grocerypos.domain.repository.SettingsRepository
import com.nexus.grocerypos.domain.util.Result
import com.nexus.grocerypos.domain.util.resultOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private object Keys {
    val BUSINESS_NAME = stringPreferencesKey("business_name")
    val ADDRESS = stringPreferencesKey("address")
    val PHONE = stringPreferencesKey("phone")
    val EMAIL = stringPreferencesKey("email")
    val CURRENCY_CODE = stringPreferencesKey("currency_code")
    val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
    val TAX_RATE = doublePreferencesKey("tax_rate_percent")
    val TAX_INCLUSIVE = booleanPreferencesKey("tax_inclusive")
    val RECEIPT_FOOTER = stringPreferencesKey("receipt_footer")
    val RECEIPT_SHOW_LOGO = booleanPreferencesKey("receipt_show_logo")
    val LOW_STOCK_DEFAULT = doublePreferencesKey("low_stock_default")
}

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupManager: BackupManager
) : SettingsRepository {

    override fun observeSettings() = context.settingsDataStore.data.map { prefs ->
        BusinessSettings(
            businessName = prefs[Keys.BUSINESS_NAME] ?: "",
            address = prefs[Keys.ADDRESS] ?: "",
            phone = prefs[Keys.PHONE] ?: "",
            email = prefs[Keys.EMAIL] ?: "",
            currencyCode = prefs[Keys.CURRENCY_CODE] ?: "USD",
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "$",
            taxRatePercent = prefs[Keys.TAX_RATE] ?: 0.0,
            taxInclusive = prefs[Keys.TAX_INCLUSIVE] ?: false,
            receiptFooterMessage = prefs[Keys.RECEIPT_FOOTER] ?: "",
            receiptShowLogo = prefs[Keys.RECEIPT_SHOW_LOGO] ?: true,
            lowStockThresholdDefault = prefs[Keys.LOW_STOCK_DEFAULT] ?: 5.0
        )
    }

    override suspend fun updateSettings(settings: BusinessSettings): Result<Unit> = resultOf {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.BUSINESS_NAME] = settings.businessName
            prefs[Keys.ADDRESS] = settings.address
            prefs[Keys.PHONE] = settings.phone
            prefs[Keys.EMAIL] = settings.email
            prefs[Keys.CURRENCY_CODE] = settings.currencyCode
            prefs[Keys.CURRENCY_SYMBOL] = settings.currencySymbol
            prefs[Keys.TAX_RATE] = settings.taxRatePercent
            prefs[Keys.TAX_INCLUSIVE] = settings.taxInclusive
            prefs[Keys.RECEIPT_FOOTER] = settings.receiptFooterMessage
            prefs[Keys.RECEIPT_SHOW_LOGO] = settings.receiptShowLogo
            prefs[Keys.LOW_STOCK_DEFAULT] = settings.lowStockThresholdDefault
        }
    }

    override suspend fun exportBackup(): Result<String> = backupManager.exportBackup()

    override suspend fun restoreBackup(sourceUri: String): Result<Unit> = backupManager.restoreBackup(sourceUri)
}
