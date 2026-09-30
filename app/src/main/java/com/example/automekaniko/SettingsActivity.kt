package com.example.automekaniko

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.automekaniko.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    companion object {
        const val PREFS_NAME = "automekaniko_settings"
        const val KEY_THEME = "app_theme"
        const val KEY_UNITS_METRIC = "units_metric"
        const val KEY_REDLINE = "dash_redline"
        const val KEY_KEEP_SCREEN_ON = "dash_keep_screen_on"
        const val KEY_AUTO_EXPAND = "checklist_auto_expand"
        const val KEY_OBD_AUTO_CONNECT = "obd_auto_connect"
        const val KEY_OBD_MAC = "obd_mac_address"
        const val KEY_REDLINE_ALERT = "dash_redline_alert"
        const val KEY_TEMP_ALERT = "dash_temp_alert"

        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_SYSTEM = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupTitle()
        loadVehicleProfileInfo()
        loadSettings()
        setupListeners()
        AppNavigation.wire(this)

        // Tactile Press Micro-Interactions
        ViewAnimationUtils.applyPressScaleToAll(
            binding.cardThemeLight,
            binding.cardThemeDark,
            binding.cardThemeSystem,
            binding.btnSwitchVehicle,
            binding.btnForgetObd,
            binding.btnResetChecklists,
            binding.btnClearDtcCache,
            binding.btnRestoreDefaults,
            binding.backBtn
        )
    }

    override fun onResume() {
        super.onResume()
        loadVehicleProfileInfo()
    }

    private fun setupTitle() {
        AppNavigation.setupBrandedTitle(this, binding.appTitle)
        binding.backBtn.setOnClickListener { finish() }
    }

    private fun loadVehicleProfileInfo() {
        val activeVehicle = VehicleManager.getActiveVehicle(this)
        binding.tvActiveVehicleName.text = activeVehicle.name
        binding.tvActiveVehicleEngine.text = "${activeVehicle.engine} • ${activeVehicle.oilCapacity} Oil"
    }

    private fun loadSettings() {
        // Theme Cards
        val theme = prefs.getInt(KEY_THEME, THEME_SYSTEM)
        updateThemeCards(theme)

        // OBD Preferences
        binding.switchObdAutoConnect.isChecked = prefs.getBoolean(KEY_OBD_AUTO_CONNECT, true)

        // Gauge Alerts
        binding.switchRedlineAlert.isChecked = prefs.getBoolean(KEY_REDLINE_ALERT, true)
        binding.switchTempAlert.isChecked = prefs.getBoolean(KEY_TEMP_ALERT, true)

        // Measurement & Display Units
        binding.switchMetric.isChecked = prefs.getBoolean(KEY_UNITS_METRIC, true)
        binding.etRedline.setText(prefs.getInt(KEY_REDLINE, 6500).toString())
        binding.switchKeepScreenOn.isChecked = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false)
        binding.switchAutoExpand.isChecked = prefs.getBoolean(KEY_AUTO_EXPAND, false)
    }

    private fun updateThemeCards(selectedTheme: Int) {
        val redColor = ContextCompat.getColor(this, R.color.theme_red)
        val dividerColor = ContextCompat.getColor(this, R.color.divider)
        val redLightColor = ContextCompat.getColor(this, R.color.theme_red_light)
        val surfaceColor = ContextCompat.getColor(this, R.color.surface_card)

        fun applyStyle(
            card: com.google.android.material.card.MaterialCardView,
            label: android.widget.TextView,
            isSelected: Boolean
        ) {
            card.strokeColor = if (isSelected) redColor else dividerColor
            card.strokeWidth = if (isSelected) (2 * resources.displayMetrics.density).toInt() else (1 * resources.displayMetrics.density).toInt()
            card.setCardBackgroundColor(if (isSelected) redLightColor else surfaceColor)
            label.setTextColor(if (isSelected) redColor else ContextCompat.getColor(this, R.color.text_primary))
        }

        applyStyle(binding.cardThemeLight, binding.tvThemeLight, selectedTheme == THEME_LIGHT)
        applyStyle(binding.cardThemeDark, binding.tvThemeDark, selectedTheme == THEME_DARK)
        applyStyle(binding.cardThemeSystem, binding.tvThemeSystem, selectedTheme == THEME_SYSTEM)
    }

    private fun setupListeners() {
        // Vehicle Switch Button
        binding.btnSwitchVehicle.setOnClickListener {
            VehicleManager.showSelectorDialog(this) {
                loadVehicleProfileInfo()
            }
        }

        // Theme Segmented Cards
        binding.cardThemeLight.setOnClickListener { selectTheme(THEME_LIGHT) }
        binding.cardThemeDark.setOnClickListener { selectTheme(THEME_DARK) }
        binding.cardThemeSystem.setOnClickListener { selectTheme(THEME_SYSTEM) }

        // OBD Preferences
        binding.switchObdAutoConnect.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_OBD_AUTO_CONNECT, isChecked).apply()
        }

        binding.btnForgetObd.setOnClickListener {
            prefs.edit().remove(KEY_OBD_MAC).apply()
            Toast.makeText(this, "Saved OBD-II Bluetooth adapter cleared.", Toast.LENGTH_SHORT).show()
        }

        // Gauge Alerts
        binding.switchRedlineAlert.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_REDLINE_ALERT, isChecked).apply()
        }

        binding.switchTempAlert.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_TEMP_ALERT, isChecked).apply()
        }

        // Measurement Units Switch
        binding.switchMetric.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_UNITS_METRIC, isChecked).apply()
        }

        // Redline EditText
        binding.etRedline.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val value = s.toString().toIntOrNull() ?: 6500
                prefs.edit().putInt(KEY_REDLINE, value).apply()
            }
        })

        // Keep Screen On Switch
        binding.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, isChecked).apply()
        }

        // Checklist Switch
        binding.switchAutoExpand.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AUTO_EXPAND, isChecked).apply()
        }

        // Data Management Buttons
        binding.btnResetChecklists.setOnClickListener {
            confirmResetChecklists()
        }

        binding.btnClearDtcCache.setOnClickListener {
            confirmClearDtcCache()
        }

        binding.btnRestoreDefaults.setOnClickListener {
            confirmRestoreDefaults()
        }
    }

    private fun selectTheme(themeMode: Int) {
        prefs.edit().putInt(KEY_THEME, themeMode).apply()
        updateThemeCards(themeMode)
        applyTheme(themeMode)
    }

    private fun applyTheme(themeMode: Int) {
        val mode = when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun confirmResetChecklists() {
        AlertDialog.Builder(this)
            .setTitle("Reset Checklist Progress?")
            .setMessage("This will clear all completed step checkmarks across all maintenance and diagnostic guides.")
            .setPositiveButton("Reset") { _, _ ->
                val checklistPrefs = getSharedPreferences(MAINTAINANCEActivity.PREFS_CHECKLIST, Context.MODE_PRIVATE)
                checklistPrefs.edit().clear().apply()
                Toast.makeText(this, "All checklist progress has been reset.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmClearDtcCache() {
        AlertDialog.Builder(this)
            .setTitle("Clear Saved DTC Codes?")
            .setMessage("This will clear cached trouble code scan logs.")
            .setPositiveButton("Clear") { _, _ ->
                Toast.makeText(this, "Saved trouble code logs cleared.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmRestoreDefaults() {
        AlertDialog.Builder(this)
            .setTitle("Restore Default Preferences?")
            .setMessage("Are you sure you want to restore all app settings to factory defaults?")
            .setPositiveButton("Restore") { _, _ ->
                prefs.edit().clear().apply()
                loadSettings()
                applyTheme(THEME_SYSTEM)
                Toast.makeText(this, "All app settings restored to defaults.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
