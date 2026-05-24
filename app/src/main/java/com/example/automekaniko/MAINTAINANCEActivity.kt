package com.example.automekaniko

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.sceneview.SceneView
import io.github.sceneview.gesture.CameraGestureDetector
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

class MAINTAINANCEActivity : AppCompatActivity() {

    private val guideList = maintenanceGuides

    data class Vec3(val x: Float, val y: Float, val z: Float)

    data class CameraSlide(
        val title: String,
        val description: String,
        val eye: Vec3,
        val lookAt: Vec3,
        val steps: List<String> = emptyList(),
        val animationStartTime: Float = 0f,
        val animationTime: Float = 0f,
        val animationDurationMs: Long = 650L
    )

    private lateinit var sceneView: SceneView
    private lateinit var modelLoader: ModelLoader
    private lateinit var modelSpinner: Spinner

    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnCameraLock: Button
    private lateinit var slideTitle: TextView
    private lateinit var slideDesc: TextView
    private lateinit var tvCameraInfo: TextView
    private lateinit var lockOverlay: View

    // ── Tab views ─────────────────────────────────────────────────────────────
    //private lateinit var tab3D:  TextView
    //private lateinit var tabOBD: TextView

    // ── Checklist overlay ─────────────────────────────────────────────────────
    private lateinit var overlayTitle:       TextView
    private lateinit var checklistContainer: android.widget.LinearLayout

    private var currentModelNode: ModelNode? = null
    private var currentGuide: MaintenanceGuide? = null
    private var currentSlides: List<CameraSlide> = emptyList()
    private var currentSlideIndex = 0
    private var isCameraLocked = true

    private var cameraAnimJob: Job? = null
    private var cameraInfoJob: Job? = null
    private var animScrubJob: Job? = null

    private var currentCameraEye    = Vec3(0f, 0f, 0f)
    private var currentOrbitTarget  = Vec3(0f, 0.5f, 0f)

    private val startEye    = Vec3(0f, 0.945f, -1.22f)
    private val startLookAt = Vec3(0f, 0.5f, 0f)

    private var savedManipulator: CameraGestureDetector.CameraManipulator? = null
    private var manipulatorCaptured = false

    private var lockedAnimTime: Float = 0f
    private var currentAnimTime: Float = 0f
    private var isScrubbing: Boolean = false

