package com.example.automekaniko

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import com.example.automekaniko.ui.screens.HomeScreen
import com.example.automekaniko.ui.theme.AutoMekanikoTheme

class MainActivity : AppCompatActivity() {

    private val activeVehicleState = mutableStateOf(VehicleManager.VIOS)
    private val isDarkThemeState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Load current active vehicle and theme preference
        activeVehicleState.value = VehicleManager.getActiveVehicle(this)
        isDarkThemeState.value = isDarkTheme(this)

        setContent {
            AutoMekanikoTheme(darkTheme = isDarkThemeState.value) {
                HomeScreen(
                    activeVehicle = activeVehicleState.value,
                    onVehicleUpdated = { updatedVehicle ->
                        activeVehicleState.value = updatedVehicle
                    },
                    onGuidesClick = { go(GuidesActivity::class.java) },
                    onLiveClick = { go(OBDActivity::class.java) },
                    onSettingsClick = { go(SettingsActivity::class.java) },
                    onTutorialClick = { TutorialManager.startTutorial(this) }
                )
            }
        }

        // Auto-run tutorial on first launch
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val isCompleted = prefs.getBoolean(TutorialManager.PREFS_KEY_COMPLETED, false)
        if (!isCompleted && !TutorialManager.isTutorialActive) {
            window.decorView.post {
                TutorialManager.startTutorial(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activeVehicleState.value = VehicleManager.getActiveVehicle(this)
        isDarkThemeState.value = isDarkTheme(this)
        TutorialManager.checkAndRenderStepOnResume(this)
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

    private fun <T : Any> go(target: Class<T>) {
        startActivity(Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
        ViewAnimationUtils.overrideActivityTransition(this, isEntering = true)
    }
}
