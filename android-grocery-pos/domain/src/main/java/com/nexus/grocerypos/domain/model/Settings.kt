package com.nexus.grocerypos.domain.model

data class BusinessSettings(
    val businessName: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val taxRatePercent: Double = 0.0,
    val taxInclusive: Boolean = false,
    val receiptFooterMessage: String = "",
    val receiptShowLogo: Boolean = true,
    val lowStockThresholdDefault: Double = 5.0
)
