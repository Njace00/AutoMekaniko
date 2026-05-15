package com.example.automekaniko

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

private val SERVICE_UUIDS = listOf(
    UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),
    UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
    UUID.fromString("000018f0-0000-1000-8000-00805f9b34fb")
)
private val RFCOMM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
private val CCCD_UUID   = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
private const val TAG   = "MainActivity"

private const val PREF_NAME = "automekaniko_obd"
private const val KEY_FUEL_LEVEL_MANUAL = "fuel_level_manual_pct"

/** Huwag permanent i-blacklist — madalas ang false NO DATA / timeout sa BLE */
private val PID_NEVER_BLACKLIST = setOf("010B", "012F", "015E")

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    private lateinit var tvBleStatus:  TextView
    private lateinit var tvStatusMsg:  TextView
    private lateinit var btnConnect:   Button
    private lateinit var tab3D:        TextView
    private lateinit var tabOBD:       TextView

    private lateinit var speedVal:    TextView
    private lateinit var rpmVal:      TextView
    private lateinit var coolantVal:  TextView
    private lateinit var throttleVal: TextView
    private lateinit var loadVal:     TextView

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

    private var bluetoothGatt:     BluetoothGatt? = null
    private var writeChar:         BluetoothGattCharacteristic? = null
    private var notifyChar:        BluetoothGattCharacteristic? = null
    private var bluetoothSocket:   BluetoothSocket? = null
    private var inputStream:       InputStream?  = null
    private var outputStream:      OutputStream? = null
    private var classicConnectJob: Job? = null

    private var isConnected   = false
    private var isInitialized = false
    private val responseChannel = Channel<String>(capacity = Channel.UNLIMITED)
    private var pollJob: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val unsupportedPids = mutableSetOf<String>()

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.all { it.value }) startBleScan()
        else toast("Permissions required for Bluetooth discovery")
    }

    /**
     * Optional: itakda ang fuel level (0–100) kapag walang PID 012F ang ECU.
     * I-disable: [setManualFuelLevelPercent(-1f)]
     */
    fun setManualFuelLevelPercent(pct: Float) {
        require(pct in -1f..100f) { "Use -1f to disable, or 0f..100f" }
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
            .putFloat(KEY_FUEL_LEVEL_MANUAL, pct).apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        setupCardLabels()
        setupFuelLevelCardLongPress()
        setupTabs()
        btnConnect.setOnClickListener { onConnectClicked() }
    }

    override fun onDestroy() {
        pollJob?.cancel()
        classicConnectJob?.cancel()
        if (hasBleConnectPermission()) bluetoothGatt?.close()
        disconnectClassic()
        super.onDestroy()
    }

    private fun setupTabs() {
        setTabActive(tab3D,  active = false)
        setTabActive(tabOBD, active = true)
        tab3D.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

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

    private fun bindViews() {
        tvBleStatus  = findViewById(R.id.tvBleStatus)
        tvStatusMsg  = findViewById(R.id.tvStatusMsg)
        btnConnect   = findViewById(R.id.btnConnect)
        tab3D        = findViewById(R.id.tab3D)
        tabOBD       = findViewById(R.id.tabOBD)
        speedVal     = findViewById(R.id.speedVal)
        rpmVal       = findViewById(R.id.rpmVal)
        coolantVal   = findViewById(R.id.coolantVal)
        throttleVal  = findViewById(R.id.throttleVal)
        loadVal      = findViewById(R.id.loadVal)
        vMaf        = cardValue(R.id.cardMaf);        vIat       = cardValue(R.id.cardIat)
        vO2s1       = cardValue(R.id.cardO2s1);       vO2s2      = cardValue(R.id.cardO2s2)
        vMap        = cardValue(R.id.cardMap);         vFuelLevel = cardValue(R.id.cardFuelLevel)
        vStft       = cardValue(R.id.cardStft);        vLtft      = cardValue(R.id.cardLtft)
        vTiming     = cardValue(R.id.cardTiming);      vBaro      = cardValue(R.id.cardBaro)
        vCat1       = cardValue(R.id.cardCat1);        vCat2      = cardValue(R.id.cardCat2)
        vModVoltage = cardValue(R.id.cardModVoltage);  vFuelRate  = cardValue(R.id.cardFuelRate)
    }

    private fun cardValue(id: Int): TextView = findViewById<View>(id).findViewById(R.id.cardValue)
    private fun cardLabel(id: Int): TextView = findViewById<View>(id).findViewById(R.id.cardLabel)
    private fun cardUnit(id: Int):  TextView = findViewById<View>(id).findViewById(R.id.cardUnit)

    private fun setupCardLabels() {
        data class Meta(val id: Int, val l: String, val u: String)
        listOf(
            Meta(R.id.cardMaf,        "Mass Air Flow",   "g/s"),
            Meta(R.id.cardIat,        "Intake Air Temp", "°C"),
            Meta(R.id.cardO2s1,       "O2 Sensor 1",     "V"),
            Meta(R.id.cardO2s2,       "O2 Sensor 2",     "V"),
            Meta(R.id.cardMap,        "MAP",              "kPa"),
            Meta(R.id.cardFuelLevel,  "Fuel Level",       "%"),
            Meta(R.id.cardStft,       "Short Fuel Trim",  "%"),
            Meta(R.id.cardLtft,       "Long Fuel Trim",   "%"),
            Meta(R.id.cardTiming,     "Timing Advance",   "°"),
            Meta(R.id.cardBaro,       "Baro Pressure",    "kPa"),
            Meta(R.id.cardCat1,       "Catalyst T1",      "°C"),
            Meta(R.id.cardCat2,       "Catalyst T2",      "°C"),
            Meta(R.id.cardModVoltage, "Module Voltage",   "V"),
            Meta(R.id.cardFuelRate,   "Fuel Rate",        "L/h")
        ).forEach { cardLabel(it.id).text = it.l; cardUnit(it.id).text = it.u }
    }

    private fun hasPermission(p: String) =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    private fun hasBleConnectPermission() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    private fun onConnectClicked() {
        if (isConnected) disconnectAndReset() else requestPermissionsAndScan()
    }

    private fun requestPermissionsAndScan() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN))    needed += Manifest.permission.BLUETOOTH_SCAN
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) needed += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            needed += Manifest.permission.BLUETOOTH
            needed += Manifest.permission.ACCESS_FINE_LOCATION
        }
        if (needed.isEmpty()) startBleScan() else permLauncher.launch(needed.toTypedArray())
    }

    private fun startBleScan() {
        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bm.adapter
        if (adapter == null || !adapter.isEnabled) { toast("Enable Bluetooth first"); return }

        setStatus("Checking paired devices…")
        btnConnect.isEnabled = false

        if (!hasBleConnectPermission()) return
        val paired = adapter.bondedDevices ?: emptySet()
        if (paired.isEmpty()) {
            runOnUiThread { setStatus("❌ NO PAIRED DEVICES\n\nPlease pair ELM327 in Settings"); btnConnect.isEnabled = true }
            return
        }

        var found = false
        paired.forEach { device ->
            val name = (device.name ?: "").uppercase()
            if ((name.contains("ELM") || name.contains("OBD")) && !found) {
                found = true
                runOnUiThread { setStatus("Found: ${device.name}\nConnecting…"); connectToDevice(device) }
            }
        }
        if (!found) runOnUiThread { setStatus("❌ NO OBD DEVICES FOUND\nCheck paired devices"); btnConnect.isEnabled = true }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (!hasBleConnectPermission()) return
        Log.d(TAG, "Connect → ${device.name} type=${device.type}")
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
                runOnUiThread { setBleConnected(true); btnConnect.isEnabled = true; setStatus("Connected! Initializing…") }
                delay(1000)
                initElm327()
                startPollLoop()
            } catch (e: Exception) {
                Log.e(TAG, "Classic failed: ${e.message}")
                isConnected = false
                runOnUiThread { setStatus("Connection failed: ${e.message}"); onDisconnected(); btnConnect.isEnabled = true }
                disconnectClassic()
            }
        }
    }

    private fun disconnectClassic() {
        try { inputStream?.close(); outputStream?.close(); bluetoothSocket?.close() } catch (_: Exception) {}
        inputStream = null; outputStream = null; bluetoothSocket = null
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
                runOnUiThread { setBleConnected(true); btnConnect.isEnabled = true }
                lifecycleScope.launch(Dispatchers.IO) { delay(1000); initElm327(); startPollLoop() }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && descriptor.uuid == CCCD_UUID) {
                isConnected = true
                runOnUiThread { setBleConnected(true); btnConnect.isEnabled = true }
                lifecycleScope.launch(Dispatchers.IO) { delay(1000); initElm327(); startPollLoop() }
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

    private suspend fun send(cmd: String, timeout: Long = 3000): String {
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
                    } else delay(10)
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
        for (cmd in listOf("ATZ", "ATE0", "ATL0", "ATS0", "ATH0", "ATSP0")) {
            send(cmd, if (cmd == "ATZ") 5000 else 2000)
            delay(300)
        }
        isInitialized = true
        runOnUiThread { setStatus("Polling Live Data…") }
    }

    private fun startPollLoop() {
        pollJob?.cancel()
        pollJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive && isConnected && isInitialized) {
                try {
                    val rpm  = parseRPM(send("010C"))
                    val spd  = parseSpeed(send("010D"))
                    val cool = parseTemp(send("0105"), "05")
                    val thr  = parsePerc(send("0111"), "11")
                    val load = parsePerc(send("0104"), "04")

                    runOnUiThread {
                        rpm?.let  { rpmVal.text      = it }
                        spd?.let  { speedVal.text    = it }
                        cool?.let { coolantVal.text  = it }
                        thr?.let  { throttleVal.text = it }
                        load?.let { loadVal.text     = it }
                    }

                    pollExtended()
                    delay(500)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Poll error: ${e.message}"); delay(1000)
                }
            }
        }
    }

    private suspend fun pollExtended() {
        val extTimeout = 4500L

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

        val mafRsp = q("0110")
        val maf    = parseMaf(mafRsp)
        val iat    = parseTemp(q("010F"), "0F")
        val o2s1   = parseO2(q("0114"), "14")
        val o2s2   = parseO2(q("0115"), "15")

        val loadRsp = q("0104")
        val baroRsp = q("0133")
        val mapRsp  = q("010B")
        val fuelRRsp = q("015E")

        val loadPct = parsePerc(loadRsp, "04")?.toDoubleOrNull()
        val baroKpa = parseBaro(baroRsp)?.toIntOrNull()

        val map   = parseMapAll(mapRsp, baroKpa, loadPct)
        val fuel  = readFuelLevelFull(extTimeout)
        val fuelR = parseFuelRate(fuelRRsp) ?: estimateFuelRateLphFromMaf(mafRsp)

        val stft   = parseFuelTrim(q("0106"), "06")
        val ltft   = parseFuelTrim(q("0107"), "07")
        val timing = parseTiming(q("010E"))
        val baro   = parseBaro(baroRsp)

        val cat1  = parseCatTemp(q("013C"), "3C")
        val cat2  = parseCatTemp(q("013E"), "3E")
        val modV  = parseVolt(q("0142"))

        runOnUiThread {
            maf?.let      { vMaf.text        = it }
            iat?.let      { vIat.text        = it }
            o2s1?.let     { vO2s1.text       = it }
            o2s2?.let     { vO2s2.text       = it }
            map?.let      { vMap.text        = it }
            fuel?.let     { vFuelLevel.text  = it }
            stft?.let     { vStft.text       = it }
            ltft?.let     { vLtft.text       = it }
            timing?.let   { vTiming.text     = it }
            baro?.let     { vBaro.text       = it }
            cat1?.let     { vCat1.text       = it }
            cat2?.let     { vCat2.text       = it }
            modV?.let     { vModVoltage.text = it }
            fuelR?.let    { vFuelRate.text   = it }
            setStatus("Connected. Polling…")
        }
    }

    private fun fuelManualPct(): Float =
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).getFloat(KEY_FUEL_LEVEL_MANUAL, -1f)

    /** Long press sa Fuel Level card → manual % (kapag walang 012F ang ECU) */
    private fun setupFuelLevelCardLongPress() {
        findViewById<View>(R.id.cardFuelLevel).setOnLongClickListener {
            val input = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                hint = "0–100 o bakante para i-clear"
                val m = fuelManualPct()
                if (m in 0f..100f) setText("%.1f".format(m))
            }
            AlertDialog.Builder(this)
                .setTitle("Fuel level (manual)")
                .setMessage("Kung NO DATA ang ECU sa PID 012F, ilagay dito ang tank % (tantsa).")
                .setView(input)
                .setPositiveButton("OK") { d, _ ->
                    val t = input.text.toString().trim().replace(',', '.')
                    if (t.isEmpty()) {
                        setManualFuelLevelPercent(-1f)
                        toast("Manual fuel cleared")
                    } else {
                        val v = t.toFloatOrNull()
                        if (v != null) {
                            setManualFuelLevelPercent(v.coerceIn(0f, 100f))
                            toast("Fuel level saved")
                        }
                    }
                    d.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    /**
     * Direktang [send] (hindi [q]) para makita pa rin ang buong sagot kahit NO DATA.
     * Sunod: alternate CAN headers, manual prefs.
     */
    private suspend fun readFuelLevelFull(extMs: Long): String? {
        repeat(4) { attempt ->
            val raw = send("012F", extMs)
            Log.d(TAG, "012F try$attempt raw=${raw.take(160)}")
            parseFuelLevelExhaustive(raw)?.let { return it }
            if (raw.contains("NO DATA", ignoreCase = true)) return@repeat
            delay(150L * (attempt + 1))
        }
        send("012F", 7000).let { raw ->
            Log.d(TAG, "012F long raw=${raw.take(160)}")
            parseFuelLevelExhaustive(raw)?.let { return it }
        }
        tryFuelLevelAlternateHeaders(extMs)?.let { return it }

        val manual = fuelManualPct()
        if (manual in 0f..100f) return "%.1f".format(manual)
        return null
    }

    /** Ilang karaniwang CAN TX address — minsan nasa ibang module ang fuel % */
    private suspend fun tryFuelLevelAlternateHeaders(timeoutEach: Long): String? {
        if (!isConnected) return null
        val headers = listOf("7E0", "7E4", "7E1", "7E6", "706", "7DF")
        for (h in headers) {
            try {
                send("AT SH $h", 800)
                delay(150)
                val raw = send("012F", timeoutEach)
                Log.d(TAG, "012F AT SH $h → ${raw.take(120)}")
                parseFuelLevelExhaustive(raw)?.let { return it }
            } catch (e: Exception) {
                Log.w(TAG, "fuel probe SH $h: ${e.message}")
            }
        }
        try {
            send("AT SH 7E0", 600)
            delay(100)
        } catch (_: Exception) {}
        return null
    }

    private fun elmResponseToHex(r: String): String {
        if (r.isBlank()) return ""
        return r.split(Regex("\\r?\\n")).joinToString("") { line ->
            val t = line.trim()
            val noNum = t.replaceFirst(Regex("(?i)^[0-9a-f]+:\\s*"), "")
            noNum.replace(Regex("[^0-9A-Fa-f]"), "").uppercase()
        }
    }

    private fun bytesForPid01(r: String, pidHex: String): List<String>? {
        val hex = elmResponseToHex(r)
        if (hex.isEmpty()) return null
        val p = pidHex.uppercase().takeLast(2).padStart(2, '0')
        val needle = "41$p"
        val idx = hex.lastIndexOf(needle)
        if (idx < 0) return null
        val tail = hex.substring(idx)
        val out = ArrayList<String>()
        var i = 0
        while (i + 1 < tail.length) {
            out.add(tail.substring(i, i + 2))
            i += 2
        }
        return out
    }

    private fun getB(r: String): List<String> {
        val hex = elmResponseToHex(r)
        if (hex.isEmpty()) return emptyList()
        val idx = hex.lastIndexOf("41")
        val c = if (idx >= 0) hex.substring(idx) else hex
        val res = mutableListOf<String>()
        var i = 0
        while (i + 1 < c.length) {
            res.add(c.substring(i, i + 2))
            i += 2
        }
        return res
    }

    private fun isErr(r: String) =
        r.isBlank() || r.contains("NO DATA", ignoreCase = true) || r.contains("ERROR") ||
                r.contains("?") || r.contains("UNABLE") || r.contains("STOPPED")

    private fun parseMapLoose(r: String): String? {
        if (isErr(r)) return null
        val m = Regex("(?i)41\\s*0B\\s*([0-9A-F]{2})").find(r) ?: return null
        return m.groupValues[1].toInt(16).toString()
    }

    private fun parseFuelLevelLoose(r: String): String? {
        if (isErr(r)) return null
        val m = Regex("(?i)41\\s*2F\\s*([0-9A-F]{2})").find(r) ?: return null
        val a = m.groupValues[1].toInt(16)
        return "%.1f".format(a * 100.0 / 255.0)
    }

    /** Rough intake MAP kapag walang tunay na 010B — hindi replacement ng sensor */
    private fun estimateMapKpaFromLoad(baroKpa: Int?, loadPct: Double?): String? {
        if (baroKpa == null || loadPct == null) return null
        if (baroKpa <= 0) return null
        val load = loadPct.coerceIn(0.0, 100.0)
        val factor = 0.25 + 0.75 * (load / 100.0)
        val est = baroKpa * factor
        val clamped = est.coerceIn(22.0, baroKpa.toDouble())
        return "%.0f".format(clamped)
    }

    private fun parseMap(r: String): String? {
        if (isErr(r)) return null
        val b = bytesForPid01(r, "0B") ?: getB(r)
        if (b.size >= 3 && b[0] == "41" && b[1] == "0B") return b[2].toInt(16).toString()
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i + 1] == "0B") return b[i + 2].toInt(16).toString()
        return null
    }

    private fun parseMapAll(r: String, baroKpa: Int?, loadPct: Double?): String? {
        val direct = parseMap(r) ?: parseMapLoose(r)
        if (direct != null) return direct
        return estimateMapKpaFromLoad(baroKpa, loadPct)
    }

    private fun parseFuelLevelAll(r: String): String? {
        if (isErr(r)) return null
        return parsePerc(r, "2F") ?: parseFuelLevelLoose(r)
    }

    private fun parseFuelLevelExhaustive(raw: String): String? {
        if (raw.isBlank()) return null
        if (raw.contains("NO DATA", ignoreCase = true)) return null

        parseFuelLevelAll(raw)?.let { return it }

        val hex = elmResponseToHex(raw)
        val needle = "412F"
        var idx = hex.lastIndexOf(needle)
        while (idx >= 0) {
            if (idx + 6 <= hex.length) {
                val a = hex.substring(idx + 4, idx + 6)
                if (a.length == 2 && a.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' })
                    return "%.1f".format(a.toInt(16) * 100.0 / 255.0)
            }
            if (idx == 0) break
            idx = hex.lastIndexOf(needle, idx - 1)
        }

        Regex("(?i)62\\s*2F\\s*([0-9A-F]{2})").find(raw)?.let { m ->
            return "%.1f".format(m.groupValues[1].toInt(16) * 100.0 / 255.0)
        }
        return null
    }

    private fun parseRPM(r: String): String? {
        if (isErr(r)) return null
        val b = getB(r)
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == "0C")
            return ((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 4).toString()
        return null
    }

    private fun parseSpeed(r: String): String? {
        if (isErr(r)) return null
        val b = getB(r)
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == "0D") return b[i+2].toInt(16).toString()
        return null
    }

    private fun parseTemp(r: String, pid: String): String? {
        if (isErr(r)) return null
        val b = getB(r); val p = pid.uppercase().takeLast(2)
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == p)
            return (b[i+2].toInt(16) - 40).toString()
        return null
    }

    private fun parsePerc(r: String, pid: String): String? {
        if (isErr(r)) return null
        val p = pid.uppercase().takeLast(2).padStart(2, '0')
        val b = bytesForPid01(r, p) ?: getB(r)
        if (b.size >= 3 && b[0] == "41" && b[1] == p)
            return "%.1f".format(b[2].toInt(16) * 100.0 / 255.0)
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == p)
            return "%.1f".format(b[i+2].toInt(16) * 100.0 / 255.0)
        return null
    }

    private fun parseVolt(r: String): String? {
        if (isErr(r)) return null
        val b = getB(r)
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == "42")
            return "%.2f".format((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 1000.0)
        return null
    }

    private fun parseMaf(r: String): String? {
        if (isErr(r)) return null
        val b = getB(r)
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == "10")
            return "%.2f".format((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 100.0)
        return null
    }

    private fun parseO2(r: String, pid: String): String? {
        if (isErr(r)) return null
        val b = getB(r); val p = pid.uppercase().takeLast(2)
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == p)
            return "%.3f".format(b[i+2].toInt(16) / 200.0)
        return null
    }

    private fun parseFuelTrim(r: String, pid: String): String? {
        if (isErr(r)) return null
        val b = getB(r); val p = pid.uppercase().takeLast(2)
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == p)
            return "%.1f".format((b[i+2].toInt(16) - 128) * 100.0 / 128.0)
        return null
    }

    private fun parseTiming(r: String): String? {
        if (isErr(r)) return null
        val b = getB(r)
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == "0E")
            return "%.1f".format(b[i+2].toInt(16) / 2.0 - 64.0)
        return null
    }

    private fun parseBaro(r: String): String? {
        if (isErr(r)) return null
        val b = getB(r)
        for (i in 0..b.size - 3) if (b[i] == "41" && b[i+1] == "33") return b[i+2].toInt(16).toString()
        return null
    }

    private fun parseCatTemp(r: String, pid: String): String? {
        if (isErr(r)) return null
        val b = getB(r); val p = pid.uppercase().takeLast(2)
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == p)
            return "%.1f".format((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 10.0 - 40.0)
        return null
    }

    private fun parseFuelRate(r: String): String? {
        if (isErr(r)) return null
        val b = bytesForPid01(r, "5E") ?: getB(r)
        if (b.size >= 4 && b[0] == "41" && b[1] == "5E")
            return "%.2f".format((b[2].toInt(16) * 256 + b[3].toInt(16)) / 20.0)
        for (i in 0..b.size - 4) if (b[i] == "41" && b[i+1] == "5E")
            return "%.2f".format((b[i+2].toInt(16) * 256 + b[i+3].toInt(16)) / 20.0)
        return null
    }

    private fun estimateFuelRateLphFromMaf(mafResponse: String): String? {
        if (isErr(mafResponse)) return null
        val b = bytesForPid01(mafResponse, "10") ?: return null
        if (b.size < 4 || b[0] != "41" || b[1] != "10") return null
        val maf = (b[2].toInt(16) * 256 + b[3].toInt(16)) / 100.0
        if (maf < 1.0) return null
        val lph = maf * 3600.0 / (14.7 * 730.0)
        return "%.2f".format(lph)
    }

    private fun setStatus(m: String) { tvStatusMsg.text = m }

    private fun setBleConnected(c: Boolean) {
        tvBleStatus.text = if (c) "● CONNECTED" else "● DISCONNECTED"
        tvBleStatus.setTextColor(if (c) 0xFF00E676.toInt() else 0xFFFF5555.toInt())
        btnConnect.text = if (c) "DISCONNECT" else "CONNECT"
        btnConnect.isEnabled = true
    }

    private fun onDisconnected() { setBleConnected(false); setStatus("Disconnected. Try again."); resetV() }

    private fun disconnectAndReset() {
        pollJob?.cancel(); classicConnectJob?.cancel()
        if (hasBleConnectPermission()) { bluetoothGatt?.disconnect(); bluetoothGatt?.close() }
        disconnectClassic(); bluetoothGatt = null; isConnected = false; onDisconnected()
    }

    private fun resetV() {
        listOf(
            speedVal, rpmVal, coolantVal, throttleVal, loadVal,
            vMaf, vIat, vO2s1, vO2s2, vMap, vFuelLevel,
            vStft, vLtft, vTiming, vBaro, vCat1, vCat2,
            vModVoltage, vFuelRate
        ).forEach { it.text = "—" }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_LONG).show()
}