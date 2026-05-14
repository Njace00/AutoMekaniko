package com.example.automekaniko

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
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
//  ELM327 BLE UUIDs
// ─────────────────────────────────────────────────────────────────────────────
private val SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
private val WRITE_UUID   = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
private val NOTIFY_UUID  = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
private val CCCD_UUID    = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

private const val TAG = "OBDActivity"

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    // ── Views ─────────────────────────────────────────────────────────────────
    private lateinit var tvBleStatus: TextView
    private lateinit var tvStatusMsg: TextView
    private lateinit var btnConnect:  Button

    // Tab views
    private lateinit var tab3D:  TextView
    private lateinit var tabOBD: TextView

    // Bottom-bar quick-tiles
    private lateinit var speedVal:    TextView
    private lateinit var rpmVal:      TextView
    private lateinit var coolantVal:  TextView
    private lateinit var throttleVal: TextView
    private lateinit var loadVal:     TextView

    // Sensor card value TextViews
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
    private var writeChar:     BluetoothGattCharacteristic? = null
    private var notifyChar:    BluetoothGattCharacteristic? = null

    private var isConnected   = false
    private var isInitialized = false

    private val responseChannel = Channel<String>(capacity = Channel.UNLIMITED)

    private var pollJob: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // ─────────────────────────────────────────────────────────────────────────
    //  Permission launcher
    // ─────────────────────────────────────────────────────────────────────────


    // ─────────────────────────────────────────────────────────────────────────
    //  Lifecycle
    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupCardLabels()
        setupTabs()          // ← wire up tab clicks
        btnConnect.setOnClickListener { onConnectClicked() }
    }

    override fun onDestroy() {
        pollJob?.cancel()
        if (hasBleConnectPermission()) bluetoothGatt?.close()
        bluetoothGatt = null
        super.onDestroy()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Tab navigation
    //  • "3D View"  tab → go back to MainActivity (finish this activity)
    //  • "OBD Data" tab → already here, do nothing
    // ─────────────────────────────────────────────────────────────────────────
    private fun setupTabs() {
        setTabActive(tab3D,  active = false)
        setTabActive(tabOBD, active = true)

        tab3D.setOnClickListener {
            // Go back to the 3D view screen
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        tabOBD.setOnClickListener {
            // Already on this screen — nothing to do
        }
    }

    /** Styles a tab TextView as active (gold pill) or inactive (transparent). */
    private fun setTabActive(tab: TextView, active: Boolean) {
        if (active) {
            tab.setTextColor(0xFF000000.toInt())
            tab.setBackgroundColor(0xFFFFD700.toInt())
            tab.setTypeface(null, Typeface.BOLD)
        } else {
            tab.setTextColor(0xFFFFD700.toInt())
            tab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tab.setTypeface(null, Typeface.NORMAL)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  View binding
    // ─────────────────────────────────────────────────────────────────────────
    private fun bindViews() {
        // The tvBleStatus TextView must exist in activity_diagnostic_data.xml.
        // If it doesn't yet, add it — see the comment block at the bottom of
        // this file for the XML snippet to paste into your connectRow.
        tvBleStatus  = findViewById(R.id.tvBleStatus)
        tvStatusMsg  = findViewById(R.id.tvStatusMsg)
        btnConnect   = findViewById(R.id.btnConnect)

        tab3D  = findViewById(R.id.tab3D)
        tabOBD = findViewById(R.id.tabOBD)

        speedVal     = findViewById(R.id.speedVal)
        rpmVal       = findViewById(R.id.rpmVal)
        coolantVal   = findViewById(R.id.coolantVal)
        throttleVal  = findViewById(R.id.throttleVal)
        loadVal      = findViewById(R.id.loadVal)

        vMaf         = cardValue(R.id.cardMaf)
        vIat         = cardValue(R.id.cardIat)
        vO2s1        = cardValue(R.id.cardO2s1)
        vO2s2        = cardValue(R.id.cardO2s2)
        vMap         = cardValue(R.id.cardMap)
        vFuelLevel   = cardValue(R.id.cardFuelLevel)
        vStft        = cardValue(R.id.cardStft)
        vLtft        = cardValue(R.id.cardLtft)
        vTiming      = cardValue(R.id.cardTiming)
        vBaro        = cardValue(R.id.cardBaro)
        vCat1        = cardValue(R.id.cardCat1)
        vCat2        = cardValue(R.id.cardCat2)
        vModVoltage  = cardValue(R.id.cardModVoltage)
        vFuelRate    = cardValue(R.id.cardFuelRate)
    }

    private fun cardValue(includeId: Int): TextView =
        (findViewById<View>(includeId)).findViewById(R.id.cardValue)

    private fun cardLabel(includeId: Int): TextView =
        (findViewById<View>(includeId)).findViewById(R.id.cardLabel)

    private fun cardUnit(includeId: Int): TextView =
        (findViewById<View>(includeId)).findViewById(R.id.cardUnit)

    private fun setupCardLabels() {
        data class CardMeta(val id: Int, val label: String, val unit: String)
        listOf(
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
        ).forEach { c ->
            cardLabel(c.id).text = c.label
            cardUnit(c.id).text  = c.unit
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Permission helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun hasPermission(p: String) =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

    private fun hasBleConnectPermission() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    private fun hasBleScanPermission() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                hasPermission(Manifest.permission.BLUETOOTH_SCAN)

    // ─────────────────────────────────────────────────────────────────────────
    //  Connect / disconnect button
    // ─────────────────────────────────────────────────────────────────────────
    private fun onConnectClicked() {
        if (isConnected) disconnectAndReset() else requestPermissionsAndScan()
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

    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BLE scan
    // ─────────────────────────────────────────────────────────────────────────
    private fun startBleScan() {
        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bm.adapter
        if (adapter == null || !adapter.isEnabled) {
            toast("Please enable Bluetooth first"); return
        }
        if (!hasBleScanPermission()) { toast("BLUETOOTH_SCAN permission missing"); return }

        setStatus("Scanning for ELM327 adapter…")
        btnConnect.isEnabled = false

        val scanner  = adapter.bluetoothLeScanner
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = if (hasBleConnectPermission()) result.device.name ?: "" else ""
                val up   = name.uppercase()
                if (up.contains("OBD") || up.contains("ELM") ||
                    up.contains("VLINK") || up.contains("SCAN")) {
                    if (hasBleScanPermission()) scanner.stopScan(this)
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

        scanner.startScan(null, settings, callback)

        mainHandler.postDelayed({
            if (hasBleScanPermission()) try { scanner.stopScan(callback) } catch (_: Exception) {}
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
        if (!hasBleConnectPermission()) {
            toast("Missing BLUETOOTH_CONNECT permission")
            btnConnect.isEnabled = true; return
        }
        bluetoothGatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "GATT connected")
                    if (hasBleConnectPermission()) gatt.discoverServices()
                    else runOnUiThread { setStatus("Missing BLUETOOTH_CONNECT permission") }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "GATT disconnected")
                    isConnected = false; isInitialized = false
                    pollJob?.cancel()
                    runOnUiThread { onDisconnected() }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                runOnUiThread { setStatus("Service discovery failed") }; return
            }
            if (!hasBleConnectPermission()) {
                runOnUiThread { setStatus("Missing BLUETOOTH_CONNECT permission") }; return
            }

            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                runOnUiThread { setStatus("ELM327 service UUID not found. Check adapter UUID constants.") }
                return
            }

            writeChar  = service.getCharacteristic(WRITE_UUID)
            notifyChar = service.getCharacteristic(NOTIFY_UUID)

            if (notifyChar == null || writeChar == null) {
                runOnUiThread { setStatus("ELM327 characteristics not found") }; return
            }

            gatt.setCharacteristicNotification(notifyChar, true)
            val desc = notifyChar!!.getDescriptor(CCCD_UUID)
            desc?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION") it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION") gatt.writeDescriptor(it)
                }
            }

            isConnected = true
            runOnUiThread { setBleConnected(true); btnConnect.isEnabled = true }

            lifecycleScope.launch(Dispatchers.IO) { delay(600); initElm327(); startPollLoop() }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val raw = characteristic.value?.toString(Charsets.UTF_8) ?: return
            responseChannel.trySend(raw)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            responseChannel.trySend(value.toString(Charsets.UTF_8))
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ELM327 command helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun sendRaw(cmd: String) {
        val char = writeChar ?: return
        if (!hasBleConnectPermission()) return
        val bytes = (cmd + "\r").toByteArray(Charsets.UTF_8)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt?.writeCharacteristic(char, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        } else {
            @Suppress("DEPRECATION") char.value = bytes
            @Suppress("DEPRECATION") bluetoothGatt?.writeCharacteristic(char)
        }
    }

    private suspend fun send(cmd: String, timeoutMs: Long = 3000): String {
        if (!isConnected) return ""
        while (responseChannel.tryReceive().isSuccess) { /* drain */ }
        withContext(Dispatchers.Main) { sendRaw(cmd) }
        val buffer   = StringBuilder()
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val chunk = withTimeoutOrNull(deadline - System.currentTimeMillis()) {
                responseChannel.receive()
            } ?: break
            buffer.append(chunk)
            if (buffer.contains('>')) break
        }
        return buffer.toString().trim().also { Log.d(TAG, "CMD: $cmd  RSP: $it") }
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
        Log.d(TAG, "Supported PIDs: ${send("0100")}")
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
                    tick++
                    if (tick % 4 == 0) pollExtended()
                    delay(300)
                } catch (e: CancellationException) { throw e
                } catch (e: Exception) { Log.w(TAG, "Poll error: ${e.message}"); delay(1000) }
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
    private fun getBytes(raw: String): List<String> {
        var c = raw.replace(Regex("\\s+"), "").uppercase()
        if (c.startsWith("7E8") || c.startsWith("7E9")) c = c.substring(3)
        val b = mutableListOf<String>(); var i = 0
        while (i < c.length - 1) { val s = c.substring(i, i + 2); if (s.matches(Regex("[0-9A-F]{2}"))) b.add(s); i += 2 }
        return b
    }

    private fun isError(raw: String) =
        raw.isBlank() || raw.contains("NO DATA") || raw.contains("TIMEOUT") ||
                raw.contains("ERROR") || raw.contains("?")

    private fun parseRPM(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == "0C") return ((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 4).toString()
        return null
    }
    private fun parseSpeed(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == "0D") return b[i+2].toInt(16).toString()
        return null
    }
    private fun parseTemp(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 3) if (b[i] == "41") return (b[i+2].toInt(16) - 40).toString()
        return null
    }
    private fun parsePercent(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw); val p = pid.uppercase()
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == p) return "%.1f".format((b[i+2].toInt(16) / 255.0) * 100)
        return null
    }
    private fun parseSignedPercent(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw); val p = pid.uppercase()
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == p) return "%.1f".format(((b[i+2].toInt(16) - 128) * 100.0) / 128.0)
        return null
    }
    private fun parseMAF(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == "10") return "%.2f".format((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 100.0)
        return null
    }
    private fun parsePressure(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw); val p = pid.uppercase()
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == p) return b[i+2].toInt(16).toString()
        return null
    }
    private fun parseTimingAdvance(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == "0E") return "%.1f".format((b[i+2].toInt(16) / 2.0) - 64.0)
        return null
    }
    private fun parseO2Voltage(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw); val p = pid.uppercase()
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == p) return "%.2f".format(b[i+2].toInt(16) / 200.0)
        return null
    }
    private fun parseCatalystTemp(raw: String, pid: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw); val p = pid.uppercase()
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == p) return "%.1f".format((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 10.0 - 40.0)
        return null
    }
    private fun parseModuleVoltage(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == "42") return "%.2f".format((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 1000.0)
        return null
    }
    private fun parseFuelRate(raw: String): String? {
        if (isError(raw)) return null
        val b = getBytes(raw)
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == "5E") return "%.2f".format((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 20.0)
        return null
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun setStatus(msg: String) { tvStatusMsg.text = msg }

    private fun setBleConnected(connected: Boolean) {
        if (connected) {
            tvBleStatus.text = "● CONNECTED"
            tvBleStatus.setTextColor(0xFF00E676.toInt())
            btnConnect.text  = "DISCONNECT"
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
        if (hasBleConnectPermission()) { bluetoothGatt?.disconnect(); bluetoothGatt?.close() }
        bluetoothGatt = null; isConnected = false; isInitialized = false
        onDisconnected()
    }

    private fun resetAllValues() {
        listOf(speedVal, rpmVal, coolantVal, throttleVal, loadVal,
            vMaf, vIat, vO2s1, vO2s2, vMap, vFuelLevel,
            vStft, vLtft, vTiming, vBaro, vCat1, vCat2,
            vModVoltage, vFuelRate
        ).forEach { it.text = "—" }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

/*
 ── XML snippet: add tvBleStatus to your connectRow in activity_diagnostic_data.xml ──

    <LinearLayout
        android:id="@+id/connectRow"
        ... >

        <!-- ADD THIS TextView before tvStatusMsg -->
        <TextView
            android:id="@+id/tvBleStatus"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="● DISCONNECTED"
            android:textColor="#FFFF5555"
            android:textSize="11sp"
            android:textStyle="bold"
            android:paddingEnd="10dp" />

        <TextView android:id="@+id/tvStatusMsg"/>
        <Button   android:id="@+id/btnConnect"/>
    </LinearLayout>
*/