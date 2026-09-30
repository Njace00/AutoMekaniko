package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.chip.Chip

class GuidesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guides)

        // ── Header Branding ──────────────────────────────────────────────────
        AppNavigation.setupBrandedTitle(this, findViewById(R.id.appTitle))
        AppNavigation.wire(this)

        // ── Navigation Click Listeners ───────────────────────────────────────
        val launchDtc = View.OnClickListener { go(DtcActivity::class.java) }
        findViewById<View>(R.id.headerDtc)?.setOnClickListener(launchDtc)
        findViewById<View>(R.id.cardDtc)?.setOnClickListener(launchDtc)
        findViewById<View>(R.id.tvDtcSummary)?.setOnClickListener(launchDtc)

        val launchMaint = View.OnClickListener { go(MAINTAINANCEActivity::class.java) }
        findViewById<View>(R.id.headerMaint)?.setOnClickListener(launchMaint)
        findViewById<View>(R.id.cardMaintenance)?.setOnClickListener(launchMaint)
        findViewById<View>(R.id.tvMaintSummary)?.setOnClickListener(launchMaint)

        var isDtcExpanded = false
        val toggleDtcExpand = {
            isDtcExpanded = !isDtcExpanded
            val cardDtc = findViewById<ViewGroup>(R.id.cardDtc)
            if (cardDtc != null) {
                TransitionManager.beginDelayedTransition(cardDtc, AutoTransition().apply { duration = 250L })
            }
            findViewById<View>(R.id.expandableDtcSection)?.visibility =
                if (isDtcExpanded) View.VISIBLE else View.GONE
            findViewById<View>(R.id.containerDtcPeek)?.visibility =
                if (isDtcExpanded) View.GONE else View.VISIBLE
            findViewById<Chip>(R.id.btnDtcOverview)?.text =
                if (isDtcExpanded) "📋 Hide Trouble Codes ▴" else "📋 Trouble Codes Overview ▾"
        }

        val cardDtc = findViewById<View>(R.id.cardDtc)
        val cardMaint = findViewById<View>(R.id.cardMaintenance)
        val btnDtcOverview = findViewById<View>(R.id.btnDtcOverview)
        val btnMaintOverview = findViewById<View>(R.id.btnMaintOverview)

        // Tactile Press Micro-Interactions
        ViewAnimationUtils.applyPressScaleToAll(cardDtc, cardMaint, btnDtcOverview, btnMaintOverview)

        // Staggered Entrance Cascade
        ViewAnimationUtils.animateEntranceCascade(listOf(cardDtc, cardMaint))

        findViewById<View>(R.id.btnDtcOverview)?.setOnClickListener { toggleDtcExpand() }
        findViewById<View>(R.id.containerDtcPeek)?.setOnClickListener { toggleDtcExpand() }

        var isMaintExpanded = false
        val toggleMaintExpand = {
            isMaintExpanded = !isMaintExpanded
            val cardMaintViewGroup = findViewById<ViewGroup>(R.id.cardMaintenance)
            if (cardMaintViewGroup != null) {
                TransitionManager.beginDelayedTransition(cardMaintViewGroup, AutoTransition().apply { duration = 250L })
            }
            findViewById<View>(R.id.expandableMaintSection)?.visibility =
                if (isMaintExpanded) View.VISIBLE else View.GONE
            findViewById<View>(R.id.containerMaintPeek)?.visibility =
                if (isMaintExpanded) View.GONE else View.VISIBLE
            findViewById<Chip>(R.id.btnMaintOverview)?.text =
                if (isMaintExpanded) "🧰 Hide Toolkit & Procedures ▴" else "🧰 Toolkit & Procedures ▾"
        }

        findViewById<View>(R.id.btnMaintOverview)?.setOnClickListener { toggleMaintExpand() }
        findViewById<View>(R.id.containerMaintPeek)?.setOnClickListener { toggleMaintExpand() }
    }

    override fun onResume() {
        super.onResume()
        refreshVehicleGuideSummaries()
        TutorialManager.checkAndRenderStepOnResume(this)
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
        ViewAnimationUtils.overrideActivityTransition(this, isEntering = true)
    }
}
