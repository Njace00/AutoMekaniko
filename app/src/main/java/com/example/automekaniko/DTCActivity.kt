package com.example.automekaniko

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import io.github.sceneview.SceneView
import io.github.sceneview.gesture.CameraGestureDetector
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DtcActivity : AppCompatActivity() {

    private val dtcList = dtcGuides

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private lateinit var sceneView:          SceneView
    private lateinit var modelLoader:        ModelLoader
    private lateinit var dtcSpinner:         Spinner
    private lateinit var topScroll:          ScrollView
    private lateinit var slidePanel:         ConstraintLayout
    private lateinit var checklistOverlay:   LinearLayout
    private lateinit var btnPrev:            Button
    private lateinit var btnNext:            Button
    private lateinit var slideTitle:         TextView
    private lateinit var slideDesc:          TextView
    private lateinit var lockOverlay:        View
    private lateinit var overlayTitle:       TextView
    private lateinit var checklistContainer: LinearLayout
    private lateinit var infoCard:           CardView
    private lateinit var tvDtcCode:          TextView
    private lateinit var tvDtcName:          TextView
    private lateinit var tvDtcDesc:          TextView
    private lateinit var partsSection:       LinearLayout
    private lateinit var partsContainer:     LinearLayout

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private var currentModelNode:  ModelNode? = null
    private var currentEntry:      DtcGuide?  = null
    private var currentSlideIndex: Int        = 0
    private var isCameraLocked:    Boolean    = true

    private var cameraAnimJob: Job? = null
    private var animScrubJob:  Job? = null

    private var currentCameraEye   = Vec3(0f, 0f, 0f)
    private var currentOrbitTarget = Vec3(0f, 0.5f, 0f)

    private val startEye    = Vec3(0f, 0.945f, -1.22f)
    private val startLookAt = Vec3(0f, 0.5f, 0f)

    private var savedManipulator:    CameraGestureDetector.CameraManipulator? = null
    private var manipulatorCaptured: Boolean = false

    private var currentAnimTime: Float = 0f

    // SceneView's render loop auto-advances the animator every frame.
    // We freeze the pose by re-stamping lockedAnimTime on every onFrame callback.
    // During scrubbing this tracks the in-progress value; at rest it holds the
    // target slide time so the pose stays frozen between slides.
    private var lockedAnimTime: Float = 0f

    // FIX: When true, onFrame backs off so the scrub coroutine has full control
    // of the animator. Without this flag, onFrame re-stamps the old lockedAnimTime
    // on every render frame between coroutine delay() steps, causing the model to
    // snap back to the previous slide's pose mid-scrub.
    private var isScrubbing: Boolean = false

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3d_dtc_guide)

        // ── "Auto" white, "Mekaniko" red ──────────────────────────────────────
        val appTitle = findViewById<TextView>(R.id.appTitle)
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFFFFFFFF.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        appTitle.text = spannable
        AppNavigation.wire(this)

        sceneView          = findViewById(R.id.sceneView)
        dtcSpinner         = findViewById(R.id.dtcSpinner)
        topScroll          = findViewById(R.id.topScroll)
        slidePanel         = findViewById(R.id.slidePanel)
        checklistOverlay   = findViewById(R.id.checklistOverlay)
        btnPrev            = findViewById(R.id.btnPrev)
        btnNext            = findViewById(R.id.btnNext)
        slideTitle         = findViewById(R.id.slideTitle)
        slideDesc          = findViewById(R.id.slideDesc)
        lockOverlay        = findViewById(R.id.lockOverlay)
        overlayTitle       = findViewById(R.id.overlayTitle)
        checklistContainer = findViewById(R.id.checklistContainer)
        infoCard           = findViewById(R.id.infoCard)
        tvDtcCode          = findViewById(R.id.tvDtcCode)
        tvDtcName          = findViewById(R.id.tvDtcName)
        tvDtcDesc          = findViewById(R.id.tvDtcDesc)
        partsSection       = findViewById(R.id.partsSection)
        partsContainer     = findViewById(R.id.partsContainer)

        modelLoader = ModelLoader(sceneView.engine, this)

        // Re-stamp lockedAnimTime on every frame across ALL tracks so SceneView's
        // internal render loop cannot advance the animation past our frozen pose.
        // FIX: Only stamp when NOT scrubbing — during a scrub the coroutine owns
        // lockedAnimTime and onFrame must not interfere.
        sceneView.onFrame = { _ ->
            if (!isScrubbing) {
                val animator = currentModelNode?.modelInstance?.animator
                if (animator != null) {
                    applyAnimationClips(animator, lockedAnimTime)
                    animator.updateBoneMatrices()
                }
            }
        }

        captureManipulatorOnce()
        setupDtcSpinner()
        setupControls()
        setCameraLockState(true)
    }

    override fun onDestroy() {
        cameraAnimJob?.cancel()
        animScrubJob?.cancel()
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // Spinner
    // -------------------------------------------------------------------------

    private fun setupDtcSpinner() {
        val labels = listOf("Select a DTC code...") +
                dtcList.map { "${it.code}  —  ${it.name}" }

        dtcSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            labels
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        dtcSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                if (position == 0) return
                loadDtcEntry(dtcList[position - 1])
            }
            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }

    // -------------------------------------------------------------------------
    // Load entry
    // -------------------------------------------------------------------------

    private fun loadDtcEntry(entry: DtcGuide) {
        currentEntry      = entry
        currentSlideIndex = 0
        currentAnimTime   = 0f
        lockedAnimTime    = 0f

        tvDtcCode.text = entry.code
        tvDtcName.text = entry.name
        tvDtcDesc.text = entry.description

        partsContainer.removeAllViews()
        entry.parts.forEach { part ->
            val tv = TextView(this).apply {
                text     = "• $part"
                textSize = 11f
                setTextColor(0xFFcccccc.toInt())
                setPadding(0, 2, 0, 2)
            }
            partsContainer.addView(tv)
        }

        infoCard.visibility         = View.VISIBLE
        partsSection.visibility     = View.VISIBLE
        slidePanel.visibility       = View.VISIBLE
        checklistOverlay.visibility = View.VISIBLE

        loadModel(entry.glbFile, entry.slides)
    }

    // -------------------------------------------------------------------------
    // Model loading
    // -------------------------------------------------------------------------

    private fun loadModel(fileName: String, slides: List<DtcSlide>) {
        lifecycleScope.launch {
            currentModelNode?.let {
                sceneView.removeChildNode(it)
                it.destroy()
                currentModelNode = null
            }

            Log.d("DtcActivity", "Loading GLB: $fileName")
            val instance = try {
                modelLoader.createModelInstance(assetFileLocation = fileName)
            } catch (e: Exception) {
                Log.e("DtcActivity", "Exception loading GLB: $fileName", e)
                null
            }

            if (instance == null) {
                Log.e("DtcActivity", "GLB not found or failed to load: $fileName")
                Toast.makeText(
                    this@DtcActivity,
                    "Could not load model: $fileName\nCheck assets folder.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val modelNode = ModelNode(
                modelInstance = instance,
                autoAnimate = false,
                scaleToUnits = 1.5f
            ).apply {
                isEditable = !isCameraLocked
                playingAnimations.clear()
            }

            sceneView.addChildNode(modelNode)
            currentModelNode = modelNode

            // Lock at time 0 immediately so onFrame holds the first pose
            lockedAnimTime = 0f
            applyAnimationTime(0f)
            applyCustomStartCamera()

            currentSlideIndex = 0
            goToSlide(0, animated = false)
        }
    }

    private fun applyCustomStartCamera() {
        setCamera(startEye, startLookAt)
        lifecycleScope.launch {
            delay(32L)
            setCamera(startEye, startLookAt)
        }
    }

    // -------------------------------------------------------------------------
    // Animation — stamp a pose, then lock there
    // -------------------------------------------------------------------------

    // Stamps the given global time across ALL animation tracks and updates lockedAnimTime
    // so onFrame continues holding that pose every subsequent frame. Some GLB clips
    // end earlier than the full tutorial timeline, so clamp each clip individually
    // to keep completed parts frozen at their last keyed pose.
    private fun applyAnimationTime(time: Float) {
        val animator = currentModelNode?.modelInstance?.animator ?: return
        lockedAnimTime  = time
        currentAnimTime = time
        applyAnimationClips(animator, time)
        animator.updateBoneMatrices()
    }

    private fun applyAnimationClips(
        animator: com.google.android.filament.gltfio.Animator,
        time: Float
    ) {
        val clipStartTimes = currentEntry?.animationClipStartTimes.orEmpty()
        repeat(animator.animationCount) { i ->
            val clipStartTime = clipStartTimes.getOrNull(i) ?: 0f
            if (time >= clipStartTime) {
                animator.applyAnimation(i, clampedAnimationTime(animator, i, time))
            }
        }
    }

    private fun clampedAnimationTime(
        animator: com.google.android.filament.gltfio.Animator,
        animationIndex: Int,
        time: Float
    ): Float {
        val duration = animator.getAnimationDuration(animationIndex)
        return time.coerceIn(0f, duration)
    }

    // Per-slide scrub:
    //   1. Set isScrubbing = true so onFrame backs off completely
    //   2. Instantly JUMP to slideStartTime (no interpolation — skips irrelevant frames)
    //   3. Smoothly SCRUB from slideStartTime → targetTime
    //   4. FREEZE at targetTime, then hand control back to onFrame via isScrubbing = false
    private fun scrubAnimationTo(
        slideStartTime: Float,
        targetTime: Float,
        durationMs: Long = 650L
    ) {
        animScrubJob?.cancel()
        animScrubJob = lifecycleScope.launch {

            // FIX: Disable onFrame stamping so it cannot fight our jump/scrub
            isScrubbing = true

            // Step 1: instant jump to the slide's start frame
            applyAnimationTime(slideStartTime)

            // Step 2: smooth scrub from start → target
            val steps     = 30
            val stepDelay = (durationMs / steps).coerceAtLeast(1L)
            repeat(steps) { i ->
                val t     = (i + 1) / steps.toFloat()
                val eased = easeInOutCubic(t)
                applyAnimationTime(lerp(slideStartTime, targetTime, eased))
                delay(stepDelay)
            }

            // Step 3: snap to exact target and freeze
            applyAnimationTime(targetTime)

            // FIX: Hand control back to onFrame — it will now hold this pose every frame
            isScrubbing = false
        }
    }

    // -------------------------------------------------------------------------
    // Controls
    // -------------------------------------------------------------------------

    private fun setupControls() {
        btnPrev.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            currentEntry ?: return@setOnClickListener
            val to = (currentSlideIndex - 1).coerceAtLeast(0)
            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
        }

        btnNext.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            val entry = currentEntry ?: return@setOnClickListener
            val to = (currentSlideIndex + 1).coerceAtMost(entry.slides.lastIndex)
            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
        }
    }

    private fun setCameraLockState(locked: Boolean) {
        isCameraLocked = locked
        if (locked) {
            if (savedManipulator == null) savedManipulator = sceneView.cameraManipulator
            sceneView.cameraManipulator  = null
            lockOverlay.visibility       = View.VISIBLE
            currentModelNode?.isEditable = false
        } else {
            if (sceneView.cameraManipulator == null && savedManipulator != null)
                sceneView.cameraManipulator = savedManipulator
            lockOverlay.visibility       = View.GONE
            currentModelNode?.isEditable = true
            val p = sceneView.cameraNode.position
            currentCameraEye = Vec3(p.x, p.y, p.z)
        }
    }

    // -------------------------------------------------------------------------
    // Slide navigation
    // -------------------------------------------------------------------------

    private fun goToSlide(index: Int, animated: Boolean) {
        val entry = currentEntry ?: return
        val slide = entry.slides[index]

        slideTitle.text   = slide.title
        slideDesc.text    = slide.description
        overlayTitle.text = slide.title

        checklistContainer.removeAllViews()
        slide.steps.forEach { step ->
            val tv = TextView(this).apply {
                text     = "• $step"
                textSize = 12f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 3, 0, 3)
            }
            checklistContainer.addView(tv)
        }

        if (animated) {
            animateCameraPose(
                startEye   = currentCameraEye,
                startLook  = currentOrbitTarget,
                endEye     = slide.eye,
                endLook    = slide.lookAt,
                durationMs = 650L
            )
            // Jump to this slide's start frame, then scrub to its pause frame
            scrubAnimationTo(
                slideStartTime = slide.animationStartTime,
                targetTime     = slide.animationTime,
                durationMs     = slide.animationDurationMs
            )
        } else {
            setCamera(slide.eye, slide.lookAt)
            // Non-animated (first load): snap directly to the pause frame
            applyAnimationTime(slide.animationTime)
        }
    }

    // -------------------------------------------------------------------------
    // Camera helpers
    // -------------------------------------------------------------------------

    private fun captureManipulatorOnce() {
        if (!manipulatorCaptured) {
            savedManipulator    = sceneView.cameraManipulator
            manipulatorCaptured = true
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
                val eye   = Vec3(
                    lerp(startEye.x, endEye.x, eased),
                    lerp(startEye.y, endEye.y, eased),
                    lerp(startEye.z, endEye.z, eased)
                )
                val look  = Vec3(
                    lerp(startLook.x, endLook.x, eased),
                    lerp(startLook.y, endLook.y, eased),
                    lerp(startLook.z, endLook.z, eased)
                )
                setCamera(eye, look)
                delay(stepDelay)
            }
            setCamera(endEye, endLook)
        }
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    private fun easeInOutCubic(t: Float) =
        if (t < 0.5f) 4f * t * t * t
        else 1f - ((-2f * t + 2f).let { it * it * it } / 2f)
}
