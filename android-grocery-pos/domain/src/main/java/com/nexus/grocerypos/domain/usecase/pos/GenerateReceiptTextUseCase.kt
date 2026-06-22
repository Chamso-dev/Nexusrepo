package com.nexus.grocerypos.domain.usecase.pos

import com.nexus.grocerypos.domain.model.BusinessSettings
import com.nexus.grocerypos.domain.model.Sale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** Renders a sale as plain-text suitable for thermal printers, sharing, or PDF export. */
class GenerateReceiptTextUseCase @Inject constructor() {
    operator fun invoke(sale: Sale, settings: BusinessSettings): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val symbol = settings.currencySymbol
        val sb = StringBuilder()

        if (settings.businessName.isNotBlank()) sb.appendLine(settings.businessName)
        if (settings.address.isNotBlank()) sb.appendLine(settings.address)
        if (settings.phone.isNotBlank()) sb.appendLine(settings.phone)
        sb.appendLine("--------------------------------")
        sb.appendLine("Receipt #${sale.receiptNumber}")
        sb.appendLine(dateFormat.format(Date(sale.createdAt)))
        sb.appendLine("--------------------------------")

        sale.items.forEach { item ->
            sb.appendLine(item.productName)
            sb.appendLine(
                "  %.2f x %s%.2f = %s%.2f".format(
                    item.quantity, symbol, item.unitPrice, symbol, item.netTotal
                )
            )
        }

        sb.appendLine("--------------------------------")
        sb.appendLine("Subtotal: $symbol%.2f".format(sale.subtotal))
        if (sale.discountTotal > 0) sb.appendLine("Discount: -$symbol%.2f".format(sale.discountTotal))
        if (sale.taxTotal > 0) sb.appendLine("Tax: $symbol%.2f".format(sale.taxTotal))
        sb.appendLine("TOTAL: $symbol%.2f".format(sale.grandTotal))
        sb.appendLine("--------------------------------")

        sale.payments.forEach { payment ->
            sb.appendLine("${payment.method}: $symbol%.2f".format(payment.amount))
            if (payment.changeDue > 0) sb.appendLine("Change: $symbol%.2f".format(payment.changeDue))
        }

        if (settings.receiptFooterMessage.isNotBlank()) {
            sb.appendLine("--------------------------------")
            sb.appendLine(settings.receiptFooterMessage)
        }

        return sb.toString()
    }
}
