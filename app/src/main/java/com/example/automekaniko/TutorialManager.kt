package com.example.automekaniko

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

object TutorialManager {
    const val PREFS_KEY_COMPLETED = "tutorial_completed"

    var isTutorialActive = false
    var currentStepIndex = 0
    private const val TOTAL_STEPS = 7

    private data class StepSpec(
        val targetActivityClass: Class<out AppCompatActivity>,
        val targetViewId: Int,
        val title: String,
        val description: String
    )

    private val steps = listOf(
        // Step 0 (Step 1 of 7)
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.cardActiveVehicle,
            title = "Step 1 of 7: Active Vehicle Profile",
            description = "Displays your active vehicle (Toyota Vios / Wigo) with engine specs and oil capacity. Tap here or 'Switch' to switch active vehicle profiles anytime!"
        ),
        // Step 1 (Step 2 of 7)
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.cardLive,
            title = "Step 2 of 7: Real-Time OBD-II Telemetry Gauges",
            description = "Displays live engine RPM, speed, coolant temp & fuel trim via Bluetooth. Audio and haptic alerts sound if coolant overheats or engine exceeds redline!"
        ),
        // Step 2 (Step 3 of 7)
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.card3D,
            title = "Step 3 of 7: 3D Interactive Repair Guides",
            description = "Interactive step-by-step repair guides for DTC fault codes and preventive maintenance with 3D model overlays & required tool lists!"
        ),
        // Step 3 (Step 4 of 7)
        StepSpec(
            targetActivityClass = GuidesActivity::class.java,
            targetViewId = R.id.cardDtc,
            title = "Step 4 of 7: Trouble Codes Diagnostic Breakdown",
            description = "View active trouble codes (like P0301 misfire), root cause analysis, multimeter testing specs, and step-by-step repair fixes!"
        ),
        // Step 4 (Step 5 of 7)
        StepSpec(
            targetActivityClass = GuidesActivity::class.java,
            targetViewId = R.id.cardMaintenance,
            title = "Step 5 of 7: Guided Care & Maintenance Toolkit",
            description = "Step-by-step preventive care checklists (Oil & Filter, Air Filter, Battery) with required tools lists (Wrench Set, Jack Stand, Screwdriver, Gloves)!"
        ),
        // Step 5 (Step 6 of 7)
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.btnTutorial,
            title = "Step 6 of 7: Interactive Help & Onboarding Guide",
            description = "Tap this '?' question mark button in the top-right header bar anytime for help or to repeat this interactive onboarding guide!"
        ),
        // Step 6 (Step 7 of 7)
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.navSettings,
            title = "Step 7 of 7: App Preferences & Settings",
            description = "Customize visual theme (Light/Dark), configure OBD auto-connect, toggle gauge audio alerts, reset checklist progress, or clear DTC cache!"
        )
    )

    fun startTutorial(activity: AppCompatActivity) {
        isTutorialActive = true
        currentStepIndex = 0
        renderStepForActivity(activity)
    }

    fun advanceStep(activity: AppCompatActivity) {
        if (!isTutorialActive) return
        currentStepIndex++
        if (currentStepIndex >= TOTAL_STEPS) {
            finishTutorial(activity)
        } else {
            val nextSpec = steps[currentStepIndex]
            if (activity::class.java != nextSpec.targetActivityClass) {
                hideOverlay(activity)
                val intent = Intent(activity, nextSpec.targetActivityClass).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }
                activity.startActivity(intent)
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                renderStepForActivity(activity)
            }
        }
    }

    fun finishTutorial(activity: AppCompatActivity) {
        isTutorialActive = false
        val prefs = activity.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREFS_KEY_COMPLETED, true).apply()
        hideOverlay(activity)

        if (activity !is MainActivity) {
            val intent = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        Toast.makeText(activity, "Tutorial finished! Tap 'Tutorial' top-right anytime to repeat.", Toast.LENGTH_SHORT).show()
    }

    fun checkAndRenderStepOnResume(activity: AppCompatActivity) {
        if (!isTutorialActive) return
        val spec = steps.getOrNull(currentStepIndex) ?: return
        if (spec.targetActivityClass == activity::class.java) {
            val overlay = activity.findViewById<View>(R.id.overlayTutorial)
            overlay?.post {
                renderStepForActivity(activity)
            }
        }
    }

    private fun hideOverlay(activity: AppCompatActivity) {
        val overlay = activity.findViewById<View>(R.id.overlayTutorial) ?: return
        overlay.animate().alpha(0f).setDuration(200L).withEndAction {
            overlay.visibility = View.GONE
        }.start()
    }

    fun renderStepForActivity(activity: AppCompatActivity) {
        val spec = steps.getOrNull(currentStepIndex) ?: return
        if (spec.targetActivityClass != activity::class.java) return

        val overlay = activity.findViewById<ViewGroup>(R.id.overlayTutorial) ?: return
        val viewSpotlight = activity.findViewById<View>(R.id.viewSpotlight)
        val tvStepTitle = activity.findViewById<TextView>(R.id.tvTutorialStepTitle)
        val tvDesc = activity.findViewById<TextView>(R.id.tvTutorialDesc)
        val btnSkip = activity.findViewById<View>(R.id.btnSkipTutorial)

        overlay.visibility = View.VISIBLE
        overlay.alpha = 1f

        overlay.setOnClickListener {
            advanceStep(activity)
        }

        btnSkip?.setOnClickListener {
            finishTutorial(activity)
        }

        tvStepTitle?.text = spec.title
        tvDesc?.text = spec.description

        val targetView = activity.findViewById<View>(spec.targetViewId)
        if (targetView != null && viewSpotlight != null) {
            TransitionManager.beginDelayedTransition(overlay, AutoTransition().apply { duration = 250L })

            val location = IntArray(2)
            targetView.getLocationOnScreen(location)

            val overlayLocation = IntArray(2)
            overlay.getLocationOnScreen(overlayLocation)

            val targetRect = Rect(
                location[0] - overlayLocation[0],
                location[1] - overlayLocation[1],
                location[0] - overlayLocation[0] + targetView.width,
                location[1] - overlayLocation[1] + targetView.height
            )

            val params = viewSpotlight.layoutParams as? ViewGroup.MarginLayoutParams
            if (params != null) {
                params.width = targetRect.width() + 16.toPx(activity)
                params.height = targetRect.height() + 16.toPx(activity)
                params.leftMargin = (targetRect.left - 8.toPx(activity)).coerceAtLeast(0)
                params.topMargin = (targetRect.top - 8.toPx(activity)).coerceAtLeast(0)
                viewSpotlight.layoutParams = params
            }

            val cardDialogue = activity.findViewById<View>(R.id.cardTutorialDialogue)
            val dialogueParams = cardDialogue?.layoutParams as? ConstraintLayout.LayoutParams
            if (dialogueParams != null) {
                val screenHeight = overlay.height.takeIf { it > 0 } ?: activity.resources.displayMetrics.heightPixels
                val targetCenterY = targetRect.centerY()

                // If target is in top half of screen, move dialogue to bottom (bias = 0.82f)
                // If target is in bottom half of screen, move dialogue to top (bias = 0.15f)
                dialogueParams.verticalBias = if (targetCenterY < screenHeight / 2) 0.82f else 0.15f
                cardDialogue.layoutParams = dialogueParams
            }
        }
    }

    private fun Int.toPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
}
