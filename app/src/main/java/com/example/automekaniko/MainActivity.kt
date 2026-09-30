package com.example.automekaniko

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ── Header Branding ──────────────────────────────────────────────────
        AppNavigation.setupBrandedTitle(this, findViewById(R.id.appTitle))
        AppNavigation.wire(this)

        setupVehicleBanner()

        // Cards (the visible clickable areas)
        val card3D   = findViewById<CardView>(R.id.card3D)
        val cardLive = findViewById<CardView>(R.id.cardLive)

        // Hidden buttons kept for backward compat — wire them too just in case
        val viewBtn = findViewById<Button>(R.id.viewbtn)
        val liveBtn = findViewById<Button>(R.id.livebtn)

        // Cards
        card3D.setOnClickListener   { go(GuidesActivity::class.java) }
        cardLive.setOnClickListener { go(OBDActivity::class.java) }

        // Hidden buttons (fallback)
        viewBtn.setOnClickListener { go(GuidesActivity::class.java) }
        liveBtn.setOnClickListener { go(OBDActivity::class.java) }

        // Tactile Press Micro-Interactions
        val cardActiveVehicle = findViewById<View>(R.id.cardActiveVehicle)
        val btnSwitchVehicle  = findViewById<View>(R.id.btnSwitchVehicle)
        val btnTutorial       = findViewById<View>(R.id.btnTutorial)
        ViewAnimationUtils.applyPressScaleToAll(cardActiveVehicle, btnSwitchVehicle, card3D, cardLive, btnTutorial)

        // Staggered Card Entrance Cascade
        ViewAnimationUtils.animateEntranceCascade(listOf(cardActiveVehicle, card3D, cardLive))

        // Tutorial re-run button
        btnTutorial?.setOnClickListener {
            TutorialManager.startTutorial(this)
        }

        // Auto-run tutorial on first launch
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val isCompleted = prefs.getBoolean(TutorialManager.PREFS_KEY_COMPLETED, false)
        if (!isCompleted && !TutorialManager.isTutorialActive) {
            findViewById<View>(R.id.scrollContentContainer)?.post {
                TutorialManager.startTutorial(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateVehicleBannerUI()
        TutorialManager.checkAndRenderStepOnResume(this)
    }

    private fun setupVehicleBanner() {
        updateVehicleBannerUI()
        val cardActiveVehicle = findViewById<View>(R.id.cardActiveVehicle)
        val btnSwitchVehicle = findViewById<View>(R.id.btnSwitchVehicle)

        val listener = View.OnClickListener {
            VehicleManager.showSelectorDialog(this) { vehicle ->
                updateVehicleBannerUI()
            }
        }
        cardActiveVehicle?.setOnClickListener(listener)
        btnSwitchVehicle?.setOnClickListener(listener)
    }

    private fun updateVehicleBannerUI() {
        val vehicle = VehicleManager.getActiveVehicle(this)
        findViewById<TextView>(R.id.tvVehicleName)?.text = vehicle.name
        findViewById<TextView>(R.id.tvVehicleEngine)?.text = "${vehicle.engine} • ${vehicle.oilCapacity} Oil"
    }

    private fun <T : Any> go(target: Class<T>) {
        startActivity(Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
        ViewAnimationUtils.overrideActivityTransition(this, isEntering = true)
    }
}
