package com.example.automekaniko

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.channels.Channel
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
//  ELM327 BLE UUIDs  (standard SPP-over-BLE / ELM327 BLE adapters)
//  Some adapters use different UUIDs — edit these two constants if your device
//  shows "NO DATA" immediately after init.
// ─────────────────────────────────────────────────────────────────────────────
private val SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
private val WRITE_UUID   = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
private val NOTIFY_UUID  = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")

private val CCCD_UUID    = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

private const val TAG = "OBDActivity"

/**
 * OBDActivity
 *
 * Handles:
 *  1. BLE scan + connect to ELM327 adapter (no bridge.py needed on Android).
 *  2. ELM327 AT init sequence.
 *  3. Continuous polling of OBD-II PIDs and display in the card grid.
 *
 * Layout: interactive_view_activity.xml
 */
class OBDActivity : AppCompatActivity() {

    // ── Views ─────────────────────────────────────────────────────────────────
    private lateinit var tvBleStatus: TextView
    private lateinit var tvStatusMsg: TextView
    private lateinit var btnConnect: Button

    // Bottom-bar quick-tiles
    private lateinit var speedVal:    TextView
    private lateinit var rpmVal:      TextView
    private lateinit var coolantVal:  TextView
    private lateinit var throttleVal: TextView
    private lateinit var loadVal:     TextView

    // Sensor card value TextViews (resolved after <include> bind)
    private lateinit var vMaf:        TextView
    private lateinit var vIat:        TextView
    private lateinit var vO2s1:       TextView
    private lateinit var vO2s2:       TextView
    private lateinit var vMap:        TextView
    private lateinit var vFuelLevel:  TextView
    private lateinit var vStft:       TextView
    private lateinit var vLtft:       TextView
    private lateinit var vTiming:     TextView
    private lateinit var vBaro:       TextView
    private lateinit var vCat1:       TextView
    private lateinit var vCat2:       TextView
    private lateinit var vModVoltage: TextView
    private lateinit var vFuelRate:   TextView

    // ── BLE state ─────────────────────────────────────────────────────────────
    private var bluetoothGatt: BluetoothGatt? = null
    private var writeChar:  BluetoothGattCharacteristic? = null
    private var notifyChar: BluetoothGattCharacteristic? = null

    private var isConnected   = false
    private var isInitialized = false

    /** Unlimited channel: BLE notify pushes here, coroutine reads. */
    private val responseChannel = Channel<String>(capacity = Channel.UNLIMITED)