    // ─────────────────────────────────────────────────────────────────────────
    //  Lifecycle
    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3d_maintainance)

        // ── "Auto" white, "Mekaniko" red ──────────────────────────────────────
        val appTitle = findViewById<TextView>(R.id.appTitle)
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFFFFFFFF.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        appTitle.text = spannable
        AppNavigation.wire(this)

        sceneView    = findViewById(R.id.sceneView)
        modelSpinner = findViewById(R.id.modelSpinner)
        btnPrev      = findViewById(R.id.btnPrev)
        btnNext      = findViewById(R.id.btnNext)
        btnCameraLock = findViewById(R.id.btnCameraLock)
        slideTitle   = findViewById(R.id.slideTitle)
        slideDesc    = findViewById(R.id.slideDesc)
        tvCameraInfo = findViewById(R.id.tvCameraInfo)
        lockOverlay  = findViewById(R.id.lockOverlay)

        // ── Tab references ────────────────────────────────────────────────────
        //tab3D  = findViewById(R.id.tab3D)
        //tabOBD = findViewById(R.id.tabOBD)

        // ── Checklist overlay ─────────────────────────────────────────────────
        overlayTitle       = findViewById(R.id.overlayTitle)
        checklistContainer = findViewById(R.id.checklistContainer)

        modelLoader = ModelLoader(sceneView.engine, this)

        sceneView.onFrame = { _ ->
            if (!isScrubbing) {
                applyAnimationTime(lockedAnimTime)
            }
        }

        captureManipulatorOnce()
        setupModelSelector()
        setupControls() // ← wire up tab clicks
        setCameraLockState(true)
        startCameraInfoUpdates()
    }

    override fun onDestroy() {
        cameraAnimJob?.cancel()
        cameraInfoJob?.cancel()
        animScrubJob?.cancel()
        super.onDestroy()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Tab navigation
    //  • "3D View"  tab  → this screen (MainActivity) — already here, do nothing
    //  • "OBD Data" tab  → launch OBDActivity
    // ─────────────────────────────────────────────────────────────────────────


    /** Styles a tab TextView as active (gold + bold) or inactive (transparent). */


    // ─────────────────────────────────────────────────────────────────────────
    //  BLE / camera boilerplate (unchanged from original)
    // ─────────────────────────────────────────────────────────────────────────
    private fun captureManipulatorOnce() {
        if (!manipulatorCaptured) {
            savedManipulator = sceneView.cameraManipulator
            manipulatorCaptured = true
        }
    }

    private fun setupModelSelector() {
        if (guideList.isEmpty()) {
            modelSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("No maintenance guides found")
            )
            return
        }

        modelSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            guideList.map { it.name }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(0xFFFFFFFF.toInt())
                loadGuide(guideList[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }

    private fun loadGuide(guide: MaintenanceGuide) {
        currentGuide = guide
        currentSlides = guide.slides
        currentSlideIndex = 0
        loadModel(guide.glbFile)
    }

    private fun setupControls() {
        btnPrev.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            val to = (currentSlideIndex - 1).coerceAtLeast(0)
            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
            updateUiState()
        }

        btnNext.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            val to = (currentSlideIndex + 1).coerceAtMost(currentSlides.lastIndex)
            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
            updateUiState()
        }

        btnCameraLock.setOnClickListener {
            setCameraLockState(!isCameraLocked)
        }

        updateUiState()
    }

    private fun setCameraLockState(locked: Boolean) {
        isCameraLocked = locked

        if (locked) {
            if (savedManipulator == null) savedManipulator = sceneView.cameraManipulator
            sceneView.cameraManipulator = null
            lockOverlay.visibility = View.VISIBLE
            currentModelNode?.isEditable = false
        } else {
            if (sceneView.cameraManipulator == null && savedManipulator != null)
                sceneView.cameraManipulator = savedManipulator
            lockOverlay.visibility = View.GONE
            currentModelNode?.isEditable = true
            val p = sceneView.cameraNode.position
            currentCameraEye = Vec3(p.x, p.y, p.z)
        }

        updateUiState()
    }

    private fun updateUiState() {
        btnCameraLock.text = if (isCameraLocked) "Camera: LOCK" else "Camera: FREE"
        btnPrev.isEnabled  = isCameraLocked && currentSlideIndex > 0
        btnNext.isEnabled  = isCameraLocked && currentSlideIndex < currentSlides.lastIndex
    }

    private fun loadModel(fileName: String) {
        lifecycleScope.launch {
            currentModelNode?.let {
                sceneView.removeChildNode(it)
                it.destroy()
            }

            val instance  = modelLoader.createModelInstance(assetFileLocation = fileName)
            val modelNode = ModelNode(
                modelInstance = instance,
                autoAnimate = false,
                scaleToUnits = 1.5f
            ).apply {
                isEditable = !isCameraLocked
                playingAnimations.clear()
            }

            modelNode.modelInstance.animator?.let { animator ->
                repeat(animator.animationCount) { i -> animator.applyAnimation(i, 0f) }
                animator.updateBoneMatrices()
            }

            sceneView.addChildNode(modelNode)
            currentModelNode = modelNode

            applyCustomStartCamera()

            currentSlideIndex  = 0
            goToSlide(currentSlideIndex, animated = false)
            updateUiState()
        }
    }

    private fun applyAnimationTime(time: Float) {
        val animator = currentModelNode?.modelInstance?.animator ?: return
        lockedAnimTime = time
        currentAnimTime = time
        repeat(animator.animationCount) { i ->
            val duration = animator.getAnimationDuration(i)
            animator.applyAnimation(i, time.coerceIn(0f, duration))
        }
        animator.updateBoneMatrices()
    }

    private fun scrubAnimationTo(
        slideStartTime: Float,
        targetTime: Float,
        durationMs: Long = 650L
    ) {
        animScrubJob?.cancel()
        animScrubJob = lifecycleScope.launch {
            isScrubbing = true

            applyAnimationTime(slideStartTime)

            val steps = 30
            val stepDelay = (durationMs / steps).coerceAtLeast(1L)
            repeat(steps) { i ->
                val t = (i + 1) / steps.toFloat()
                val eased = easeInOutCubic(t)
                applyAnimationTime(lerp(slideStartTime, targetTime, eased))
                delay(stepDelay)
            }

            applyAnimationTime(targetTime)
            isScrubbing = false
        }
    }

    private fun applyCustomStartCamera() {
        setCamera(startEye, startLookAt)
        lifecycleScope.launch {
            delay(32L)
            setCamera(startEye, startLookAt)
        }
    }

    private fun goToSlide(index: Int, animated: Boolean) {
        val slide = currentSlides.getOrNull(index) ?: return
        slideTitle.text = slide.title
        slideDesc.text  = slide.description

        // ── Update checklist overlay ──────────────────────────────────────────
        overlayTitle.text = slide.title
        checklistContainer.removeAllViews()
        slide.steps.forEach { step ->
            val tv = android.widget.TextView(this).apply {
                text = "• $step"
                textSize = 12f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 3, 0, 3)
            }
            checklistContainer.addView(tv)
        }

        if (animated) {
            animateCameraPose(
                startEye  = currentCameraEye,
                startLook = currentOrbitTarget,
                endEye    = slide.eye,
                endLook   = slide.lookAt,
                durationMs = 650L
            )
            scrubAnimationTo(
                slideStartTime = slide.animationStartTime,
                targetTime = slide.animationTime,
                durationMs = slide.animationDurationMs
            )
        } else {
            setCamera(slide.eye, slide.lookAt)
            applyAnimationTime(slide.animationTime)
        }
    }

    private fun setCamera(cameraPos: Vec3, lookTarget: Vec3) {
        currentCameraEye   = cameraPos
        currentOrbitTarget = lookTarget
        sceneView.cameraNode.position = Position(cameraPos.x, cameraPos.y, cameraPos.z)
        sceneView.cameraNode.lookAt(Position(lookTarget.x, lookTarget.y, lookTarget.z))
    }

    private fun animateCameraPose(
        startEye: Vec3, startLook: Vec3,
        endEye: Vec3,   endLook: Vec3,
        durationMs: Long
    ) {
        cameraAnimJob?.cancel()
        cameraAnimJob = lifecycleScope.launch {
            val steps     = 30
            val stepDelay = (durationMs / steps).coerceAtLeast(1L)

            repeat(steps) { i ->
                val t     = (i + 1) / steps.toFloat()
                val eased = easeInOutCubic(t)
                val eye   = Vec3(lerp(startEye.x, endEye.x, eased), lerp(startEye.y, endEye.y, eased), lerp(startEye.z, endEye.z, eased))
                val look  = Vec3(lerp(startLook.x, endLook.x, eased), lerp(startLook.y, endLook.y, eased), lerp(startLook.z, endLook.z, eased))
                setCamera(eye, look)
                delay(stepDelay)
            }
            setCamera(endEye, endLook)
        }
    }

    private fun startCameraInfoUpdates() {
        cameraInfoJob?.cancel()
        cameraInfoJob = lifecycleScope.launch {
            while (true) {
                val p   = sceneView.cameraNode.position
                val cam = Vec3(p.x, p.y, p.z)
                if (!isCameraLocked) currentCameraEye = cam

                val target   = currentOrbitTarget
                val dx       = cam.x - target.x
                val dy       = cam.y - target.y
                val dz       = cam.z - target.z
                val distance = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(0.0001f)
                val yaw      = Math.toDegrees(atan2(dx, dz).toDouble()).toFloat()
                val pitch    = Math.toDegrees(asin((dy / distance).toDouble())).toFloat()

                tvCameraInfo.text =
                    "eye=(%.2f, %.2f, %.2f) target=(%.2f, %.2f, %.2f) d=%.2f yaw=%.1f pitch=%.1f"
                        .format(cam.x, cam.y, cam.z, target.x, target.y, target.z, distance, yaw, pitch)

                delay(120L)
            }
        }
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    private fun easeInOutCubic(t: Float) =
        if (t < 0.5f) 4f * t * t * t
        else 1f - ((-2f * t + 2f).let { it * it * it } / 2f)
}
