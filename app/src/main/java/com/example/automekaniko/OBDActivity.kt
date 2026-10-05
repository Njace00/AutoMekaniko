package com.example.automekaniko

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.automekaniko.ui.screens.ObdScreen
import com.example.automekaniko.ui.theme.AutoMekanikoTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@SuppressLint("MissingPermission")
class OBDActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "OBDActivity"
        private val RFCOMM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        private val SERVICE_UUIDS = listOf(
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
            UUID.fromString("000018f0-0000-1000-8000-00805f9b34fb")
        )
        private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val PID_NEVER_BLACKLIST = setOf(
            "010C", "010D", "0105", "0111", "0104",
            "010F", "0110", "010A", "010B", "0114",
            "0115", "0100", "0120", "0140", "011F"
        )
    }

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private var writeChar: BluetoothGattCharacteristic? = null
    private var notifyChar: BluetoothGattCharacteristic? = null

    private var isConnected = false
    private var isInitialized = false

    private val responseChannel = Channel<String>(Channel.UNLIMITED)
    private var pollJob: Job? = null
    private var classicConnectJob: Job? = null

    private val unsupportedPids = mutableSetOf<String>()
    private var extendedCycle = 0

    // Compose Reactive State
    private val isBleConnectedState = mutableStateOf(false)
    private val statusTextState = mutableStateOf("Disconnected")
    private val isDarkThemeState = mutableStateOf(false)

    // Sensor readings map for Compose UI
    private val sensorReadingsMap = mutableStateMapOf<Int, String>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            showDeviceSelectionDialog()
        } else {
            Toast.makeText(this, "Bluetooth permissions required for OBD2 scanning", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bm.adapter

        isDarkThemeState.value = isDarkTheme(this)

        setContent {
            AutoMekanikoTheme(darkTheme = isDarkThemeState.value) {
                ObdScreen(
                    sensorValues = sensorReadingsMap,
                    isConnected = isBleConnectedState.value,
                    statusMessage = statusTextState.value,
                    isMetric = true,
                    rpmRedline = 6500,
                    onConnectClick = { onConnectButtonClicked() },
                    onHomeClick = { goHome() },
                    onSettingsClick = { goSettings() },
                    onFuelLevelLongClick = { }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isDarkThemeState.value = isDarkTheme(this)
        TutorialManager.checkAndRenderStepOnResume(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        pollJob?.cancel()
        classicConnectJob?.cancel()
        disconnect()
    }

    private fun setStatus(msg: String) {
        statusTextState.value = msg
    }

    private fun setBleConnected(connected: Boolean) {
        isBleConnectedState.value = connected
    }

    private fun updateSensor(cardId: Int, valStr: String) {
        if (valStr.isNotBlank()) {
            sensorReadingsMap[cardId] = valStr
        }
    }

    private fun onConnectButtonClicked() {
        if (isConnected) {
            disconnect()
            onDisconnected()
        } else {
            checkPermissionsAndConnect()
        }
    }

    private fun onDisconnected() {
        isConnected = false
        isInitialized = false
        setBleConnected(false)
        setStatus("Disconnected")
    }

    private fun checkPermissionsAndConnect() {
        val required = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                required.add(Manifest.permission.BLUETOOTH_SCAN)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                required.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            required.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (required.isNotEmpty()) {
            requestPermissionLauncher.launch(required.toTypedArray())
        } else {
            showDeviceSelectionDialog()
        }
    }

    private fun hasBleConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun showDeviceSelectionDialog() {
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show()
            return
        }

        val paired = bluetoothAdapter.bondedDevices.toList()
        if (paired.isEmpty()) {
            Toast.makeText(this, "No paired Bluetooth devices found. Please pair your OBD2 adapter in Bluetooth settings.", Toast.LENGTH_LONG).show()
            return
        }

        val names = paired.map { "${it.name ?: "Unknown"} (${it.address})" }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Select OBD2 Adapter")
            .setItems(names) { _, which ->
                val device = paired[which]
                connectToDevice(device)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        disconnect()

        try {
            if (device.type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                runOnUiThread { setStatus("Connecting (Classic)…") }
                connectClassic(device)
            } else {
                runOnUiThread { setStatus("Connecting (BLE)…") }
                bluetoothGatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "connectToDevice: ${e.message}")
            runOnUiThread { setStatus("Connection error: ${e.message}") }
        }
    }

    private fun connectClassic(device: BluetoothDevice) {
        classicConnectJob?.cancel()
        classicConnectJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(RFCOMM_UUID)
                bluetoothSocket?.connect()
                inputStream  = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream
                isConnected  = true
                runOnUiThread { setBleConnected(true); setStatus("Connected! Initializing…") }
                delay(100) // Fast 100ms connection delay
                initElm327()
                startPollLoop()
            } catch (e: Exception) {
                Log.e(TAG, "Classic failed: ${e.message}")
                isConnected = false
                runOnUiThread { setStatus("Connection failed: ${e.message}"); onDisconnected() }
                disconnectClassic()
            }
        }
    }

    private fun disconnectClassic() {
        try { inputStream?.close(); outputStream?.close(); bluetoothSocket?.close() } catch (_: Exception) {}
        inputStream = null; outputStream = null; bluetoothSocket = null
    }

    private fun disconnect() {
        pollJob?.cancel()
        classicConnectJob?.cancel()
        disconnectClassic()

        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (_: Exception) {}
        bluetoothGatt = null

        isConnected = false
        isInitialized = false
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when {
                newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "GATT connected")
                    runOnUiThread { setStatus("Connected! Loading services…") }
                    if (hasBleConnectPermission()) gatt.discoverServices()
                }
                newState == BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "GATT disconnected status=$status")
                    isConnected = false; isInitialized = false; pollJob?.cancel()
                    runOnUiThread { onDisconnected() }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                runOnUiThread { setStatus("Service discovery failed") }; gatt.disconnect(); return
            }
            var svc = SERVICE_UUIDS.firstNotNullOfOrNull { gatt.getService(it) }
            if (svc == null) svc = gatt.services.find { s ->
                s.characteristics.any { (it.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 } &&
                        s.characteristics.any { (it.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0 }
            }
            if (svc == null) { runOnUiThread { setStatus("No compatible BLE service") }; gatt.disconnect(); return }

            notifyChar = svc.characteristics.find { (it.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 }
            writeChar  = svc.characteristics.find { (it.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0 }

            if (notifyChar == null || writeChar == null) {
                runOnUiThread { setStatus("Data channels not found") }; gatt.disconnect(); return
            }

            gatt.setCharacteristicNotification(notifyChar!!, true)
            val cccd = notifyChar!!.getDescriptor(CCCD_UUID)
            if (cccd != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    gatt.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                else { cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE; gatt.writeDescriptor(cccd) }
            } else {
                isConnected = true
                runOnUiThread { setBleConnected(true) }
                lifecycleScope.launch(Dispatchers.IO) { delay(100); initElm327(); startPollLoop() }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && descriptor.uuid == CCCD_UUID) {
                isConnected = true
                runOnUiThread { setBleConnected(true) }
                lifecycleScope.launch(Dispatchers.IO) { delay(100); initElm327(); startPollLoop() }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, c: BluetoothGattCharacteristic, v: ByteArray) {
            responseChannel.trySend(String(v, Charsets.UTF_8))
        }
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, c: BluetoothGattCharacteristic) {
            responseChannel.trySend(String(c.value ?: return, Charsets.UTF_8))
        }
    }

    private fun sendRaw(cmd: String) {
        try {
            if (outputStream != null) {
                outputStream!!.write((cmd + "\r").toByteArray(Charsets.UTF_8))
                outputStream!!.flush()
                Log.d(TAG, "TX(classic): $cmd")
                return
            }
            val c = writeChar ?: return
            val b = (cmd + "\r").toByteArray()
            val type = if ((c.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0)
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE else BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                bluetoothGatt?.writeCharacteristic(c, b, type)
            else { c.writeType = type; c.value = b; bluetoothGatt?.writeCharacteristic(c) }
        } catch (e: Exception) { Log.e(TAG, "sendRaw: ${e.message}") }
    }

    private suspend fun send(cmd: String, timeout: Long = 500): String {
        if (!isConnected) return ""

        if (outputStream != null && inputStream != null) {
            return try {
                sendRaw(cmd)
                val buf = ByteArray(4096); val sb = StringBuilder()
                val t0 = System.currentTimeMillis()
                while (System.currentTimeMillis() - t0 < timeout) {
                    val avail = inputStream!!.available()
                    if (avail > 0) {
                        val n = inputStream!!.read(buf, 0, minOf(avail, buf.size))
                        if (n > 0) {
                            sb.append(String(buf, 0, n, Charsets.UTF_8))
                            if (sb.contains(">")) break
                        }
                    } else delay(1) // Fast 1ms socket check
                }
                sb.toString().trim().also { Log.d(TAG, "CMD=$cmd RSP=${it.take(120)}") }
            } catch (e: Exception) { Log.e(TAG, "send '$cmd': ${e.message}"); "" }
        }

        while (responseChannel.tryReceive().isSuccess) {}
        withContext(Dispatchers.Main) { sendRaw(cmd) }
        val sb = StringBuilder(); val end = System.currentTimeMillis() + timeout
        while (System.currentTimeMillis() < end) {
            val chunk = withTimeoutOrNull(end - System.currentTimeMillis()) { responseChannel.receive() } ?: break
            sb.append(chunk); if (sb.contains(">")) break
        }
        return sb.toString().trim().also { if (it.isEmpty()) Log.w(TAG, "Timeout: $cmd") }
    }

    private suspend fun initElm327() {
        runOnUiThread { setStatus("Initializing ELM327…") }
        unsupportedPids.clear()
        for (cmd in listOf("ATZ", "ATE0", "ATL0", "ATS0", "ATH0", "ATSP0", "ATAT1")) {
            send(cmd, if (cmd == "ATZ") 1200 else 500)
            delay(30) // Fast 30ms inter-command delay
        }
        isInitialized = true
        runOnUiThread { setStatus("Polling Live Data…") }
    }

    private fun startPollLoop() {
        pollJob?.cancel()
        extendedCycle = 0
        pollJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive && isConnected && isInitialized) {
                try {
                    // Fast 300ms max timeout per core PID query
                    val rpm  = parseRPM(send("010C", 300))
                    val spd  = parseSpeed(send("010D", 300))
                    val cool = parseTemp(send("0105", 300), "05")
                    val thr  = parsePerc(send("0111", 300), "11")
                    val load = parsePerc(send("0104", 300), "04")

                    runOnUiThread {
                        updateSensor(R.id.cardRpm, rpm)
                        updateSensor(R.id.cardSpeed, spd)
                        updateSensor(R.id.cardCoolant, cool)
                        updateSensor(R.id.cardThrottle, thr)
                        updateSensor(R.id.cardLoad, load)
                    }

                    if (extendedCycle++ % 10 == 0) pollExtended()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Poll error: ${e.message}"); delay(100)
                }
            }
        }
    }

    private suspend fun pollExtended() {
        val extTimeout = 500L

        suspend fun q(pid: String): String {
            if (pid !in PID_NEVER_BLACKLIST && pid in unsupportedPids) return ""
            val r = send(pid, extTimeout)
            if (r.isBlank()) return ""
            if (r.contains("NO DATA", ignoreCase = true)) {
                if (pid !in PID_NEVER_BLACKLIST) unsupportedPids.add(pid)
                Log.d(TAG, "NO DATA pid=$pid")
                return ""
            }
            if (r.contains("ERROR", ignoreCase = true) ||
                r.contains("UNABLE", ignoreCase = true) ||
                r.contains("STOPPED", ignoreCase = true)
            ) return ""
            return r
        }

        val mafRsp   = q("0110")
        val maf      = parseMaf(mafRsp)
        val iat      = parseTemp(q("010F"), "0F")
        val o2s1     = parseO2(q("0114"), "14")
        val o2s2     = parseO2(q("0115"), "15")

        val loadRsp  = q("0104")
        val calcLoad = parsePerc(loadRsp, "04")

        runOnUiThread {
            updateSensor(R.id.cardMaf, maf)
            updateSensor(R.id.cardIat, iat)
            updateSensor(R.id.cardO2s1, o2s1)
            updateSensor(R.id.cardO2s2, o2s2)
            if (calcLoad.isNotBlank()) updateSensor(R.id.cardLoad, calcLoad)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  OBD PID Parsers
    // ─────────────────────────────────────────────────────────────────────────
    private fun parseRPM(raw: String): String {
        val bytes = hexBytes(raw, "41 0C") ?: return ""
        if (bytes.size < 2) return ""
        val rpm = ((bytes[0] and 0xFF) * 256 + (bytes[1] and 0xFF)) / 4
        return "$rpm RPM"
    }

    private fun parseSpeed(raw: String): String {
        val bytes = hexBytes(raw, "41 0D") ?: return ""
        if (bytes.isEmpty()) return ""
        val kmh = bytes[0] and 0xFF
        return "$kmh km/h"
    }

    private fun parseTemp(raw: String, pidHex: String): String {
        val bytes = hexBytes(raw, "41 $pidHex") ?: return ""
        if (bytes.isEmpty()) return ""
        val c = (bytes[0] and 0xFF) - 40
        return "$c°C"
    }

    private fun parsePerc(raw: String, pidHex: String): String {
        val bytes = hexBytes(raw, "41 $pidHex") ?: return ""
        if (bytes.isEmpty()) return ""
        val pct = (bytes[0] and 0xFF) * 100 / 255
        return "$pct%"
    }

    private fun parseMaf(raw: String): String {
        val bytes = hexBytes(raw, "41 10") ?: return ""
        if (bytes.size < 2) return ""
        val grams = ((bytes[0] and 0xFF) * 256 + (bytes[1] and 0xFF)) / 100f
        return String.format(Locale.US, "%.1f g/s", grams)
    }

    private fun parseO2(raw: String, pidHex: String): String {
        val bytes = hexBytes(raw, "41 $pidHex") ?: return ""
        if (bytes.isEmpty()) return ""
        val volts = (bytes[0] and 0xFF) / 200f
        return String.format(Locale.US, "%.2f V", volts)
    }

    private fun hexBytes(raw: String, header: String): IntArray? {
        val clean = raw.replace("\r", " ").replace("\n", " ").uppercase()
        val idx = clean.indexOf(header)
        if (idx == -1) return null
        val after = clean.substring(idx + header.length).trim()
        val tokens = after.split("\\s+".toRegex()).takeWhile { it.length == 2 && it.all { c -> c in "0123456789ABCDEF" } }
        if (tokens.isEmpty()) return null
        return tokens.map { it.toInt(16) }.toIntArray()
    }

    private fun isDarkTheme(context: Context): Boolean {
        val prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        return when (prefs.getInt(SettingsActivity.KEY_THEME, SettingsActivity.THEME_SYSTEM)) {
            SettingsActivity.THEME_LIGHT -> false
            SettingsActivity.THEME_DARK -> true
            else -> {
                val uiMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
        ViewAnimationUtils.overrideActivityTransition(this, isEntering = false)
    }

    private fun goSettings() {
        startActivity(Intent(this, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
        ViewAnimationUtils.overrideActivityTransition(this, isEntering = true)
    }
}