    private var pollJob: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // ─────────────────────────────────────────────────────────────────────────
    //  Permission launcher
    // ─────────────────────────────────────────────────────────────────────────
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.all { it.value }) startBleScan()
        else toast("Bluetooth permissions are required to connect to OBD adapter")
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Lifecycle
    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.interactive_view_activity)

        bindViews()
        setupCardLabels()
        btnConnect.setOnClickListener { onConnectClicked() }
    }

    override fun onDestroy() {
        pollJob?.cancel()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            bluetoothGatt?.close()
        }
        bluetoothGatt = null
        super.onDestroy()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  View binding
    // ─────────────────────────────────────────────────────────────────────────
    private fun bindViews() {
        tvBleStatus  = findViewById(R.id.tvBleStatus)
        tvStatusMsg  = findViewById(R.id.tvStatusMsg)
        btnConnect   = findViewById(R.id.btnConnect)

        speedVal    = findViewById(R.id.speedVal)
        rpmVal      = findViewById(R.id.rpmVal)
        coolantVal  = findViewById(R.id.coolantVal)
        throttleVal = findViewById(R.id.throttleVal)
        loadVal     = findViewById(R.id.loadVal)

        // Each <include> card exposes its children via the include id → find child
        vMaf        = cardValue(R.id.cardMaf)
        vIat        = cardValue(R.id.cardIat)
        vO2s1       = cardValue(R.id.cardO2s1)
        vO2s2       = cardValue(R.id.cardO2s2)
        vMap        = cardValue(R.id.cardMap)
        vFuelLevel  = cardValue(R.id.cardFuelLevel)
        vStft       = cardValue(R.id.cardStft)
        vLtft       = cardValue(R.id.cardLtft)
        vTiming     = cardValue(R.id.cardTiming)
        vBaro       = cardValue(R.id.cardBaro)
        vCat1       = cardValue(R.id.cardCat1)
        vCat2       = cardValue(R.id.cardCat2)
        vModVoltage = cardValue(R.id.cardModVoltage)
        vFuelRate   = cardValue(R.id.cardFuelRate)
    }

    /** Helper: finds the cardValue TextView inside an included card view. */
    private fun cardValue(includeId: Int): TextView =
        (findViewById<View>(includeId)).findViewById(R.id.cardValue)

    private fun cardLabel(includeId: Int): TextView =
        (findViewById<View>(includeId)).findViewById(R.id.cardLabel)

    private fun cardUnit(includeId: Int): TextView =
        (findViewById<View>(includeId)).findViewById(R.id.cardUnit)

    /** Populate the static label + unit text for each sensor card. */
    private fun setupCardLabels() {
        data class CardMeta(val id: Int, val label: String, val unit: String)

        val cards = listOf(
            CardMeta(R.id.cardMaf,        "Mass Air Flow",          "g/s"),
            CardMeta(R.id.cardIat,        "Intake Air Temp",        "°C"),
            CardMeta(R.id.cardO2s1,       "O2 Sensor 1",            "V"),
            CardMeta(R.id.cardO2s2,       "O2 Sensor 2",            "V"),
            CardMeta(R.id.cardMap,        "MAP",                    "kPa"),
            CardMeta(R.id.cardFuelLevel,  "Fuel Level",             "%"),
            CardMeta(R.id.cardStft,       "Short Fuel Trim B1",     "%"),
            CardMeta(R.id.cardLtft,       "Long Fuel Trim B1",      "%"),
            CardMeta(R.id.cardTiming,     "Timing Advance",         "°"),
            CardMeta(R.id.cardBaro,       "Barometric Pressure",    "kPa"),
            CardMeta(R.id.cardCat1,       "Catalyst Temp B1S1",     "°C"),
            CardMeta(R.id.cardCat2,       "Catalyst Temp B1S2",     "°C"),
            CardMeta(R.id.cardModVoltage, "Control Module Voltage", "V"),
            CardMeta(R.id.cardFuelRate,   "Fuel Rate",              "L/h"),
        )
        cards.forEach { c ->
            cardLabel(c.id).text = c.label
            cardUnit(c.id).text  = c.unit
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Connect / disconnect button
    // ─────────────────────────────────────────────────────────────────────────
    private fun onConnectClicked() {
        if (isConnected) disconnectAndReset()
        else requestPermissionsAndScan()
    }

    private fun requestPermissionsAndScan() {
        val needed = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN))
                needed += Manifest.permission.BLUETOOTH_SCAN
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
                needed += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            if (!hasPermission(Manifest.permission.BLUETOOTH))
                needed += Manifest.permission.BLUETOOTH
            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
                needed += Manifest.permission.ACCESS_FINE_LOCATION
        }

        if (needed.isEmpty()) startBleScan()
        else permLauncher.launch(needed.toTypedArray())
    }

    private fun hasPermission(p: String) =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

    // ─────────────────────────────────────────────────────────────────────────
    //  BLE scan
    // ─────────────────────────────────────────────────────────────────────────
    private fun startBleScan() {
        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bm.adapter
        if (adapter == null || !adapter.isEnabled) {
            toast("Please enable Bluetooth first"); return
        }

        setStatus("Scanning for ELM327 adapter…")
        btnConnect.isEnabled = false

        val scanner = adapter.bluetoothLeScanner
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // Guard BLUETOOTH_CONNECT before getDevice().name on API 31+
                val name: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
                        result.device.name ?: ""
                    else ""
                } else {
                    result.device.name ?: ""
                }

                val nameUpper = name.uppercase()
                if (nameUpper.contains("OBD") ||
                    nameUpper.contains("ELM") ||
                    nameUpper.contains("VLINK") ||
                    nameUpper.contains("SCAN")) {

                    // Guard BLUETOOTH_SCAN before stopScan on API 31+
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                        hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                        scanner.stopScan(this)
                    }
                    Log.d(TAG, "Found OBD device: $name")
                    setStatus("Found $name — connecting…")
                    connectToDevice(result.device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                setStatus("Scan failed (code $errorCode)")
                btnConnect.isEnabled = true
            }
        }

        // Guard BLUETOOTH_SCAN before startScan on API 31+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            scanner.startScan(null, settings, callback)
        } else {
            toast("BLUETOOTH_SCAN permission missing")
            btnConnect.isEnabled = true
            return
        }

        // Auto-stop scan after 15 s if nothing found
        mainHandler.postDelayed({
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                try { scanner.stopScan(callback) } catch (_: Exception) {}
            }
            if (!isConnected) {
                setStatus("No OBD adapter found. Make sure it's plugged in and BLE is on.")
                btnConnect.isEnabled = true
            }
        }, 15_000)
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GATT connection
    // ─────────────────────────────────────────────────────────────────────────
    private fun connectToDevice(device: BluetoothDevice) {
        // Guard BLUETOOTH_CONNECT before connectGatt on API 31+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            toast("Missing BLUETOOTH_CONNECT permission")
            btnConnect.isEnabled = true
            return
        }
        bluetoothGatt = device.connectGatt(
            this, false, gattCallback, BluetoothDevice.TRANSPORT_LE
        )
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "GATT connected — discovering services")
                    // Guard BLUETOOTH_CONNECT before discoverServices on API 31+
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                        hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                        gatt.discoverServices()
                    } else {
                        runOnUiThread { setStatus("Missing BLUETOOTH_CONNECT permission") }
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "GATT disconnected")
                    isConnected   = false
                    isInitialized = false
                    pollJob?.cancel()
                    runOnUiThread { onDisconnected() }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                runOnUiThread { setStatus("Service discovery failed") }
                return
            }

            // Guard BLUETOOTH_CONNECT before all gatt operations on API 31+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                runOnUiThread { setStatus("Missing BLUETOOTH_CONNECT permission") }
                return
            }

            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                Log.w(TAG, "Primary service not found — check adapter UUID constants")
                runOnUiThread {
                    setStatus("ELM327 service UUID not found. Check adapter UUID constants.")
                }
                return
            }

            writeChar  = service.getCharacteristic(WRITE_UUID)
            notifyChar = service.getCharacteristic(NOTIFY_UUID)

            if (notifyChar == null || writeChar == null) {
                runOnUiThread { setStatus("ELM327 characteristics not found") }
                return
            }

            // Enable notifications
            gatt.setCharacteristicNotification(notifyChar, true)

            val desc = notifyChar!!.getDescriptor(CCCD_UUID)
            desc?.let {
                // Use new non-deprecated overload on API 33+; fall back for older
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(it)
                }
            }

            isConnected = true
            runOnUiThread {
                setBleConnected(true)
                btnConnect.isEnabled = true
            }

            // Kick off init + poll on IO coroutine
            lifecycleScope.launch(Dispatchers.IO) {
                delay(600)          // give descriptor write time to settle
                initElm327()
                startPollLoop()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            val raw = characteristic.value?.toString(Charsets.UTF_8) ?: return
            Log.v(TAG, "RX: ${raw.trim()}")
            responseChannel.trySend(raw)
        }

        // New overload required for Android 13+
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            val raw = value.toString(Charsets.UTF_8)
            Log.v(TAG, "RX13: ${raw.trim()}")
            responseChannel.trySend(raw)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ELM327 command helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Send a raw string + CR to the ELM327. */
    private fun sendRaw(cmd: String) {
        val char = writeChar ?: return

        // Guard BLUETOOTH_CONNECT before writeCharacteristic on API 31+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return

        val bytes = (cmd + "\r").toByteArray(Charsets.UTF_8)

        // Use new non-deprecated overload on API 33+; fall back for older
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt?.writeCharacteristic(
                char,
                bytes,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        } else {
            @Suppress("DEPRECATION")
            char.value = bytes
            @Suppress("DEPRECATION")
            bluetoothGatt?.writeCharacteristic(char)
        }
    }

    /**
     * Send cmd and wait for a response terminated by '>'.
     * Reassembles chunked BLE notifications.
     * Returns "" on timeout or disconnect.
     */
    private suspend fun send(cmd: String, timeoutMs: Long = 3000): String {
        if (!isConnected) return ""
        // Drain any stale responses
        while (responseChannel.tryReceive().isSuccess) { /* discard */ }

        withContext(Dispatchers.Main) { sendRaw(cmd) }

        val buffer = StringBuilder()
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val remaining = deadline - System.currentTimeMillis()
            val chunk = withTimeoutOrNull(remaining) {
                responseChannel.receive()
            } ?: break
            buffer.append(chunk)
            if (buffer.contains('>')) break
        }
        val result = buffer.toString().trim()
        Log.d(TAG, "CMD: $cmd  RSP: $result")
        return result
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ELM327 init sequence
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun initElm327() {
        runOnUiThread { setStatus("Initializing ELM327…") }
        send("ATZ");     delay(2000)
        send("ATE0");    delay(400)
        send("ATL0");    delay(400)
        send("ATS0");    delay(400)
        send("ATH1");    delay(400)
        send("ATAT2");   delay(400)
        send("ATST FF"); delay(400)
        send("ATSP0");   delay(800)

        val supported = send("0100")
        Log.d(TAG, "Supported PIDs: $supported")

        isInitialized = true
        runOnUiThread { setStatus("ELM327 ready — polling live data…") }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Continuous poll loop
    // ─────────────────────────────────────────────────────────────────────────
    private fun startPollLoop() {
        pollJob?.cancel()
        pollJob = lifecycleScope.launch(Dispatchers.IO) {
            var tick = 0
            while (isActive && isConnected && isInitialized) {
                try {
                    // ── Primary PIDs (every cycle) ───────────────────────────
                    val rpm = parseRPM(send("010C"))
                    val spd = parseSpeed(send("010D"))
                    val tmp = parseTemp(send("0105"))
                    val thr = parsePercent(send("0111"), "11")
                    val ld  = parsePercent(send("0104"), "04")

                    runOnUiThread {
                        rpm?.let { rpmVal.text      = it }
                        spd?.let { speedVal.text    = it }
                        tmp?.let { coolantVal.text  = it }
                        thr?.let { throttleVal.text = it }
                        ld?.let  { loadVal.text     = it }
                    }

                    // ── Extended PIDs (every 4th cycle ≈ every ~1.2 s) ──────
                    tick++
                    if (tick % 4 == 0) pollExtended()

                    delay(300)

                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Poll error: ${e.message}")
                    delay(1000)
                }
            }
        }
    }

    private suspend fun pollExtended() {
        runOnUiThread { setStatus("Updating extended sensors…") }

        val maf  = parseMAF(send("0110"))
        val stft = parseSignedPercent(send("0106"), "06")
        val ltft = parseSignedPercent(send("0107"), "07")
        val iat  = parseTemp(send("010F"))
        val o2s1 = parseO2Voltage(send("0114"), "14")
        val o2s2 = parseO2Voltage(send("0115"), "15")
        val cat1 = parseCatalystTemp(send("013C"), "3C")
        val cat2 = parseCatalystTemp(send("013D"), "3D")
        val modV = parseModuleVoltage(send("0142"))
        val frat = parseFuelRate(send("015E"))
        val map  = parsePressure(send("010B"), "0B")
        val baro = parsePressure(send("0133"), "33")
        val fuel = parsePercent(send("012F"), "2F")
        val tim  = parseTimingAdvance(send("010E"))

        runOnUiThread {
            maf?.let  { vMaf.text        = it }
            stft?.let { vStft.text       = it }
            ltft?.let { vLtft.text       = it }
            iat?.let  { vIat.text        = it }
            o2s1?.let { vO2s1.text       = it }
            o2s2?.let { vO2s2.text       = it }
            cat1?.let { vCat1.text       = it }
            cat2?.let { vCat2.text       = it }
            modV?.let { vModVoltage.text = it }
            frat?.let { vFuelRate.text   = it }
            map?.let  { vMap.text        = it }
            baro?.let { vBaro.text       = it }
            fuel?.let { vFuelLevel.text  = it }
            tim?.let  { vTiming.text     = it }
            setStatus("Live data polling…")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  OBD-II response parsers
    // ─────────────────────────────────────────────────────────────────────────

    /** Strip header bytes and return clean byte list. */
    private fun getBytes(raw: String): List<String> {
        var cleaned = raw.replace(Regex("\\s+"), "").uppercase()
        if (cleaned.startsWith("7E8") || cleaned.startsWith("7E9"))
            cleaned = cleaned.substring(3)
        val bytes = mutableListOf<String>()
        var i = 0
        while (i < cleaned.length - 1) {
            val b = cleaned.substring(i, i + 2)
            if (b.matches(Regex("[0-9A-F]{2}"))) bytes.add(b)
            i += 2
        }
        return bytes
    }

    private fun isError(raw: String) =
        raw.isBlank() || raw.contains("NO DATA") || raw.contains("TIMEOUT") ||
                raw.contains("ERROR") || raw.contains("?")

    private fun parseRPM(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 4) {
            if (b[i] == "41" && b[i+1] == "0C") {
                val a = b[i+2].toInt(16); val bv = b[i+3].toInt(16)
                return ((a * 256 + bv) / 4).toString()
            }
        }
        return null
    }

    private fun parseSpeed(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 3) {
            if (b[i] == "41" && b[i+1] == "0D") return b[i+2].toInt(16).toString()
        }
        return null
    }

    private fun parseTemp(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 3) {
            if (b[i] == "41") return (b[i+2].toInt(16) - 40).toString()
        }
        return null
    }

    private fun parsePercent(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        val p = pid.uppercase()
        for (i in 0..b.size - 3) {
            if (b[i] == "41" && b[i+1] == p)
                return "%.1f".format((b[i+2].toInt(16) / 255.0) * 100)
        }
        return null
    }

    private fun parseSignedPercent(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        val p = pid.uppercase()
        for (i in 0..b.size - 3) {
            if (b[i] == "41" && b[i+1] == p) {
                val v = ((b[i+2].toInt(16) - 128) * 100.0) / 128.0
                return "%.1f".format(v)
            }
        }
        return null
    }

    private fun parseMAF(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 4) {
            if (b[i] == "41" && b[i+1] == "10") {
                val v = (b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 100.0
                return "%.2f".format(v)
            }
        }
        return null
    }

    private fun parsePressure(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        val p = pid.uppercase()
        for (i in 0..b.size - 3) {
            if (b[i] == "41" && b[i+1] == p) return b[i+2].toInt(16).toString()
        }
        return null
    }

    private fun parseTimingAdvance(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 3) {
            if (b[i] == "41" && b[i+1] == "0E") {
                val v = (b[i+2].toInt(16) / 2.0) - 64.0
                return "%.1f".format(v)
            }
        }
        return null
    }

    private fun parseO2Voltage(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        val p = pid.uppercase()
        for (i in 0..b.size - 3) {
            if (b[i] == "41" && b[i+1] == p)
                return "%.2f".format(b[i+2].toInt(16) / 200.0)
        }
        return null
    }

    private fun parseCatalystTemp(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        val p = pid.uppercase()
        for (i in 0..b.size - 4) {
            if (b[i] == "41" && b[i+1] == p) {
                val v = (b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 10.0 - 40.0
                return "%.1f".format(v)
            }
        }
        return null
    }

    private fun parseModuleVoltage(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 4) {
            if (b[i] == "41" && b[i+1] == "42") {
                val v = (b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 1000.0
                return "%.2f".format(v)
            }
        }
        return null
    }

    private fun parseFuelRate(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 4) {
            if (b[i] == "41" && b[i+1] == "5E") {
                val v = (b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 20.0
                return "%.2f".format(v)
            }
        }
        return null
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun setStatus(msg: String) { tvStatusMsg.text = msg }

    private fun setBleConnected(connected: Boolean) {
        if (connected) {
            tvBleStatus.text     = "● CONNECTED"
            tvBleStatus.setTextColor(0xFF00E676.toInt())
            btnConnect.text      = "DISCONNECT"
        } else {
            tvBleStatus.text     = "● DISCONNECTED"
            tvBleStatus.setTextColor(0xFFFF5555.toInt())
            btnConnect.text      = "CONNECT"
            btnConnect.isEnabled = true
        }
    }

    private fun onDisconnected() {
        setBleConnected(false)
        setStatus("Disconnected. Tap Connect to reconnect.")
        resetAllValues()
    }

    private fun disconnectAndReset() {
        pollJob?.cancel()
        // Guard BLUETOOTH_CONNECT before disconnect/close on API 31+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        }
        bluetoothGatt  = null
        isConnected    = false
        isInitialized  = false
        onDisconnected()
    }

    private fun resetAllValues() {
        listOf(
            speedVal, rpmVal, coolantVal, throttleVal, loadVal,
            vMaf, vIat, vO2s1, vO2s2, vMap, vFuelLevel,
            vStft, vLtft, vTiming, vBaro, vCat1, vCat2,
            vModVoltage, vFuelRate
        ).forEach { it.text = "—" }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}