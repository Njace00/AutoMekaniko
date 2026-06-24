package com.example.automekaniko

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.automekaniko.databinding.Activity3dMaintainanceBinding
import com.example.automekaniko.databinding.ItemChecklistStepBinding
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

    private lateinit var binding: Activity3dMaintainanceBinding
    private val guideList = maintenanceGuides

    data class Vec3(val x: Float, val y: Float, val z: Float)

    data class InfoItem(
        val title: String,
        val description: String,
        val imageResId: Int? = null
    )

    data class CameraSlide(
        val title: String,
        val description: String,
        val eye: Vec3,
        val lookAt: Vec3,
        val steps: List<String> = emptyList(),
        val animationStartTime: Float = 0f,
        val animationTime: Float = 0f,
        val animationDurationMs: Long = 650L,
        val infoTitle: String? = null,
        val infoItems: List<InfoItem> = emptyList()
    )

    private lateinit var sceneView: SceneView
    private lateinit var modelLoader: ModelLoader

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

    private val startEye    = Vec3(0.15f, 0.95f, -2.75f)
    private val startLookAt = Vec3(0f, 0.30f, 0f)

    private var savedManipulator: CameraGestureDetector.CameraManipulator? = null
    private var manipulatorCaptured = false

    private var lockedAnimTime: Float = 0f
    private var currentAnimTime: Float = 0f
    private var isScrubbing: Boolean = false
    private var isInfoOpen: Boolean = false
    private var isBottomDrawerOpen: Boolean = false

    // ─────────────────────────────────────────────────────────────────────────
    //  Lifecycle
    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Activity3dMaintainanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── "Auto" white, "Mekaniko" red ──────────────────────────────────────
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFFFFFFFF.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.appTitle.text = spannable
        AppNavigation.wire(this)

        sceneView    = binding.sceneView

        binding.closeTab.setOnClickListener { toggleInfoPanel() }
        binding.progressSection.setOnClickListener { toggleBottomDrawer() }

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

        binding.backBtn.setOnClickListener {
            finish()
        }
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
        binding.closeTab.animate().cancel()
        binding.infoSlidePanel.translationX = slideWidth.toFloat()
        binding.infoSlidePanel.alpha = 0f
        binding.infoSlidePanel.visibility = View.VISIBLE
        binding.infoSlidePanel.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(260L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        binding.closeTab.animate()
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
        binding.closeTab.animate().cancel()
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
        binding.closeTab.animate()
            .translationX(0f)
            .setDuration(220L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        binding.ivTabArrowTop.animate().rotation(0f).setDuration(180L).start()
        binding.ivTabArrowBottom.animate().rotation(0f).setDuration(180L).start()
    }

    private fun toggleBottomDrawer() {
        setBottomDrawerOpen(!isBottomDrawerOpen, animated = true)
    }

    private fun setBottomDrawerOpen(open: Boolean, animated: Boolean) {
        isBottomDrawerOpen = open
        binding.bottomChecklistScroll.visibility = if (open) View.VISIBLE else View.GONE
        binding.bottomDrawerArrow.animate()
            .rotation(if (open) 180f else 0f)
            .setDuration(if (animated) 180L else 0L)
            .start()

        val targetHeight = (if (open) 220 else 96).toPx()
        val params = binding.progressSection.layoutParams as ConstraintLayout.LayoutParams
        if (!animated) {
            params.height = targetHeight
            binding.progressSection.layoutParams = params
            return
        }

        ValueAnimator.ofInt(binding.progressSection.height.takeIf { it > 0 } ?: params.height, targetHeight).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                params.height = animator.animatedValue as Int
                binding.progressSection.layoutParams = params
            }
            start()
        }
    }

    private fun captureManipulatorOnce() {
        if (!manipulatorCaptured) {
            savedManipulator = sceneView.cameraManipulator
            manipulatorCaptured = true
        }
    }

    private fun setupModelSelector() {
        if (guideList.isEmpty()) {
            binding.modelSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("No maintenance guides found")
            )
            return
        }

        binding.modelSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            guideList.map { it.name }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        
        // Use "Maintainance" as the fixed header title per user request
        binding.tvMaintainanceTitle.text = "Maintainance"

        loadModel(guide.glbFile)
    }

    private fun setupControls() {
        binding.btnPrev.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            val to = (currentSlideIndex - 1).coerceAtLeast(0)
            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
            updateUiState()
        }

        binding.btnNext.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            val to = (currentSlideIndex + 1).coerceAtMost(currentSlides.lastIndex)
            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
            updateUiState()
        }

        binding.btnCameraLock.setOnClickListener {
            setCameraLockState(!isCameraLocked)
        }

        updateUiState()
    }

    private fun setCameraLockState(locked: Boolean) {
        isCameraLocked = locked

        if (locked) {
            if (savedManipulator == null) savedManipulator = sceneView.cameraManipulator
            sceneView.cameraManipulator = null
            binding.lockOverlay.visibility = View.VISIBLE
            currentModelNode?.isEditable = false
        } else {
            if (sceneView.cameraManipulator == null && savedManipulator != null)
                sceneView.cameraManipulator = savedManipulator
            binding.lockOverlay.visibility = View.GONE
            currentModelNode?.isEditable = true
            val p = sceneView.cameraNode.position
            currentCameraEye = Vec3(p.x, p.y, p.z)
        }

        updateUiState()
    }

    private fun updateUiState() {
        binding.btnCameraLock.text = if (isCameraLocked) "Camera: LOCK" else "Camera: FREE"
        binding.btnPrev.isEnabled  = isCameraLocked && currentSlideIndex > 0
        binding.btnNext.isEnabled  = isCameraLocked && currentSlideIndex < currentSlides.lastIndex
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

            currentSlideIndex = 0
            goToSlide(currentSlideIndex, animated = false, applySlideCamera = false)
            applyCustomStartCamera()
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
            delay(120L)
            setCamera(startEye, startLookAt)
        }
    }

    private fun goToSlide(index: Int, animated: Boolean, applySlideCamera: Boolean = true) {
        val slide = currentSlides.getOrNull(index) ?: return
        binding.slideTitle.text = slide.title
        binding.slideDesc.text  = slide.description

        // ── Update checklist overlay ──────────────────────────────────────────
        binding.overlayTitle.text = slide.title
        binding.checklistContainer.removeAllViews()
        slide.steps.forEach { step ->
            val tv = android.widget.TextView(this).apply {
                text = "• $step"
                textSize = 12f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 3, 0, 3)
            }
            binding.checklistContainer.addView(tv)
        }

        updateInfoPanel(slide)

        val useOverviewCamera = index <= 1
        val targetEye  = if (useOverviewCamera) startEye else slide.eye
        val targetLook = if (useOverviewCamera) startLookAt else slide.lookAt

        if (animated) {
            animateCameraPose(
                startEye  = currentCameraEye,
                startLook = currentOrbitTarget,
                endEye    = targetEye,
                endLook   = targetLook,
                durationMs = 650L
            )
            scrubAnimationTo(
                slideStartTime = slide.animationStartTime,
                targetTime = slide.animationTime,
                durationMs = slide.animationDurationMs
            )
        } else {
            if (applySlideCamera) {
                setCamera(targetEye, targetLook)
            }
            applyAnimationTime(slide.animationTime)
        }
    }

    private fun updateInfoPanel(slide: CameraSlide) {
        // If there's no info content, we can hide the entire tab or just clear it.
        // For now, let's just clear it.
        if (slide.infoItems.isEmpty()) {
            isInfoOpen = false
            binding.infoSlidePanel.animate().cancel()
            binding.closeTab.animate().cancel()
            binding.closeTab.visibility = View.GONE
            binding.infoSlidePanel.visibility = View.GONE
            binding.infoSlidePanel.translationX = 0f
            binding.infoSlidePanel.alpha = 1f
            binding.tvTabText.text = "INFO"
            binding.ivTabArrowTop.rotation = 0f
            binding.ivTabArrowBottom.rotation = 0f
            binding.infoItemsContainer.removeAllViews()
            return
        } else {
            binding.closeTab.visibility = View.VISIBLE
            binding.tvTabText.text = if (isInfoOpen) "CLOSE" else "INFO"
        }

        binding.tvInfoPanelTitle.text = slide.infoTitle ?: "Recommended Info"
        binding.infoItemsContainer.removeAllViews()

        slide.infoItems.forEach { item ->
            val itemLayout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16.toPx())
                }
            }

            val leftContainer = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    100.toPx(),
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val titleTv = android.widget.TextView(this).apply {
                text = item.title
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            leftContainer.addView(titleTv)

            if (item.imageResId != null) {
                val iv = android.widget.ImageView(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        80.toPx(),
                        80.toPx()
                    ).apply {
                        setMargins(0, 4.toPx(), 0, 0)
                    }
                    setImageResource(item.imageResId)
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                }
                leftContainer.addView(iv)
            }

            itemLayout.addView(leftContainer)

            val descTv = android.widget.TextView(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
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

                binding.tvCameraInfo.text =
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
