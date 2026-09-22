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
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.automekaniko.databinding.Activity3dMaintainanceBinding
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

    companion object {
        const val PREFS_CHECKLIST = "automekaniko_checklist"
        fun getStepKey(guideName: String, slideIndex: Int, stepIndex: Int) =
            "${guideName}_s${slideIndex}_i${stepIndex}"
    }

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
        val steps: List<ChecklistStep> = emptyList(),
        val animationStartTime: Float = 0f,
        val animationTime: Float = 0f,
        val animationDurationMs: Long = 650L,
        val infoTitle: String? = null,
        val infoItems: List<InfoItem> = emptyList(),
        val removeFirst: String? = null,
        val teardownPath: String? = null,
        val targetPartName: String? = null,
        val targetPartLocationNote: String? = null
    )

    private lateinit var sceneView: SceneView
    private lateinit var modelLoader: ModelLoader

    private var currentModelNode: ModelNode? = null
    private var currentGuide: MaintenanceGuide? = null
    private var currentSlides: List<CameraSlide> = emptyList()
    private var currentSlideIndex = 0
    private var isCameraLocked = true
    private val checkedStepsBySlide = mutableMapOf<Int, MutableSet<Int>>()

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

        // ── Header Branding ──────────────────────────────────────────────────
        AppNavigation.setupBrandedTitle(this, binding.appTitle)
        AppNavigation.wire(this)

        sceneView    = binding.sceneView

        binding.closeTab.setOnClickListener { toggleInfoPanel() }
        binding.progressSection.setOnClickListener { toggleBottomDrawer() }
        binding.btnInfoModern.setOnClickListener { toggleInfoPanel() }
        binding.btnCloseInfo.setOnClickListener { closeInfoPanel() }

        modelLoader = ModelLoader(sceneView.engine, this)

        sceneView.onFrame = { _ ->
            if (!isScrubbing) {
                applyAnimationTime(lockedAnimTime)
            }
        }

        captureManipulatorOnce()
        setupModelSelector()
        setupVehicleHeaderButton()
        setupControls() // ← wire up tab clicks
        setCameraLockState(true)
        startCameraInfoUpdates()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        goToSlide(currentSlideIndex, animated = false, applySlideCamera = false)
    }

    override fun onDestroy() {
        cameraAnimJob?.cancel()
        cameraInfoJob?.cancel()
        animScrubJob?.cancel()
        super.onDestroy()
    }

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
        binding.btnInfoModern.visibility = View.GONE
        binding.infoSlidePanel.animate().cancel()
        binding.infoSlidePanel.scaleX = 0.8f
        binding.infoSlidePanel.scaleY = 0.8f
        binding.infoSlidePanel.alpha = 0f
        binding.infoSlidePanel.visibility = View.VISIBLE
        binding.infoSlidePanel.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(260L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun closeInfoPanel() {
        binding.infoSlidePanel.animate().cancel()
        binding.infoSlidePanel.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .alpha(0f)
            .setDuration(220L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                binding.infoSlidePanel.visibility = View.GONE
                val slide = currentSlides.getOrNull(currentSlideIndex)
                if (slide != null && slide.infoItems.isNotEmpty()) {
                    binding.btnInfoModern.visibility = View.VISIBLE
                }
            }
            .start()
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
            binding.guideAutoComplete.setText("No maintenance guides found", false)
            return
        }

        binding.guideAutoComplete.setSimpleItems(guideList.map { it.name }.toTypedArray())

        val openDropdown = {
            binding.guideAutoComplete.showDropDown()
        }
        binding.guideAutoComplete.setOnClickListener { openDropdown() }
        binding.menuGuides.setOnClickListener { openDropdown() }

        binding.guideAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedName = binding.guideAutoComplete.adapter.getItem(position)?.toString()
            val guide = guideList.find { it.name == selectedName } ?: guideList.getOrNull(position) ?: guideList[0]
            loadGuide(guide)
        }

        // Initial Selection
        if (guideList.isNotEmpty()) {
            binding.guideAutoComplete.setText(guideList[0].name, false)
            loadGuide(guideList[0])
        }
    }

    private fun setupVehicleHeaderButton() {
        val btnHeaderVehicle = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnHeaderVehicle)
        fun updateButtonText() {
            val vehicle = VehicleManager.getActiveVehicle(this)
            btnHeaderVehicle?.text = "🚘 ${vehicle.name.replace("Toyota ", "")}"
        }
        updateButtonText()
        btnHeaderVehicle?.setOnClickListener {
            VehicleManager.showSelectorDialog(this) {
                updateButtonText()
                currentGuide?.let { guide -> loadGuide(guide) }
            }
        }
    }

    private fun loadGuide(guide: MaintenanceGuide) {
        currentGuide = guide
        currentSlides = guide.slides
        currentSlideIndex = 0
        checkedStepsBySlide.clear()
        
        // Use "Maintainance" as the fixed header title per user request
        binding.tvMaintainanceTitle.text = "Maintainance"

        // Update Meta Bar
        binding.chipDifficulty.text = guide.difficulty
        binding.tvEstTime.text = "⏱ ${guide.estimatedTime}"
        binding.btnToolsPrep.setOnClickListener {
            showToolsPrepDialog(guide.requiredTools, guide.estimatedTime, guide.difficulty, guide.prerequisites)
        }

        loadModel(guide.glbFile)
    }

    private fun showToolsPrepDialog(
        tools: List<String>,
        time: String,
        difficulty: String,
        prereqs: List<String>
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tools_prep, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.tvDialogDifficulty).text = "Difficulty: $difficulty"
        dialogView.findViewById<TextView>(R.id.tvDialogTime).text = "⏱ Est. Time: $time"

        val cgTools = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.cgTools)
        cgTools.removeAllViews()
        tools.forEach { tool ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = tool
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(R.color.theme_red_light)
                setTextColor(androidx.core.content.ContextCompat.getColor(this@MAINTAINANCEActivity, R.color.theme_red))
            }
            cgTools.addView(chip)
        }

        val containerPrereqs = dialogView.findViewById<LinearLayout>(R.id.containerPrereqs)
        containerPrereqs.removeAllViews()
        prereqs.forEach { prereq ->
            val cb = androidx.appcompat.widget.AppCompatCheckBox(this).apply {
                text = prereq
                setTextColor(androidx.core.content.ContextCompat.getColor(this@MAINTAINANCEActivity, R.color.text_primary))
                textSize = 13f
                setPadding(12, 12, 12, 12)
            }
            containerPrereqs.addView(cb)
        }

        dialogView.findViewById<View>(R.id.btnCloseDialog).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirmPrep).setOnClickListener { dialog.dismiss() }

        dialog.show()
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

            val instance = try {
                modelLoader.createModelInstance(assetFileLocation = fileName)
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@MAINTAINANCEActivity, "Error loading model: $fileName", android.widget.Toast.LENGTH_LONG).show()
                null
            } ?: return@launch

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
            goToSlide(currentSlideIndex, animated = false, applySlideCamera = true)
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

    private fun goToSlide(index: Int, animated: Boolean, applySlideCamera: Boolean = true) {
        val slide = currentSlides.getOrNull(index) ?: return
        binding.slideTitle.text = slide.title
        
        // Update Progress Bar
        val progress = ((index + 1).toFloat() / currentSlides.size * 100).toInt()
        binding.stepProgressBar.setProgress(progress, animated)

        // ── 3D HUD Part Badge Callout ─────────────────────────────────────────
        if (!slide.targetPartName.isNullOrEmpty()) {
            binding.hudPartBadge.visibility = View.VISIBLE
            binding.tvHudPartName.text = "📍 ${slide.targetPartName}"
            if (!slide.targetPartLocationNote.isNullOrEmpty()) {
                binding.tvHudPartNote.text = slide.targetPartLocationNote
                binding.tvHudPartNote.visibility = View.VISIBLE
            } else {
                binding.tvHudPartNote.visibility = View.GONE
            }
            binding.hudPartBadge.setOnClickListener {
                if (!slide.targetPartLocationNote.isNullOrEmpty()) {
                    val isVis = binding.tvHudPartNote.visibility == View.VISIBLE
                    binding.tvHudPartNote.visibility = if (isVis) View.GONE else View.VISIBLE
                }
            }
        } else {
            binding.hudPartBadge.visibility = View.GONE
        }

        binding.checklistContainer.removeAllViews()

        // ── Dependency & Teardown Path Callouts ──────────────────────────────
        if (!slide.removeFirst.isNullOrEmpty()) {
            val tagView = TextView(this).apply {
                text = "🔒 REMOVE FIRST: ${slide.removeFirst}"
                setTextColor(androidx.core.content.ContextCompat.getColor(this@MAINTAINANCEActivity, R.color.theme_red))
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 8.toPx())
            }
            binding.checklistContainer.addView(tagView)
        }

        if (!slide.teardownPath.isNullOrEmpty()) {
            val pathView = TextView(this).apply {
                text = "🛤 Sequence: ${slide.teardownPath}"
                setTextColor(androidx.core.content.ContextCompat.getColor(this@MAINTAINANCEActivity, R.color.text_secondary))
                textSize = 11f
                setPadding(0, 0, 0, 16.toPx())
            }
            binding.checklistContainer.addView(pathView)
        }

        val inflater = LayoutInflater.from(this)
        val checkedSteps = checkedStepsBySlide.getOrPut(index) { mutableSetOf() }

        fun updateCompletionText() {
            val desc = slide.description
            val prep = if (!slide.removeFirst.isNullOrEmpty()) " [🔒 ${slide.removeFirst}]" else ""
            binding.slideDesc.text = "$desc$prep (${checkedSteps.size}/${slide.steps.size} Done)"
        }

        val checklistPrefs = getSharedPreferences(PREFS_CHECKLIST, MODE_PRIVATE)

        slide.steps.forEachIndexed { stepIndex, step ->
            val row = inflater.inflate(R.layout.item_checklist_step, binding.checklistContainer, false)
            val label = row.findViewById<TextView>(R.id.stepLabel)
            val checkboxIcon = row.findViewById<ImageView>(R.id.stepCheckboxIcon)
            val infoText = row.findViewById<TextView>(R.id.stepInfoText)
            val warningText = row.findViewById<TextView>(R.id.stepWarningText)
            val infoBtn = row.findViewById<ImageView>(R.id.btnStepInfo)

            label.text = step.label
            if (!step.warning.isNullOrEmpty()) {
                warningText.visibility = View.VISIBLE
                warningText.text = "⚠️ ${step.warning}"
            }

            if (!step.info.isNullOrEmpty()) {
                infoBtn.visibility = View.VISIBLE
                infoText.text = step.info
                
                val autoExpand = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
                    .getBoolean(SettingsActivity.KEY_AUTO_EXPAND, false)
                if (autoExpand) {
                    infoText.visibility = View.VISIBLE
                    infoBtn.rotation = 180f
                }
            }

            val stepKey = getStepKey(currentGuide?.name ?: "", index, stepIndex)
            var isChecked = checklistPrefs.getBoolean(stepKey, false)
            if (isChecked) checkedSteps.add(stepIndex) else checkedSteps.remove(stepIndex)
            
            checkboxIcon.setImageResource(
                if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked
            )

            row.setOnClickListener {
                isChecked = !isChecked
                checklistPrefs.edit().putBoolean(stepKey, isChecked).apply()
                if (isChecked) checkedSteps.add(stepIndex) else checkedSteps.remove(stepIndex)
                checkboxIcon.setImageResource(
                    if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked
                )
                updateCompletionText()
            }

            infoBtn.setOnClickListener {
                val isVisible = infoText.visibility == View.VISIBLE
                infoText.visibility = if (isVisible) View.GONE else View.VISIBLE
                infoBtn.animate().rotation(if (isVisible) 0f else 180f).setDuration(200L).start()
            }

            binding.checklistContainer.addView(row)
        }

        updateCompletionText()

        updateInfoPanel(slide)

        val targetEye  = slide.eye
        val targetLook = slide.lookAt

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
            binding.infoSlidePanel.visibility = View.GONE
            binding.btnInfoModern.visibility = View.GONE
            binding.infoItemsContainer.removeAllViews()
            return
        } else {
            binding.btnInfoModern.visibility = View.VISIBLE
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
                setTextColor(0xFF222222.toInt())
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
                setTextColor(0xFF555555.toInt())
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
                if (!isCameraLocked) {
                    currentCameraEye = cam
                }

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
