package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class GuidesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guides)

        // ── Header Branding ──────────────────────────────────────────────────
        AppNavigation.setupBrandedTitle(this, findViewById(R.id.appTitle))
        AppNavigation.wire(this)

        findViewById<CardView>(R.id.cardDtc).setOnClickListener {
            go(DtcActivity::class.java)
        }

        findViewById<CardView>(R.id.cardMaintenance).setOnClickListener {
            go(MAINTAINANCEActivity::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshVehicleGuideSummaries()
    }

    private fun refreshVehicleGuideSummaries() {
        val activeVehicle = VehicleManager.getActiveVehicle(this)
        val shortVehicleName = activeVehicle.name.replace("Toyota ", "")

        val dtcCount = getDtcGuidesForVehicle(activeVehicle.id).size
        val maintCount = getMaintenanceGuidesForVehicle(activeVehicle.id).size

        val tvDtcSummary = findViewById<TextView>(R.id.tvDtcSummary)
        val tvMaintSummary = findViewById<TextView>(R.id.tvMaintSummary)

        tvDtcSummary?.text = "$dtcCount Interactive Diagnostic Repair Guides for $shortVehicleName • Tap to open"
        tvMaintSummary?.text = "$maintCount Preventive Care Guides for $shortVehicleName with Tool Lists • Tap to open"
    }

    private fun <T : Any> go(target: Class<T>) {
        val intent = Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
