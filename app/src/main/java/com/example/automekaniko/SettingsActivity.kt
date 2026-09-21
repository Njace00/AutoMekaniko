package com.example.automekaniko

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
        loadSettings()
        setupListeners()
        AppNavigation.wire(this)
    }

    private fun setupTitle() {
        AppNavigation.setupBrandedTitle(this, binding.appTitle)
    }

    private fun loadSettings() {
        // Theme
        val theme = prefs.getInt(KEY_THEME, THEME_SYSTEM)
        when (theme) {
            THEME_LIGHT -> binding.rbThemeLight.isChecked = true
            THEME_DARK -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        // Units
        binding.switchMetric.isChecked = prefs.getBoolean(KEY_UNITS_METRIC, true)

        // Dashboard
        binding.etRedline.setText(prefs.getInt(KEY_REDLINE, 6500).toString())
        binding.switchKeepScreenOn.isChecked = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false)

        // Checklist
        binding.switchAutoExpand.isChecked = prefs.getBoolean(KEY_AUTO_EXPAND, false)
    }

    private fun setupListeners() {
        // Theme RadioGroup
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbThemeLight -> THEME_LIGHT
                R.id.rbThemeDark -> THEME_DARK
                else -> THEME_SYSTEM
            }
            prefs.edit().putInt(KEY_THEME, mode).apply()
            applyTheme(mode)
        }

        // Units Switch
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

        // Reset Button
        binding.btnResetChecklists.setOnClickListener {
            resetChecklistProgress()
        }
    }

    private fun applyTheme(themeMode: Int) {
        val mode = when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun resetChecklistProgress() {
        val checklistPrefs = getSharedPreferences(MAINTAINANCEActivity.PREFS_CHECKLIST, Context.MODE_PRIVATE)
        checklistPrefs.edit().clear().apply()
        Toast.makeText(this, "All checklist progress has been reset.", Toast.LENGTH_SHORT).show()
    }
}
