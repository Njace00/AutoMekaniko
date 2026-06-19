package com.example.automekaniko

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

@SuppressLint("MissingPermission")
object BluetoothManager {
    private const val TAG = "BluetoothELM327"

    /**
     * Connect to ELM327 OBD2 Bluetooth dongle
     */
    fun connectToELM327(context: Context) {
        // Get Bluetooth adapter
        @Suppress("DEPRECATION")
        val adapter: BluetoothAdapter? = android.bluetooth.BluetoothAdapter.getDefaultAdapter()

        if (adapter == null) {
            Toast.makeText(context, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        // Check permissions for Bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Check if Bluetooth is enabled
        if (!adapter.isEnabled) {
            Toast.makeText(context, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show()
            val enableBtIntent = Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivity(enableBtIntent)
            return
        }

        // Get bonded devices
        val pairedDevices: Set<BluetoothDevice> = adapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(context, "No paired Bluetooth devices found. Please pair your ELM327 dongle first.", Toast.LENGTH_LONG).show()
            return
        }

        // Filter for ELM327 or common OBD2 device names
        val elm327Devices = pairedDevices.filter { device ->
            val name = device.name ?: ""
            name.contains("ELM", ignoreCase = true) ||
            name.contains("OBD", ignoreCase = true) ||
            name.contains("VGATE", ignoreCase = true)
        }

        val deviceNames: Array<String> = if (elm327Devices.isNotEmpty()) {
            elm327Devices.map { "${it.name} (${it.address})" }.toTypedArray()
        } else {
            pairedDevices.map { "${it.name} (${it.address})" }.toTypedArray()
        }

        val devices: List<BluetoothDevice> = if (elm327Devices.isNotEmpty()) elm327Devices else pairedDevices.toList()

        AlertDialog.Builder(context)
            .setTitle("Select ELM327 Device")
            .setItems(deviceNames) { _, which ->
                val selectedDevice: BluetoothDevice = devices[which]
                launchOBDActivity(context, selectedDevice)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Launch OBDActivity with selected ELM327 device
     */
    private fun launchOBDActivity(context: Context, device: BluetoothDevice) {
        Toast.makeText(context, "Connecting to ${device.name}...", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Connecting to ELM327: ${device.name} (${device.address})")

        val intent = Intent(context, OBDActivity::class.java)
        intent.putExtra("DEVICE_ADDRESS", device.address)
        intent.putExtra("DEVICE_NAME", device.name)
        context.startActivity(intent)
    }
}


