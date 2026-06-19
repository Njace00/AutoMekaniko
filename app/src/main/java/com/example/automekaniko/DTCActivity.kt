package com.example.automekaniko

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.automekaniko.databinding.Activity3dDtcGuideBinding
import com.example.automekaniko.databinding.ItemChecklistStepBinding
import io.github.sceneview.SceneView
import io.github.sceneview.gesture.CameraGestureDetector
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DtcActivity : AppCompatActivity() {

    private lateinit var binding: Activity3dDtcGuideBinding
    private var dtcList: List<DtcGuide> = dtcGuides
    private val activeNavColor = 0xFFFF2020.toInt()
    private val inactiveNavColor = 0xFF666666.toInt()
    private val nextButtonColor = 0xFFE02020.toInt()
    private val prevButtonColor = 0xFF8A8A8A.toInt()

    // -------------------------------------------------------------------------
    // Views (removed individual view declarations)
    // -------------------------------------------------------------------------

    private lateinit var sceneView:          SceneView
    private lateinit var modelLoader:        ModelLoader

    private var isInfoOpen = false
    private var dtcConfirmedInSession = false

    private val previewGlbFile = "Vehicle Preventive Maintenance Checklist (VPMC).glb"

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

    private val startEye    = Vec3(0.15f, 0.95f, -2.75f)
    private val startLookAt = Vec3(0f, 0.30f, 0f)

    private var savedManipulator:    CameraGestureDetector.CameraManipulator? = null
    private var manipulatorCaptured: Boolean = false

    private var currentAnimTime: Float = 0f
    private var lockedAnimTime:  Float = 0f
    private var isScrubbing:     Boolean = false

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullscreenChrome()
        binding = Activity3dDtcGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFFFFFFFF.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.appTitle.text = spannable
        AppNavigation.wire(this)
        applyDtcBottomNavColors()

        sceneView = binding.sceneView
        binding.infoTab.setOnClickListener { toggleInfoPanel() }

        modelLoader = ModelLoader(sceneView.engine, this)

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
        setupControls()
        setCameraLockState(true)

        // Automatically load the first guide if list is not empty
        if (dtcList.isNotEmpty()) {
            dtcConfirmedInSession = true
            loadDtcEntry(dtcList[0], showChecklist = true)
        } else {
            loadPreviewModel()
        }

        binding.backBtn.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        enableFullscreenChrome()
        if (currentModelNode == null) {
            loadPreviewModel()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableFullscreenChrome()
        }
    }

    private fun loadPreviewModel() {
        lifecycleScope.launch {
            delay(200L)
            loadGlbModel(previewGlbFile)
            currentEntry = null
            currentSlideIndex = 0
            dtcConfirmedInSession = false
            applyCustomStartCamera()
        }
    }

    override fun onDestroy() {
        cameraAnimJob?.cancel()
        animScrubJob?.cancel()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun enableFullscreenChrome() {
        window.statusBarColor = 0xFF000000.toInt()
        window.navigationBarColor = 0xFF000000.toInt()
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun applyDtcBottomNavColors() {
        binding.hometxt.setColorFilter(inactiveNavColor)
        binding.livetxt.setColorFilter(inactiveNavColor)
        binding.connecttxt.setColorFilter(activeNavColor)
        binding.settingtxt.setColorFilter(inactiveNavColor)
    }

    // -------------------------------------------------------------------------
    // INFO panel
    // -------------------------------------------------------------------------

    private fun toggleInfoPanel() {
        isInfoOpen = !isInfoOpen
        if (isInfoOpen) {
            openInfoPanel()
        } else {
            closeInfoPanel()
        }
    }

    private fun openInfoPanel() {
        val slideWidth = binding.mainCard.width.takeIf { it > 0 } ?: binding.root.width
        binding.tvTabText.text = "CLOSE"
        binding.infoSlidePanel.animate().cancel()
        binding.infoTab.animate().cancel()
        binding.infoSlidePanel.translationX = slideWidth.toFloat()
        binding.infoSlidePanel.alpha = 0f
        binding.infoSlidePanel.visibility = View.VISIBLE
        binding.infoSlidePanel.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(260L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        binding.infoTab.animate()
            .translationX(-6f)
            .setDuration(260L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        binding.ivTabArrowTop.animate().rotation(180f).setDuration(220L).start()
        binding.ivTabArrowBottom.animate().rotation(180f).setDuration(220L).start()
    }

    private fun closeInfoPanel() {
        val slideWidth = binding.mainCard.width.takeIf { it > 0 } ?: binding.root.width
        binding.tvTabText.text = "INFO"
        binding.infoSlidePanel.animate().cancel()
        binding.infoTab.animate().cancel()
        binding.infoSlidePanel.animate()
            .translationX(slideWidth.toFloat())
            .alpha(0f)
            .setDuration(220L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                binding.infoSlidePanel.visibility = View.GONE
                binding.infoSlidePanel.translationX = 0f
                binding.infoSlidePanel.alpha = 1f
            }
            .start()
        binding.infoTab.animate()
            .translationX(0f)
            .setDuration(220L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        binding.ivTabArrowTop.animate().rotation(0f).setDuration(180L).start()
        binding.ivTabArrowBottom.animate().rotation(0f).setDuration(180L).start()
    }

    private fun updateInfoPanel(slide: DtcSlide) {
        val fallbackItems = mutableListOf<MAINTAINANCEActivity.InfoItem>()
        val guide = currentEntry
        if (slide.infoItems.isEmpty()) {
            guide?.let {
                fallbackItems.add(MAINTAINANCEActivity.InfoItem("DTC", "${it.code} - ${it.name}"))
                fallbackItems.add(MAINTAINANCEActivity.InfoItem("Guide", it.description))
                if (it.parts.isNotEmpty()) {
                    fallbackItems.add(MAINTAINANCEActivity.InfoItem("Parts", it.parts.joinToString(", ")))
                }
            }
            if (slide.steps.isNotEmpty()) {
                fallbackItems.add(MAINTAINANCEActivity.InfoItem("Current Step", slide.steps.joinToString("\n")))
            }
        }

        val items = if (slide.infoItems.isEmpty()) fallbackItems else slide.infoItems
        binding.tvInfoPanelTitle.text = slide.infoTitle ?: "Recommended Info"
        binding.infoItemsContainer.removeAllViews()

        items.forEach { item ->
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16.toPx())
                }
            }

            val leftContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    100.toPx(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val titleTv = TextView(this).apply {
                text = item.title
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            leftContainer.addView(titleTv)

            if (item.imageResId != null) {
                val iv = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        80.toPx(),
                        80.toPx()
                    ).apply {
                        setMargins(0, 4.toPx(), 0, 0)
                    }
                    setImageResource(item.imageResId)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                leftContainer.addView(iv)
            }

            itemLayout.addView(leftContainer)

            val descTv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                text = item.description
                setTextColor(0xFFCCCCCC.toInt())
                textSize = 11f
            }
            itemLayout.addView(descTv)

            binding.infoItemsContainer.addView(itemLayout)
        }
    }

    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()

    // -------------------------------------------------------------------------
    // Load entry
    // -------------------------------------------------------------------------

    private fun loadDtcEntry(entry: DtcGuide, showChecklist: Boolean = false) {
        currentEntry      = entry
        currentSlideIndex = 0
        currentAnimTime   = 0f
        lockedAnimTime    = 0f
        updateUiState()
        loadGlbModel(entry.glbFile) {
            currentSlideIndex = 0
            goToSlide(0, animated = false, applySlideCamera = false)
            applyCustomStartCamera()
            updateUiState()
            if (showChecklist) {
                binding.checklistOverlay.visibility = View.GONE
            }
        }
    }

    // -------------------------------------------------------------------------
    // Model loading
    // -------------------------------------------------------------------------

    private fun loadGlbModel(fileName: String, onLoaded: (() -> Unit)? = null) {
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

            lockedAnimTime = 0f
            applyAnimationTime(0f)
            onLoaded?.invoke()
        }
    }

    private fun applyCustomStartCamera() {
        setCamera(startEye, startLookAt)
        lifecycleScope.launch {
            delay(32L)
            setCamera(startEye, startLookAt)
            delay(120L)
            setCamera(startEye, startLookAt)
        }
    }

    // -------------------------------------------------------------------------
    // Animation
    // -------------------------------------------------------------------------

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

    private fun scrubAnimationTo(
        slideStartTime: Float,
        targetTime: Float,
        durationMs: Long = 650L
    ) {
        animScrubJob?.cancel()
        animScrubJob = lifecycleScope.launch {
            isScrubbing = true
            applyAnimationTime(slideStartTime)

            val steps     = 30
            val stepDelay = (durationMs / steps).coerceAtLeast(1L)
            repeat(steps) { i ->
                val t     = (i + 1) / steps.toFloat()
                val eased = easeInOutCubic(t)
                applyAnimationTime(lerp(slideStartTime, targetTime, eased))
                delay(stepDelay)
            }

            applyAnimationTime(targetTime)
            isScrubbing = false
        }
    }

    // -------------------------------------------------------------------------
    // Controls
    // -------------------------------------------------------------------------

    private fun setupControls() {
        binding.btnPrev.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            currentEntry ?: return@setOnClickListener
            val to = (currentSlideIndex - 1).coerceAtLeast(0)
            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
            updateUiState()
        }

        binding.btnNext.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            val entry = currentEntry ?: return@setOnClickListener
            val to = (currentSlideIndex + 1).coerceAtMost(entry.slides.lastIndex)
            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
            updateUiState()
        }

        updateUiState()
    }

    private fun setCameraLockState(locked: Boolean) {
        isCameraLocked = locked
        if (locked) {
            if (savedManipulator == null) savedManipulator = sceneView.cameraManipulator
            sceneView.cameraManipulator  = null
            binding.lockOverlay.visibility       = View.VISIBLE
            currentModelNode?.isEditable = false
        } else {
            if (sceneView.cameraManipulator == null && savedManipulator != null)
                sceneView.cameraManipulator = savedManipulator
            binding.lockOverlay.visibility       = View.GONE
            currentModelNode?.isEditable = true
            val p = sceneView.cameraNode.position
            currentCameraEye = Vec3(p.x, p.y, p.z)
        }
        updateUiState()
    }

    private fun updateUiState() {
        val lastSlideIndex = currentEntry?.slides?.lastIndex ?: -1
        val hasEntry = currentEntry != null

        binding.btnPrev.isEnabled = isCameraLocked && hasEntry && dtcConfirmedInSession && currentSlideIndex > 0
        binding.btnNext.isEnabled = isCameraLocked && hasEntry && dtcConfirmedInSession && currentSlideIndex < lastSlideIndex

        binding.btnPrev.backgroundTintList = ColorStateList.valueOf(prevButtonColor)
        binding.btnNext.backgroundTintList = ColorStateList.valueOf(nextButtonColor)
        binding.btnPrev.setTextColor(0xFFFFFFFF.toInt())
        binding.btnNext.setTextColor(0xFFFFFFFF.toInt())
        binding.btnPrev.alpha = 1f
        binding.btnNext.alpha = 1f
    }

    // -------------------------------------------------------------------------
    // Slide navigation
    // -------------------------------------------------------------------------

    private fun goToSlide(index: Int, animated: Boolean, applySlideCamera: Boolean = true) {
        val entry = currentEntry ?: return
        val slide = entry.slides[index]
        val total = entry.slides.size

        binding.slideTitle.text   = slide.title
        binding.slideDesc.text    = "${slide.description} (${index + 1}/$total Done)"
        binding.overlayTitle.text = slide.title

        populateChecklist(slide.steps)
        updateInfoPanel(slide)

        if (animated) {
            animateCameraPose(
                startEye   = currentCameraEye,
                startLook  = currentOrbitTarget,
                endEye     = slide.eye,
                endLook    = slide.lookAt,
                durationMs = 650L
            )
            scrubAnimationTo(
                slideStartTime = slide.animationStartTime,
                targetTime     = slide.animationTime,
                durationMs     = slide.animationDurationMs
            )
        } else {
            if (applySlideCamera) {
                setCamera(slide.eye, slide.lookAt)
            }
            applyAnimationTime(slide.animationTime)
        }
    }

    private fun populateChecklist(steps: List<String>) {
        binding.checklistContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        steps.forEach { step ->
            val rowBinding = ItemChecklistStepBinding.inflate(inflater, binding.checklistContainer, true)
            rowBinding.stepLabel.text = step
            rowBinding.stepCheckboxIcon.setImageResource(R.drawable.checkbox_red_checked)
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
