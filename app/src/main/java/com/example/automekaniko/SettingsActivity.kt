package com.example.automekaniko

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf
import com.example.automekaniko.ui.screens.SettingsScreen
import com.example.automekaniko.ui.theme.AutoMekanikoTheme

class SettingsActivity : AppCompatActivity() {

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

    private val isDarkThemeState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        isDarkThemeState.value = isDarkTheme(this)

        setContent {
            AutoMekanikoTheme(darkTheme = isDarkThemeState.value) {
                SettingsScreen(
                    onHomeClick = { goHome() },
                    onBackClick = { finish() },
                    onThemeChanged = { themeMode ->
                        applyAppTheme(themeMode)
                        isDarkThemeState.value = isDarkTheme(this)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isDarkThemeState.value = isDarkTheme(this)
        TutorialManager.checkAndRenderStepOnResume(this)
    }

    private fun isDarkTheme(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return when (prefs.getInt(KEY_THEME, THEME_SYSTEM)) {
            THEME_LIGHT -> false
            THEME_DARK -> true
            else -> {
                val uiMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    private fun applyAppTheme(themeMode: Int) {
        val mode = when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
        ViewAnimationUtils.overrideActivityTransition(this, isEntering = false)
    }
}
