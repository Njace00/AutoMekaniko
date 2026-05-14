package com.example.automekaniko

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

private const val TAG = "OBD_CLASSIC"

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    // ─────────────────────────────────────────────
    // SPP UUID for Classic Bluetooth
    // ─────────────────────────────────────────────
    private val SPP_UUID: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // ─────────────────────────────────────────────
    // Views
    // ─────────────────────────────────────────────
    private lateinit var tvBleStatus: TextView
    private lateinit var tvStatusMsg: TextView
    private lateinit var btnConnect: Button

    private lateinit var speedVal: TextView
    private lateinit var rpmVal: TextView
    private lateinit var coolantVal: TextView
    private lateinit var throttleVal: TextView
    private lateinit var loadVal: TextView

    // ─────────────────────────────────────────────
    // Bluetooth
    // ─────────────────────────────────────────────
    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    private var isConnected = false
    private var pollJob: Job? = null

    // ─────────────────────────────────────────────
    // Permission launcher
    // ─────────────────────────────────────────────
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->

        if (grants.all { it.value }) {
            connectToPairedOBD()
        } else {
            toast("Bluetooth permissions denied")
        }
    }

    // ─────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        bindViews()

        btnConnect.setOnClickListener {
            if (isConnected) {
                disconnectOBD()
            } else {
                requestPermissions()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectOBD()
    }

    // ─────────────────────────────────────────────
    // Bind views
    // ─────────────────────────────────────────────
    private fun bindViews() {

        tvBleStatus = findViewById(R.id.tvBleStatus)
        tvStatusMsg = findViewById(R.id.tvStatusMsg)
        btnConnect = findViewById(R.id.btnConnect)

        speedVal = findViewById(R.id.speedVal)
        rpmVal = findViewById(R.id.rpmVal)
        coolantVal = findViewById(R.id.coolantVal)
        throttleVal = findViewById(R.id.throttleVal)
        loadVal = findViewById(R.id.loadVal)
    }

    // ─────────────────────────────────────────────
    // Permissions
    // ─────────────────────────────────────────────
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {

        val needed = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
                needed += Manifest.permission.BLUETOOTH_CONNECT

        } else {

            if (!hasPermission(Manifest.permission.BLUETOOTH))
                needed += Manifest.permission.BLUETOOTH

            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
                needed += Manifest.permission.ACCESS_FINE_LOCATION
        }

        if (needed.isEmpty()) {
            connectToPairedOBD()
        } else {
            permLauncher.launch(needed.toTypedArray())
        }
    }

    // ─────────────────────────────────────────────
    // Connect to paired OBD
    // ─────────────────────────────────────────────
    private fun connectToPairedOBD() {

        val adapter = BluetoothAdapter.getDefaultAdapter()

        if (adapter == null) {
            toast("Bluetooth not supported")
            return
        }

        if (!adapter.isEnabled) {
            toast("Please enable Bluetooth")
            return
        }

        setStatus("Searching paired OBD adapter...")

        val device = adapter.bondedDevices.firstOrNull {

            val name = it.name ?: ""

            Log.d(TAG, "Paired device: $name")

            name.contains("OBD", true) ||
                    name.contains("ELM", true) ||
                    name.contains("VLINK", true) ||
                    name.contains("SCAN", true)
        }

        if (device == null) {
            setStatus("No paired OBD adapter found")
            return
        }

        connectSocket(device)
    }

    // ─────────────────────────────────────────────
    // Socket connection
    // ─────────────────────────────────────────────
    private fun connectSocket(device: BluetoothDevice) {

        lifecycleScope.launch(Dispatchers.IO) {

            try {

                runOnUiThread {
                    setStatus("Connecting to ${device.name}...")
                    btnConnect.isEnabled = false
                }

                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()

                socket?.connect()

                input = socket?.inputStream
                output = socket?.outputStream

                isConnected = true

                runOnUiThread {
                    setBleConnected(true)
                    setStatus("Connected to ${device.name}")
                    btnConnect.isEnabled = true
                }

                initElm327()

                startPolling()

            } catch (e: Exception) {

                Log.e(TAG, "Connection failed", e)

                runOnUiThread {
                    setStatus("Connection failed")
                    btnConnect.isEnabled = true
                    setBleConnected(false)
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // ELM327 Init
    // ─────────────────────────────────────────────
    private suspend fun initElm327() {

        sendCommand("ATZ")
        delay(2000)

        sendCommand("ATE0")
        delay(500)

        sendCommand("ATL0")
        delay(500)

        sendCommand("ATS0")
        delay(500)

        sendCommand("ATSP0")
        delay(1000)

        Log.d(TAG, sendCommand("0100"))
    }

    // ─────────────────────────────────────────────
    // Send command
    // ─────────────────────────────────────────────
    private suspend fun sendCommand(command: String): String {

        return withContext(Dispatchers.IO) {

            try {

                output?.write("$command\r".toByteArray())
                output?.flush()

                delay(300)

                val buffer = ByteArray(1024)

                val bytes = input?.read(buffer) ?: 0

                val response = String(buffer, 0, bytes)

                Log.d(TAG, "CMD: $command")
                Log.d(TAG, "RSP: $response")

                response

            } catch (e: Exception) {

                Log.e(TAG, "Send failed", e)

                ""
            }
        }
    }

    // ─────────────────────────────────────────────
    // Polling loop
    // ─────────────────────────────────────────────
    private fun startPolling() {

        pollJob?.cancel()

        pollJob = lifecycleScope.launch(Dispatchers.IO) {

            while (isActive && isConnected) {

                try {

                    val rpm = parseRPM(sendCommand("010C"))
                    val speed = parseSpeed(sendCommand("010D"))
                    val temp = parseTemp(sendCommand("0105"))
                    val throttle = parsePercent(sendCommand("0111"), "11")
                    val load = parsePercent(sendCommand("0104"), "04")

                    runOnUiThread {

                        rpm?.let { rpmVal.text = it }
                        speed?.let { speedVal.text = it }
                        temp?.let { coolantVal.text = it }
                        throttle?.let { throttleVal.text = it }
                        load?.let { loadVal.text = it }
                    }

                    delay(500)

                } catch (e: Exception) {

                    Log.e(TAG, "Polling failed", e)
                    delay(1000)
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // Parsers
    // ─────────────────────────────────────────────
    private fun getBytes(raw: String): List<String> {

        val clean = raw
            .replace("\r", "")
            .replace("\n", "")
            .replace(">", "")
            .trim()

        return clean.chunked(2)
    }

    private fun parseRPM(raw: String): String? {

        val b = getBytes(raw)

        for (i in 0 until b.size - 3) {

            if (b[i] == "41" && b[i + 1] == "0C") {

                val value =
                    (b[i + 2].toInt(16) * 256 + b[i + 3].toInt(16)) / 4

                return value.toString()
            }
        }

        return null
    }

    private fun parseSpeed(raw: String): String? {

        val b = getBytes(raw)

        for (i in 0 until b.size - 2) {

            if (b[i] == "41" && b[i + 1] == "0D") {
                return b[i + 2].toInt(16).toString()
            }
        }

        return null
    }

    private fun parseTemp(raw: String): String? {

        val b = getBytes(raw)

        for (i in 0 until b.size - 2) {

            if (b[i] == "41" && b[i + 1] == "05") {
                return (b[i + 2].toInt(16) - 40).toString()
            }
        }

        return null
    }

    private fun parsePercent(raw: String, pid: String): String? {

        val b = getBytes(raw)

        for (i in 0 until b.size - 2) {

            if (b[i] == "41" && b[i + 1] == pid) {

                val value =
                    (b[i + 2].toInt(16) / 255.0) * 100

                return "%.1f".format(value)
            }
        }

        return null
    }

    // ─────────────────────────────────────────────
    // Disconnect
    // ─────────────────────────────────────────────
    private fun disconnectOBD() {

        try {

            pollJob?.cancel()

            input?.close()
            output?.close()
            socket?.close()

        } catch (_: Exception) {
        }

        input = null
        output = null
        socket = null

        isConnected = false

        runOnUiThread {
            setBleConnected(false)
            setStatus("Disconnected")
        }
    }

    // ─────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────
    private fun setStatus(msg: String) {
        tvStatusMsg.text = msg
    }

    private fun setBleConnected(connected: Boolean) {

        if (connected) {

            tvBleStatus.text = "● CONNECTED"
            tvBleStatus.setTextColor(0xFF00E676.toInt())

            btnConnect.text = "DISCONNECT"

        } else {

            tvBleStatus.text = "● DISCONNECTED"
            tvBleStatus.setTextColor(0xFFFF5555.toInt())

            btnConnect.text = "CONNECT"
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}