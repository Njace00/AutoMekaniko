package com.example.automekaniko

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.example.automekaniko.ui.screens.DtcScreen
import com.example.automekaniko.ui.theme.AutoMekanikoTheme
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DtcActivity : AppCompatActivity() {

    private var dtcList: List<DtcGuide> = dtcGuides
    private lateinit var sceneViewContainer: View
    private lateinit var sceneView: SceneView
    private lateinit var modelLoader: ModelLoader

    private val activeVehicleState = mutableStateOf(VehicleManager.VIOS)
    private val selectedDtcGuideState = mutableStateOf<DtcGuide?>(null)
    private val currentSlideIndexState = mutableIntStateOf(0)
    private val checkedStepIndicesState = mutableStateOf<Set<Int>>(emptySet())
    private val isDarkThemeState = mutableStateOf(false)

    // Per-Slide Checklist Memory Map
    private val checkedStepsBySlide = mutableMapOf<Int, MutableSet<Int>>()

    private var currentModelNode: ModelNode? = null
    private var currentEntry: DtcGuide? = null

    // Vsync-Driven Animation & Camera Interpolation State
    private var is3DTransitionActive = false
    private var animStartMs: Long = 0L
    private var transitionDurationMs: Long = 1000L

    private var startAnimTime: Float = 0f
    private var targetAnimTime: Float = 0f
    private var lastAppliedAnimTime: Float = -1f

    private var startCameraEye = Vec3(-2.20f, 1.20f, -2.40f)
    private var targetCameraEye = Vec3(-2.20f, 1.20f, -2.40f)
    private var currentCameraEye = Vec3(-2.20f, 1.20f, -2.40f)

    private var startCameraLook = Vec3(0.00f, 0.20f, 0.00f)
    private var targetCameraLook = Vec3(0.00f, 0.20f, 0.00f)
    private var currentCameraLookAt = Vec3(0.00f, 0.20f, 0.00f)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inflate SceneView from XML layout to ensure full Filament engine & lighting initialization
        sceneViewContainer = LayoutInflater.from(this).inflate(R.layout.layout_scene_view, null)
        sceneView = sceneViewContainer.findViewById(R.id.sceneViewInternal)
        modelLoader = ModelLoader(sceneView.engine, this)

        isDarkThemeState.value = isDarkTheme(this)
        lockViewportGestures()
        setupVsyncFrameLoop()
        refreshDtcListForActiveVehicle()

        setContent {
            AutoMekanikoTheme(darkTheme = isDarkThemeState.value) {
                DtcScreen(
                    activeVehicle = activeVehicleState.value,
                    availableDtcGuides = dtcList,
                    selectedDtcGuide = selectedDtcGuideState.value,
                    currentSlideIndex = currentSlideIndexState.intValue,
                    sceneViewInstance = sceneViewContainer,
                    checkedStepIndices = checkedStepIndicesState.value,
                    onVehicleUpdated = { updatedVehicle ->
                        activeVehicleState.value = updatedVehicle
                        VehicleManager.setActiveVehicle(this, updatedVehicle)
                        refreshDtcListForActiveVehicle()
                    },
                    onDtcGuideSelected = { guide ->
                        loadDtcEntry(guide)
                    },
                    onStepCheckedChange = { stepIdx, isChecked ->
                        toggleStepChecked(stepIdx, isChecked)
                    },
                    onNextStepClick = { onNextClicked() },
                    onPrevStepClick = { onPrevClicked() },
                    onBackClick = { finish() },
                    onHomeClick = { goHome() },
                    onSettingsClick = { goSettings() }
                )
            }
        }
    }

    private fun lockViewportGestures() {
        // Lock out manual touch dragging, orbiting, and zoom gestures so the viewport is non-interactive
        sceneView.setOnTouchListener { _, _ -> true }
        sceneView.setOnGenericMotionListener(null)
    }

    private fun setupVsyncFrameLoop() {
        // Synchronize 3D GLB model bone animation and camera pose directly with Filament vsync 60 FPS / 120 FPS render loop
        sceneView.onFrame = { _ ->
            if (is3DTransitionActive) {
                val elapsed = SystemClock.uptimeMillis() - animStartMs
                val rawT = (elapsed.toFloat() / transitionDurationMs).coerceIn(0f, 1f)
                val easedT = easeInOutCubic(rawT)

                // 1. Interpolate GLB bone animation across all tracks
                val currAnimTime = lerp(startAnimTime, targetAnimTime, easedT)
                applyAnimationTime(currAnimTime)

                // 2. Interpolate Camera Eye and LookAt targets
                val eyeX = lerp(startCameraEye.x, targetCameraEye.x, easedT)
                val eyeY = lerp(startCameraEye.y, targetCameraEye.y, easedT)
                val eyeZ = lerp(startCameraEye.z, targetCameraEye.z, easedT)

                val lookX = lerp(startCameraLook.x, targetCameraLook.x, easedT)
                val lookY = lerp(startCameraLook.y, targetCameraLook.y, easedT)
                val lookZ = lerp(startCameraLook.z, targetCameraLook.z, easedT)

                sceneView.cameraNode.position = Position(eyeX, eyeY, eyeZ)
                sceneView.cameraNode.lookAt(Position(lookX, lookY, lookZ))

                currentCameraEye = Vec3(eyeX, eyeY, eyeZ)
                currentCameraLookAt = Vec3(lookX, lookY, lookZ)

                if (rawT >= 1.0f) {
                    is3DTransitionActive = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isDarkThemeState.value = isDarkTheme(this)
        refreshDtcListForActiveVehicle()
        TutorialManager.checkAndRenderStepOnResume(this)
    }

    override fun onPause() {
        is3DTransitionActive = false
        super.onPause()
    }

    private fun refreshDtcListForActiveVehicle() {
        val activeVehicle = VehicleManager.getActiveVehicle(this)
        activeVehicleState.value = activeVehicle
        dtcList = getDtcGuidesForVehicle(activeVehicle.id)
        if (selectedDtcGuideState.value == null) {
            val firstGuide = dtcList.firstOrNull()
            if (TutorialManager.isTutorialActive) {
                selectedDtcGuideState.value = firstGuide
                firstGuide?.let { loadDtcEntry(it) }
            }
        } else if (!dtcList.contains(selectedDtcGuideState.value)) {
            selectedDtcGuideState.value = null
            currentEntry = null
        }
        isDarkThemeState.value = isDarkTheme(this)
    }

    private fun loadDtcEntry(entry: DtcGuide) {
        selectedDtcGuideState.value = entry
        currentEntry = entry
        currentSlideIndexState.intValue = 0
        checkedStepsBySlide.clear()
        checkedStepIndicesState.value = emptySet()
        lastAppliedAnimTime = -1f
        loadGlbModel(entry.glbFile) {
            goToSlide(0)
        }
    }

    private fun goToSlide(slideIndex: Int) {
        val entry = currentEntry ?: return
        val slides = entry.slides
        if (slideIndex !in slides.indices) return

        currentSlideIndexState.intValue = slideIndex
        val savedCheckedForSlide = checkedStepsBySlide[slideIndex] ?: emptySet()
        checkedStepIndicesState.value = savedCheckedForSlide

        val slide = slides[slideIndex]

        // Configure hardware vsync frame interpolation
        startCameraEye = currentCameraEye
        startCameraLook = currentCameraLookAt
        targetCameraEye = slide.eye
        targetCameraLook = slide.lookAt

        startAnimTime = if (lastAppliedAnimTime >= 0f) lastAppliedAnimTime else slide.animationStartTime
        targetAnimTime = slide.animationTime

        transitionDurationMs = slide.animationDurationMs.coerceAtLeast(400L)
        animStartMs = SystemClock.uptimeMillis()
        is3DTransitionActive = true
    }

    private fun applyAnimationTime(time: Float) {
        if (Math.abs(time - lastAppliedAnimTime) < 0.0005f) return
        lastAppliedAnimTime = time
        val animator = currentModelNode?.modelInstance?.animator ?: return
        // Apply animation time across ALL tracks (hood opening, battery, dipstick, oil cap, filter, etc.)
        repeat(animator.animationCount) { i ->
            val duration = animator.getAnimationDuration(i)
            animator.applyAnimation(i, time.coerceIn(0f, duration))
        }
        animator.updateBoneMatrices()
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    private fun easeInOutCubic(t: Float) =
        if (t < 0.5f) 4f * t * t * t
        else 1f - ((-2f * t + 2f).let { it * it * it } / 2f)

    private fun toggleStepChecked(index: Int, isChecked: Boolean) {
        val slideIdx = currentSlideIndexState.intValue
        val slideSet = checkedStepsBySlide.getOrPut(slideIdx) { mutableSetOf() }
        if (isChecked) slideSet.add(index) else slideSet.remove(index)
        checkedStepIndicesState.value = slideSet.toSet()
    }

    private fun onNextClicked() {
        val entry = currentEntry ?: return
        val currentSlide = entry.slides.getOrNull(currentSlideIndexState.intValue) ?: return
        val totalChecklistCount = currentSlide.steps.size
        val completedCount = checkedStepIndicesState.value.size

        // Enforce checklist prerequisite gate
        if (totalChecklistCount > 0 && completedCount < totalChecklistCount) {
            Toast.makeText(this, "Please complete all checklist items for this step first!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentIdx = currentSlideIndexState.intValue
        if (currentIdx < entry.slides.size - 1) {
            goToSlide(currentIdx + 1)
        } else {
            Toast.makeText(this, "Guide Completed! Great job fixing ${entry.code}.", Toast.LENGTH_SHORT).show()
            goToSlide(0)
        }
    }

    private fun onPrevClicked() {
        val currentIdx = currentSlideIndexState.intValue
        if (currentIdx > 0) {
            goToSlide(currentIdx - 1)
        }
    }

    private fun loadGlbModel(glbFile: String, onLoaded: (() -> Unit)? = null) {
        lifecycleScope.launch {
            currentModelNode?.let {
                runCatching {
                    sceneView.removeChildNode(it)
                    it.destroy()
                }
                currentModelNode = null
            }
            delay(150.milliseconds)
            runCatching {
                val instance = modelLoader.createModelInstance(glbFile)
                    ?: modelLoader.createModelInstance("models/$glbFile")
                val node = ModelNode(
                    modelInstance = instance,
                    autoAnimate = false,
                    scaleToUnits = 1.5f
                )
                currentModelNode = node
                sceneView.addChildNode(node)
                onLoaded?.invoke()
            }.onFailure { e ->
                Log.e("DtcActivity", "Failed to load GLB model '$glbFile': ${e.message}", e)
            }
        }
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
}
