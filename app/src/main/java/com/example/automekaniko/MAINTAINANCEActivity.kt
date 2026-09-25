package com.example.automekaniko

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
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
import kotlin.time.Duration.Companion.milliseconds

class MAINTAINANCEActivity : AppCompatActivity() {

    companion object {
        const val PREFS_CHECKLIST = "automekaniko_checklist"
        fun getStepKey(guideName: String, slideIndex: Int, stepIndex: Int) =
            "${guideName}_s${slideIndex}_i$stepIndex"
    }

    private lateinit var binding: Activity3dMaintainanceBinding
    private var guideList: List<MaintenanceGuide> = emptyList()

    data class Vec3(val x: Float, val y: Float, val z: Float)

    data class InfoItem(
        val title: String,
        val description: String,
        val imageResId: Int? = null,
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

    private var savedManipulator: CameraGestureDetector.CameraManipulator? = null
    private var manipulatorCaptured = false

    private var lockedAnimTime: Float = 0f
    private var currentAnimTime: Float = 0f
    private var isScrubbing: Boolean = false
    private var isInfoOpen: Boolean = false

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

        binding.btnInfoModern.setOnClickListener { toggleInfoPanel() }
        binding.btnCloseInfo.setOnClickListener { closeInfoPanel() }
        binding.btnOverviewModern.setOnClickListener { toggleChecklistPanel() }
        binding.btnCloseChecklist.setOnClickListener { closeChecklistPanel() }

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

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        setupVehicleHeaderButton()
        refreshGuideListForActiveVehicle()
        goToSlide(currentSlideIndex, animated = false, applySlideCamera = false)
    }

    override fun onPause() {
        cameraAnimJob?.cancel()
        cameraInfoJob?.cancel()
        animScrubJob?.cancel()
        super.onPause()
    }

