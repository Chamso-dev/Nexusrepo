package com.nexus.grocerypos.domain.printing

import com.nexus.grocerypos.domain.util.Result

/** A Bluetooth (or otherwise connectable) receipt printer the cashier can print to. */
data class PrinterDevice(
    val name: String,
    val address: String
)

/**
 * Abstraction over the physical receipt printer so feature/UI code never touches
 * Android Bluetooth APIs directly. Implemented in the data layer.
 */
interface ReceiptPrinter {
    /** Printers already paired with the device at the OS level. */
    suspend fun pairedPrinters(): Result<List<PrinterDevice>>

    /** Opens an RFCOMM connection to [device] and prints [receiptText] as ESC/POS. */
    suspend fun print(device: PrinterDevice, receiptText: String): Result<Unit>
}
