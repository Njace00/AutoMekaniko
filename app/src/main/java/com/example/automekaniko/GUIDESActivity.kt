package com.example.automekaniko

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import com.example.automekaniko.ui.screens.GuidesScreen
import com.example.automekaniko.ui.theme.AutoMekanikoTheme

class GuidesActivity : AppCompatActivity() {

    private val dtcSummaryTextState = mutableStateOf("")
    private val maintSummaryTextState = mutableStateOf("")
    private val isDarkThemeState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        isDarkThemeState.value = isDarkTheme(this)
        refreshVehicleGuideSummaries()

        setContent {
            AutoMekanikoTheme(darkTheme = isDarkThemeState.value) {
                GuidesScreen(
                    dtcSummaryText = dtcSummaryTextState.value,
                    maintSummaryText = maintSummaryTextState.value,
                    onDtcClick = { go(DtcActivity::class.java) },
                    onMaintClick = { go(MAINTAINANCEActivity::class.java) },
                    onHomeClick = { goHome() },
                    onSettingsClick = { goSettings() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isDarkThemeState.value = isDarkTheme(this)
        refreshVehicleGuideSummaries()
        TutorialManager.checkAndRenderStepOnResume(this)
    }

    private fun refreshVehicleGuideSummaries() {
        val activeVehicle = VehicleManager.getActiveVehicle(this)
        val shortVehicleName = activeVehicle.name.replace("Toyota ", "")

        val dtcCount = getDtcGuidesForVehicle(activeVehicle.id).size
        val maintCount = getMaintenanceGuidesForVehicle(activeVehicle.id).size

        dtcSummaryTextState.value = "$dtcCount Interactive Diagnostic Repair Guides for $shortVehicleName • Tap to open"
        maintSummaryTextState.value = "$maintCount Preventive Care Guides for $shortVehicleName with Tool Lists • Tap to open"
        isDarkThemeState.value = isDarkTheme(this)
    }

    private fun isDarkTheme(context: Context): Boolean {
        val prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        return when (prefs.getInt(SettingsActivity.KEY_THEME, SettingsActivity.THEME_LIGHT)) {
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

    private fun <T : Any> go(target: Class<T>) {
        val intent = Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
        ViewAnimationUtils.overrideActivityTransition(this, isEntering = true)
    }
}
