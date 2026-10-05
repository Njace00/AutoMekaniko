package com.example.automekaniko

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import com.example.automekaniko.ui.components.TutorialOverlay

object TutorialManager {
    const val PREFS_KEY_COMPLETED = "tutorial_completed"

    private val COMPOSE_OVERLAY_ID = View.generateViewId()

    val isTutorialActiveState = mutableStateOf(false)
    val currentStepIndexState = mutableIntStateOf(0)

    var isTutorialActive: Boolean
        get() = isTutorialActiveState.value
        set(value) { isTutorialActiveState.value = value }

    var currentStepIndex: Int
        get() = currentStepIndexState.intValue
        set(value) { currentStepIndexState.intValue = value }

    val boundsMap = mutableStateMapOf<Int, Rect>()

    private data class StepSpec(
        val targetActivityClass: Class<out AppCompatActivity>,
        val targetViewId: Int,
        val title: String,
        val description: String
    )

    private val steps = listOf(
        // Step 1: Active Vehicle
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.cardActiveVehicle,
            title = "Active Vehicle Profile",
            description = "Displays your active vehicle (Toyota Vios / Wigo) with engine specs and oil capacity. Tap here or 'Switch' to switch active vehicle profiles anytime!"
        ),
        // Step 2: OBD Telemetry
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.cardLive,
            title = "Real-Time OBD-II Telemetry Gauges",
            description = "Displays live engine RPM, speed, coolant temp & fuel trim via Bluetooth. Audio and haptic alerts sound if coolant overheats or engine exceeds redline!"
        ),
        // Step 3: 3D Guides Hub
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.card3D,
            title = "3D Interactive Repair Guides",
            description = "Interactive step-by-step repair guides for DTC fault codes and preventive maintenance with 3D model overlays & required tool lists!"
        ),
        // Step 4: DTC Card in Guides Hub
        StepSpec(
            targetActivityClass = GuidesActivity::class.java,
            targetViewId = R.id.cardDtc,
            title = "Trouble Codes Diagnostic Hub",
            description = "View active trouble codes (like P0301 misfire or P2118 throttle motor), root cause analysis, multimeter testing specs, and step-by-step repair fixes!"
        ),
        // Step 5: Inside DTC Screen - Dropdown Selector
        StepSpec(
            targetActivityClass = DtcActivity::class.java,
            targetViewId = R.id.menuDtc,
            title = "DTC Fault Code Selection",
            description = "Tap this dropdown menu to select specific diagnostic trouble codes (e.g. P0301 Cylinder Misfire, P2118 Throttle Control) for your active vehicle!"
        ),
        // Step 6: Inside DTC Screen - Tools & Prep
        StepSpec(
            targetActivityClass = DtcActivity::class.java,
            targetViewId = R.id.btnToolsPrep,
            title = "Required Tools & Safety Prep",
            description = "Tap 'Tools & Prep' anytime to view required tools (Multimeter, Socket Wrench), estimated repair time, difficulty level, and safety rules!"
        ),
        // Step 7: Inside DTC Screen - Interactive Checklist
        StepSpec(
            targetActivityClass = DtcActivity::class.java,
            targetViewId = R.id.btnOverviewModern,
            title = "Interactive Repair Checklist",
            description = "Tap this Checklist icon to open the step-by-step repair checklist! Check off completed steps and read safety warnings as you fix the issue."
        ),
        // Step 8: Inside DTC Screen - Tech Specs & Info Panel
        StepSpec(
            targetActivityClass = DtcActivity::class.java,
            targetViewId = R.id.btnInfoModern,
            title = "Technical Diagnostic Info & Specs",
            description = "Tap this Info icon to view component multimeter testing specs, harness pinouts, component location notes, and root cause diagnostic guides."
        ),
        // Step 9: Inside DTC Screen - 3D Step Controls
        StepSpec(
            targetActivityClass = DtcActivity::class.java,
            targetViewId = R.id.navButtonsSection,
            title = "3D Step Animation Controls",
            description = "Use 'Next Step' and 'Prev' to rotate 3D animations and navigate step-by-step through component teardowns and reassembly!"
        ),
        // Step 10: Maintenance Card in Guides Hub
        StepSpec(
            targetActivityClass = GuidesActivity::class.java,
            targetViewId = R.id.cardMaintenance,
            title = "Guided Care & Maintenance Toolkit",
            description = "Step-by-step preventive care guides (Engine Oil & Filter, Air Filter, Battery) with required tools lists and recommended service intervals!"
        ),
        // Step 11: Inside Maintenance Screen - Guide Selector
        StepSpec(
            targetActivityClass = MAINTAINANCEActivity::class.java,
            targetViewId = R.id.menuDtc,
            title = "Preventive Maintenance Selector",
            description = "Select preventive care procedures like Engine Oil & Filter Change, Engine Air Filter Replacement, or Battery Replacement!"
        ),
        // Step 12: Inside Maintenance Screen - Tools & Prep
        StepSpec(
            targetActivityClass = MAINTAINANCEActivity::class.java,
            targetViewId = R.id.btnToolsPrep,
            title = "Required Tools & Maintenance Prep",
            description = "Tap 'Tools & Prep' to check required tools (Oil Filter Wrench, Drain Pan, Gloves), vehicle prep checklist, and time estimates."
        ),
        // Step 13: Inside Maintenance Screen - Interactive Checklist
        StepSpec(
            targetActivityClass = MAINTAINANCEActivity::class.java,
            targetViewId = R.id.btnOverviewModern,
            title = "Maintenance Checklist & Verification",
            description = "Tap the Checklist icon to follow step-by-step instructions. Check off each task as you complete it to track progress!"
        ),
        // Step 14: Inside Maintenance Screen - Fluid & Tech Specs
        StepSpec(
            targetActivityClass = MAINTAINANCEActivity::class.java,
            targetViewId = R.id.btnInfoModern,
            title = "Spec Details & Fluid Capacities",
            description = "Tap the Info icon to view oil capacities (e.g., 3.3L 0W-20), oil filter part numbers, tightening torque specs, and maintenance tips."
        ),
        // Step 15: Inside Maintenance Screen - 3D Teardown Controls
        StepSpec(
            targetActivityClass = MAINTAINANCEActivity::class.java,
            targetViewId = R.id.navButtonsSection,
            title = "3D Teardown & Reassembly Controls",
            description = "Step through 3D maintenance animations using 'Next Step'. Complete all checklist items to unlock subsequent repair phases!"
        ),
        // Step 16: Tutorial Help Button
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.btnTutorial,
            title = "Interactive Help & Onboarding Guide",
            description = "Tap this '?' question mark button in the top-right header bar anytime for help or to repeat this interactive onboarding guide!"
        ),
        // Step 17: Settings
        StepSpec(
            targetActivityClass = MainActivity::class.java,
            targetViewId = R.id.navSettings,
            title = "App Preferences & Settings",
            description = "Customize visual theme (Light/Dark), configure OBD auto-connect, toggle gauge audio alerts, reset checklist progress, or clear DTC cache!"
        )
    )

    var onStepUpdated: (() -> Unit)? = null

    data class StepInfo(
        val targetActivityClass: Class<out AppCompatActivity>,
        val targetViewId: Int,
        val title: String,
        val description: String,
        val stepIndex: Int,
        val totalSteps: Int
    )

    fun getCurrentStepInfo(): StepInfo? {
        if (!isTutorialActiveState.value) return null
        val spec = steps.getOrNull(currentStepIndexState.intValue) ?: return null
        return StepInfo(
            targetActivityClass = spec.targetActivityClass,
            targetViewId = spec.targetViewId,
            title = spec.title,
            description = spec.description,
            stepIndex = currentStepIndexState.intValue,
            totalSteps = steps.size
        )
    }

    fun startTutorial(activity: AppCompatActivity) {
        isTutorialActiveState.value = true
        currentStepIndexState.intValue = 0
        onStepUpdated?.invoke()
        renderStepForActivity(activity)
    }

    fun advanceStep(activity: AppCompatActivity) {
        if (!isTutorialActiveState.value) return
        currentStepIndexState.intValue++
        onStepUpdated?.invoke()
        if (currentStepIndexState.intValue >= steps.size) {
            finishTutorial(activity)
        } else {
            val nextSpec = steps[currentStepIndexState.intValue]
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
        isTutorialActiveState.value = false
        currentStepIndexState.intValue = 0
        boundsMap.clear()
        onStepUpdated?.invoke()

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

        Toast.makeText(activity, "Tutorial finished! Tap '?' top-right anytime to repeat.", Toast.LENGTH_SHORT).show()
    }

    fun checkAndRenderStepOnResume(activity: AppCompatActivity) {
        if (!isTutorialActiveState.value) return
        val spec = steps.getOrNull(currentStepIndexState.intValue) ?: return
        if (spec.targetActivityClass == activity::class.java) {
            renderStepForActivity(activity)
        }
    }

    private fun hideOverlay(activity: AppCompatActivity) {
        // Clean up legacy ComposeView overlays if present
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val composeOverlay = rootView.findViewById<View>(COMPOSE_OVERLAY_ID) ?: return
        rootView.removeView(composeOverlay)
    }

    fun renderStepForActivity(activity: AppCompatActivity) {
        if (!isTutorialActiveState.value) return
        val spec = steps.getOrNull(currentStepIndexState.intValue) ?: return
        if (spec.targetActivityClass != activity::class.java) return

        val targetView = activity.findViewById<View>(spec.targetViewId)
        if (targetView != null) {
            targetView.post {
                val location = IntArray(2)
                targetView.getLocationOnScreen(location)
                boundsMap[spec.targetViewId] = Rect(
                    location[0],
                    location[1],
                    location[0] + targetView.width,
                    location[1] + targetView.height
                )
                onStepUpdated?.invoke()
            }
        }

        // All activities now render TutorialOverlay directly inside their root AutoMekanikoTheme Compose tree
        onStepUpdated?.invoke()
    }
}

// Extension Modifier to easily mark Compose elements for tutorial targeting
fun Modifier.tutorialTarget(viewId: Int): Modifier = this.onGloballyPositioned { coordinates ->
    val position = coordinates.positionInWindow()
    val size = coordinates.size
    TutorialManager.boundsMap[viewId] = Rect(
        position.x.toInt(),
        position.y.toInt(),
        (position.x + size.width).toInt(),
        (position.y + size.height).toInt()
    )
}
