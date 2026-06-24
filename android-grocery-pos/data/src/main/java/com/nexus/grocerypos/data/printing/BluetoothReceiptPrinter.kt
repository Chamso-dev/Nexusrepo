package com.nexus.grocerypos.data.printing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.nexus.grocerypos.domain.printing.PrinterDevice
import com.nexus.grocerypos.domain.printing.ReceiptPrinter
import com.nexus.grocerypos.domain.util.DispatcherProvider
import com.nexus.grocerypos.domain.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Prints receipts to a classic Bluetooth (SPP/RFCOMM) thermal printer using the ESC/POS
 * command set. Replaces the PC build's USB/serial thermal printer with a wireless one.
 */
@Singleton
class BluetoothReceiptPrinter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : ReceiptPrinter {

    private val adapter: BluetoothAdapter?
        get() = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    @SuppressLint("MissingPermission")
    override suspend fun pairedPrinters(): Result<List<PrinterDevice>> = withContext(dispatchers.io) {
        val adapter = adapter
            ?: return@withContext Result.Error("This device has no Bluetooth hardware.")
        if (!adapter.isEnabled) {
            return@withContext Result.Error("Bluetooth is turned off. Enable it and pair your printer.")
        }
        if (!hasConnectPermission()) {
            return@withContext Result.Error("Bluetooth permission is required to find printers.")
        }
        try {
            val devices = adapter.bondedDevices.orEmpty().map {
                PrinterDevice(name = it.name ?: "Unknown printer", address = it.address)
            }
            Result.Success(devices)
        } catch (t: Throwable) {
            Result.Error(t.message ?: "Could not read paired Bluetooth devices.", t)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun print(device: PrinterDevice, receiptText: String): Result<Unit> =
        withContext(dispatchers.io) {
            val adapter = adapter
                ?: return@withContext Result.Error("This device has no Bluetooth hardware.")
            if (!hasConnectPermission()) {
                return@withContext Result.Error("Bluetooth permission is required to print.")
            }
            val target: BluetoothDevice = try {
                adapter.getRemoteDevice(device.address)
            } catch (t: Throwable) {
                return@withContext Result.Error("Printer address is invalid.", t)
            }

            var socket: BluetoothSocket? = null
            try {
                adapter.cancelDiscovery()
                socket = target.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                socket.outputStream.use { stream ->
                    stream.writeReceipt(receiptText)
                    stream.flush()
                }
                Result.Success(Unit)
            } catch (t: Throwable) {
                Result.Error("Could not print: ${t.message ?: "connection failed"}", t)
            } finally {
                runCatching { socket?.close() }
            }
        }

    private fun OutputStream.writeReceipt(text: String) {
        write(INIT)
        write(ALIGN_LEFT)
        // CP437 is the default codepage on most ESC/POS thermal printers.
        write(text.toByteArray(charset = Charsets.US_ASCII))
        write(FEED_AND_CUT)
    }

    private fun hasConnectPermission(): Boolean {
        // BLUETOOTH_CONNECT is only enforced from Android 12 (API 31); older versions
        // are covered by the install-time BLUETOOTH permission.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        // Standard Serial Port Profile UUID used by virtually all ESC/POS BT printers.
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        val INIT = byteArrayOf(0x1B, 0x40)            // ESC @  -> reset printer
        val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00) // ESC a 0
        // Feed 4 lines then partial cut (GS V 66 0); printers without a cutter ignore it.
        val FEED_AND_CUT = byteArrayOf(0x1B, 0x64, 0x04, 0x1D, 0x56, 0x42, 0x00)
    }
}