    override fun onDestroy() {
        cameraAnimJob?.cancel()
        cameraInfoJob?.cancel()
        animScrubJob?.cancel()
        currentModelNode?.let {
            runCatching {
                sceneView.removeChildNode(it)
                it.destroy()
            }
            currentModelNode = null
        }
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

    private var isChecklistOpen = false

    private fun toggleChecklistPanel() {
        isChecklistOpen = !isChecklistOpen
        if (isChecklistOpen) {
            openChecklistPanel()
        } else {
            closeChecklistPanel()
        }
    }

    private fun openChecklistPanel() {
        binding.btnOverviewModern.visibility = View.GONE
        binding.checklistSlidePanel.animate().cancel()
        binding.checklistSlidePanel.scaleX = 0.8f
        binding.checklistSlidePanel.scaleY = 0.8f
        binding.checklistSlidePanel.alpha = 0f
        binding.checklistSlidePanel.visibility = View.VISIBLE
        binding.checklistSlidePanel.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(260L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun closeChecklistPanel() {
        isChecklistOpen = false
        binding.checklistSlidePanel.animate().cancel()
        binding.checklistSlidePanel.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .alpha(0f)
            .setDuration(220L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                binding.checklistSlidePanel.visibility = View.GONE
                binding.btnOverviewModern.visibility = View.VISIBLE
            }
            .start()
    }

    private fun captureManipulatorOnce() {
        if (!manipulatorCaptured) {
            savedManipulator = sceneView.cameraManipulator
            manipulatorCaptured = true
        }
    }

    private fun setupModelSelector() {
        val openDropdown = {
            binding.guideAutoComplete.showDropDown()
        }
        binding.guideAutoComplete.setOnClickListener { openDropdown() }
        binding.menuGuides.setOnClickListener { openDropdown() }

        binding.guideAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedName = binding.guideAutoComplete.adapter.getItem(position)?.toString()
            val guide = guideList.find { it.name == selectedName } ?: guideList.getOrNull(position) ?: guideList.firstOrNull()
            if (guide != null) {
                loadGuide(guide)
            }
        }

        refreshGuideListForActiveVehicle()
    }

    private fun refreshGuideListForActiveVehicle() {
        val activeVehicle = VehicleManager.getActiveVehicle(this)
        guideList = getMaintenanceGuidesForVehicle(activeVehicle.id)

        if (guideList.isEmpty()) {
            binding.guideAutoComplete.setSimpleItems(emptyArray())
            binding.guideAutoComplete.setText("No maintenance guides for ${activeVehicle.name.replace("Toyota ", "")}", false)
            currentGuide = null
            currentSlides = emptyList()
            currentSlideIndex = 0
            return
        }

        binding.guideAutoComplete.setSimpleItems(guideList.map { it.name }.toTypedArray())

        val currentMatchesVehicle = currentGuide?.vehicleId?.equals(activeVehicle.id, ignoreCase = true) == true

        if (currentGuide == null || !currentMatchesVehicle) {
            val firstGuide = guideList[0]
            binding.guideAutoComplete.setText(firstGuide.name, false)
            loadGuide(firstGuide)
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
                refreshGuideListForActiveVehicle()
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

        // Automatically show Tools & Prep dialog on guide load
        showToolsPrepDialog(guide.requiredTools, guide.estimatedTime, guide.difficulty, guide.prerequisites)

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
                setTextColor(ContextCompat.getColor(this@MAINTAINANCEActivity, R.color.theme_red))
            }
            cgTools.addView(chip)
        }

        val containerPrereqs = dialogView.findViewById<LinearLayout>(R.id.containerPrereqs)
        containerPrereqs.removeAllViews()
        val prereqCheckBoxes = mutableListOf<androidx.appcompat.widget.AppCompatCheckBox>()
        prereqs.forEach { prereq ->
            val cb = androidx.appcompat.widget.AppCompatCheckBox(this).apply {
                text = prereq
                setTextColor(ContextCompat.getColor(this@MAINTAINANCEActivity, R.color.text_primary))
                textSize = 13f
                setPadding(12, 12, 12, 12)
            }
            containerPrereqs.addView(cb)
            prereqCheckBoxes.add(cb)
        }

        dialogView.findViewById<View>(R.id.btnCloseDialog).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirmPrep).setOnClickListener {
            val allChecked = prereqCheckBoxes.isEmpty() || prereqCheckBoxes.all { it.isChecked }
            if (allChecked) {
                dialog.dismiss()
            } else {
                android.widget.Toast.makeText(this, "Please complete the checklist first!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

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

            if (!isSlideChecklistComplete(currentSlideIndex)) {
                triggerIncompleteChecklistBlink()
                return@setOnClickListener
            }

            val to = (currentSlideIndex + 1).coerceAtMost(currentSlides.lastIndex)
            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
            updateUiState()
        }

        updateUiState()
    }

    private fun isSlideChecklistComplete(slideIndex: Int): Boolean {
        val slide = currentSlides.getOrNull(slideIndex) ?: return true
        if (slide.steps.isEmpty()) return true

        val checklistPrefs = getSharedPreferences(PREFS_CHECKLIST, MODE_PRIVATE)
        val guideName = currentGuide?.name ?: ""
        slide.steps.indices.forEach { stepIndex ->
            val stepKey = getStepKey(guideName, slideIndex, stepIndex)
            if (!checklistPrefs.getBoolean(stepKey, false)) {
                return false
            }
        }
        return true
    }

    private fun triggerIncompleteChecklistBlink() {
        android.widget.Toast.makeText(this, "Please complete all checklist steps before proceeding!", android.widget.Toast.LENGTH_SHORT).show()

        val btn = binding.btnOverviewModern
        btn.animate().cancel()
        val blinkAnimator = ValueAnimator.ofFloat(1f, 0.15f, 1f, 0.15f, 1f, 0.15f, 1f).apply {
            duration = 900L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                btn.alpha = animator.animatedValue as Float
            }
        }
        blinkAnimator.start()
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
        val lastSlideIndex = currentSlides.lastIndex
        val showPrev = isCameraLocked && currentSlideIndex > 0
        val targetPrevVis = if (showPrev) View.VISIBLE else View.GONE

        if (binding.btnPrev.visibility != targetPrevVis) {
            androidx.transition.TransitionManager.beginDelayedTransition(
                binding.navButtonsSection,
                androidx.transition.AutoTransition().apply { duration = 280L }
            )
            binding.btnPrev.visibility = targetPrevVis
        }

        binding.btnPrev.isEnabled = showPrev
        binding.btnNext.isEnabled = isCameraLocked && currentSlideIndex < lastSlideIndex
        binding.btnNext.text = if (lastSlideIndex in 0..currentSlideIndex) "Finish" else "Next Step"
    }

    private fun loadModel(fileName: String) {
        lifecycleScope.launch {
            currentModelNode?.let {
                sceneView.removeChildNode(it)
                it.destroy()
            }

            val instance = try {
                modelLoader.createModelInstance(assetFileLocation = fileName)
            } catch (_: Exception) {
                Toast.makeText(this@MAINTAINANCEActivity, "Error loading model: $fileName", Toast.LENGTH_LONG).show()
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

            modelNode.modelInstance.animator.let { animator ->
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
        val checkedSteps = checkedStepsBySlide.getOrPut(index) { mutableSetOf() }
        val desc = slide.description
        val prep = if (!slide.removeFirst.isNullOrEmpty()) " [🔒 ${slide.removeFirst}]" else ""
        binding.slideDesc.text = "$desc$prep (${checkedSteps.size}/${slide.steps.size} Done)"
        
        // Update Progress Bar
        val progress = ((index + 1).toFloat() / currentSlides.size * 100).toInt()
        binding.stepProgressBar.setProgress(progress, animated)

        // Wire Overview Button Click Listener
        binding.cardOverviewButton.setOnClickListener {
            showChecklistBottomSheet(slide, index)
        }

        // ── 3D HUD Part Badge Callout ─────────────────────────────────────────
        if (!slide.targetPartName.isNullOrEmpty()) {
            binding.hudPartBadge.visibility = View.VISIBLE
            binding.tvHudPartName.text = "📍 ${slide.targetPartName}"
            binding.hudPartBadge.setOnClickListener(null)
        } else {
            binding.hudPartBadge.visibility = View.GONE
        }

        populateChecklist(slide, index)
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

    private fun populateChecklist(slide: CameraSlide, slideIndex: Int) {
        binding.checklistContainer.removeAllViews()

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
        val checkedSteps = checkedStepsBySlide.getOrPut(slideIndex) { mutableSetOf() }

        fun updateCompletionText() {
            val desc = slide.description
            val prep = if (!slide.removeFirst.isNullOrEmpty()) " [🔒 ${slide.removeFirst}]" else ""
            binding.slideDesc.text = "$desc$prep (${checkedSteps.size}/${slide.steps.size} Done)"
        }

        val checklistPrefs = getSharedPreferences(PREFS_CHECKLIST, MODE_PRIVATE)
        val autoExpand = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
            .getBoolean(SettingsActivity.KEY_AUTO_EXPAND, false)

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
                if (autoExpand) {
                    infoText.visibility = View.VISIBLE
                    infoBtn.rotation = 180f
                }
            }

            val stepKey = getStepKey(currentGuide?.name ?: "", slideIndex, stepIndex)
            var isChecked = checklistPrefs.getBoolean(stepKey, false)
            if (isChecked) checkedSteps.add(stepIndex) else checkedSteps.remove(stepIndex)

            checkboxIcon.setImageResource(
                if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked
            )

            row.setOnClickListener {
                isChecked = !isChecked
                checklistPrefs.edit { putBoolean(stepKey, isChecked) }
                if (isChecked) checkedSteps.add(stepIndex) else checkedSteps.remove(stepIndex)
                checkboxIcon.setImageResource(
                    if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked
                )
                updateCompletionText()
            }

            infoBtn.setOnClickListener {
                val isVis = infoText.isVisible
                infoText.isVisible = !isVis
                infoBtn.animate().rotation(if (isVis) 0f else 180f).setDuration(200L).start()
            }

            binding.checklistContainer.addView(row)
        }

        updateCompletionText()
    }

    private fun showChecklistBottomSheet(slide: CameraSlide, slideIndex: Int) {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val bsView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_checklist, null)
        dialog.setContentView(bsView)

        val tvTitle = bsView.findViewById<TextView>(R.id.bsStepTitle)
        val tvDesc = bsView.findViewById<TextView>(R.id.bsStepDesc)
        val container = bsView.findViewById<LinearLayout>(R.id.bsChecklistContainer)
        val btnClose = bsView.findViewById<View>(R.id.btnCloseBs)
        val btnDone = bsView.findViewById<View>(R.id.btnDoneBs)

        tvTitle.text = slide.title

        val checkedSteps = checkedStepsBySlide.getOrPut(slideIndex) { mutableSetOf() }

        fun updateCompletionText() {
            val desc = slide.description
            val prep = if (!slide.removeFirst.isNullOrEmpty()) " [🔒 ${slide.removeFirst}]" else ""
            val formatted = "$desc$prep (${checkedSteps.size}/${slide.steps.size} Done)"
            tvDesc.text = formatted
            binding.slideDesc.text = formatted
        }

        populateChecklistContainer(container, slide, slideIndex) {
            updateCompletionText()
        }

        updateCompletionText()

        btnClose.setOnClickListener { dialog.dismiss() }
        btnDone.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun populateChecklistContainer(
        container: LinearLayout,
        slide: CameraSlide,
        slideIndex: Int,
        onStepChanged: () -> Unit
    ) {
        container.removeAllViews()

        // ── Dependency & Teardown Path Callouts ──────────────────────────────
        if (!slide.removeFirst.isNullOrEmpty()) {
            val tagView = TextView(this).apply {
                text = "🔒 REMOVE FIRST: ${slide.removeFirst}"
                setTextColor(androidx.core.content.ContextCompat.getColor(this@MAINTAINANCEActivity, R.color.theme_red))
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 8.toPx())
            }
            container.addView(tagView)
        }

        if (!slide.teardownPath.isNullOrEmpty()) {
            val pathView = TextView(this).apply {
                text = "🛤 Sequence: ${slide.teardownPath}"
                setTextColor(androidx.core.content.ContextCompat.getColor(this@MAINTAINANCEActivity, R.color.text_secondary))
                textSize = 11f
                setPadding(0, 0, 0, 16.toPx())
            }
            container.addView(pathView)
        }

        val inflater = LayoutInflater.from(this)
        val checkedSteps = checkedStepsBySlide.getOrPut(slideIndex) { mutableSetOf() }

        val checklistPrefs = getSharedPreferences(PREFS_CHECKLIST, MODE_PRIVATE)
        val autoExpand = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
            .getBoolean(SettingsActivity.KEY_AUTO_EXPAND, false)

        slide.steps.forEachIndexed { stepIndex, step ->
            val row = inflater.inflate(R.layout.item_checklist_step, container, false)
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
                if (autoExpand) {
                    infoText.visibility = View.VISIBLE
                    infoBtn.rotation = 180f
                }
            }

            val stepKey = getStepKey(currentGuide?.name ?: "", slideIndex, stepIndex)
            var isChecked = checklistPrefs.getBoolean(stepKey, false)
            if (isChecked) checkedSteps.add(stepIndex) else checkedSteps.remove(stepIndex)

            checkboxIcon.setImageResource(
                if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked
            )

            row.setOnClickListener {
                isChecked = !isChecked
                checklistPrefs.edit { putBoolean(stepKey, isChecked) }
                if (isChecked) checkedSteps.add(stepIndex) else checkedSteps.remove(stepIndex)
                checkboxIcon.setImageResource(
                    if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked
                )
                onStepChanged()
            }

            infoBtn.setOnClickListener {
                val isVis = infoText.isVisible
                infoText.isVisible = !isVis
                infoBtn.animate().rotation(if (isVis) 0f else 180f).setDuration(200L).start()
            }

            container.addView(row)
        }
    }

    private fun updateOverviewButtonPosition(hasInfoButton: Boolean) {
        val targetMarginTop = if (hasInfoButton) 68.toPx() else 12.toPx()
        val params = binding.btnOverviewModern.layoutParams as? android.view.ViewGroup.MarginLayoutParams ?: return
        if (params.topMargin != targetMarginTop) {
            androidx.transition.TransitionManager.beginDelayedTransition(
                binding.mainCard,
                androidx.transition.AutoTransition().apply { duration = 280L }
            )
            params.topMargin = targetMarginTop
            binding.btnOverviewModern.layoutParams = params
        }
    }

    private fun updateInfoPanel(slide: CameraSlide) {
        if (slide.infoItems.isEmpty()) {
            isInfoOpen = false
            binding.infoSlidePanel.animate().cancel()
            binding.infoSlidePanel.visibility = View.GONE
            binding.btnInfoModern.visibility = View.GONE
            binding.infoItemsContainer.removeAllViews()
            updateOverviewButtonPosition(hasInfoButton = false)
            return
        } else {
            binding.btnInfoModern.visibility = View.VISIBLE
            updateOverviewButtonPosition(hasInfoButton = true)
        }

        binding.tvInfoPanelTitle.text = slide.infoTitle ?: "Recommended Info"
        binding.infoItemsContainer.removeAllViews()

        val primaryTextColor = androidx.core.content.ContextCompat.getColor(this, R.color.text_primary)

        slide.infoItems.forEach { item ->
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16.toPx())
                }
            }

            val titleTv = TextView(this).apply {
                text = item.title
                setTextColor(primaryTextColor)
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 4.toPx())
            }
            itemLayout.addView(titleTv)

            if (item.imageResId != null) {
                val rowContainer = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                val iv = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        72.toPx(),
                        72.toPx()
                    ).apply {
                        setMargins(0, 0, 12.toPx(), 0)
                    }
                    setImageResource(item.imageResId)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                rowContainer.addView(iv)

                val descTv = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    text = item.description
                    setTextColor(primaryTextColor)
                    textSize = 13.5f
                    setLineSpacing(0f, 1.2f)
                }
                rowContainer.addView(descTv)
                itemLayout.addView(rowContainer)
            } else {
                val descTv = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = item.description
                    setTextColor(primaryTextColor)
                    textSize = 13.5f
                    setLineSpacing(0f, 1.2f)
                }
                itemLayout.addView(descTv)
            }

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

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    private fun easeInOutCubic(t: Float) =
        if (t < 0.5f) 4f * t * t * t
        else 1f - ((-2f * t + 2f).let { it * it * it } / 2f)
}
