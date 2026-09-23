package com.example.automekaniko

import android.animation.ValueAnimator
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.automekaniko.databinding.Activity3dDtcGuideBinding
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

    // -------------------------------------------------------------------------
    // Views (removed individual view declarations)
    // -------------------------------------------------------------------------

    private lateinit var sceneView:          SceneView
    private lateinit var modelLoader:        ModelLoader

    private var isInfoOpen = false
    private var isBottomDrawerOpen = false
    private var dtcConfirmedInSession = false

    private val previewGlbFile = "Vehicle Preventive Maintenance Checklist (VPMC).glb"

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private var currentModelNode:  ModelNode? = null
    private var currentEntry:      DtcGuide?  = null
    private var currentSlideIndex: Int        = 0
    private var isCameraLocked:    Boolean    = true
    private val checkedStepsBySlide = mutableMapOf<Int, MutableSet<Int>>()

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
        binding = Activity3dDtcGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── Header Branding ──────────────────────────────────────────────────
        AppNavigation.setupBrandedTitle(this, binding.appTitle)
        AppNavigation.wire(this)

        sceneView = binding.sceneView
        binding.closeTab.setOnClickListener { toggleInfoPanel() }
        binding.progressSection.setOnClickListener { toggleBottomDrawer() }
        binding.btnInfoModern.setOnClickListener { toggleInfoPanel() }
        binding.btnCloseInfo.setOnClickListener { closeInfoPanel() }

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
        setupDtcSelector()
        setupVehicleHeaderButton()
        setupControls()
        setCameraLockState(true)

        binding.backBtn.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        setupVehicleHeaderButton()
        refreshDtcListForActiveVehicle()
        goToSlide(currentSlideIndex, animated = false, applySlideCamera = false)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
    }

    private fun loadPreviewModel() {
        lifecycleScope.launch {
            delay(200L)
            loadGlbModel(previewGlbFile)
            currentEntry = null
            currentSlideIndex = 0
            dtcConfirmedInSession = false
        }
    }

    override fun onPause() {
        cameraAnimJob?.cancel()
        animScrubJob?.cancel()
        super.onPause()
    }

    override fun onDestroy() {
        cameraAnimJob?.cancel()
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
                binding.btnInfoModern.visibility = View.VISIBLE
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

    private fun updateInfoPanel(slide: DtcSlide) {
        val fallbackItems = mutableListOf<MAINTAINANCEActivity.InfoItem>()
        val guide = currentEntry
        
        // For DTC, we almost always want info visible (fallback to code info)
        binding.btnInfoModern.visibility = View.VISIBLE

        if (slide.infoItems.isEmpty()) {
            guide?.let {
                fallbackItems.add(MAINTAINANCEActivity.InfoItem("DTC", "${it.code} - ${it.name}"))
                fallbackItems.add(MAINTAINANCEActivity.InfoItem("Guide", it.description))
                if (it.parts.isNotEmpty()) {
                    fallbackItems.add(MAINTAINANCEActivity.InfoItem("Parts", it.parts.joinToString(", ")))
                }
            }
            if (slide.steps.isNotEmpty()) {
                val stepsText = slide.steps.joinToString("\n") { it.label }
                fallbackItems.add(MAINTAINANCEActivity.InfoItem("Current Step", stepsText))
            }
        }

        val items = if (slide.infoItems.isEmpty()) fallbackItems else slide.infoItems
        binding.tvInfoPanelTitle.text = slide.infoTitle ?: "Recommended Info"
        binding.infoItemsContainer.removeAllViews()

        val primaryTextColor = androidx.core.content.ContextCompat.getColor(this, R.color.text_primary)
        val secondaryTextColor = androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary)

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
                setTextColor(primaryTextColor)
                textSize = 13f
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
                setTextColor(secondaryTextColor)
                textSize = 12f
                setLineSpacing(0f, 1.15f)
            }
            itemLayout.addView(descTv)

            binding.infoItemsContainer.addView(itemLayout)
        }
    }

    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()

    // -------------------------------------------------------------------------
    // Load entry
    // -------------------------------------------------------------------------

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
                refreshDtcListForActiveVehicle()
            }
        }
    }

    private fun loadDtcEntry(entry: DtcGuide) {
        currentEntry      = entry
        currentSlideIndex = 0
        checkedStepsBySlide.clear()
        currentAnimTime   = 0f
        lockedAnimTime    = 0f

        // Update Meta Bar
        binding.chipDifficulty.text = entry.difficulty
        binding.tvEstTime.text = "⏱ ${entry.estimatedTime}"
        binding.btnToolsPrep.setOnClickListener {
            showToolsPrepDialog(entry.requiredTools, entry.estimatedTime, entry.difficulty, entry.prerequisites)
        }

        updateUiState()
        loadGlbModel(entry.glbFile) {
            currentSlideIndex = 0
            goToSlide(0, animated = false, applySlideCamera = true)
            updateUiState()
        }
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
                setTextColor(androidx.core.content.ContextCompat.getColor(this@DtcActivity, R.color.theme_red))
            }
            cgTools.addView(chip)
        }

        val containerPrereqs = dialogView.findViewById<LinearLayout>(R.id.containerPrereqs)
        containerPrereqs.removeAllViews()
        prereqs.forEach { prereq ->
            val cb = androidx.appcompat.widget.AppCompatCheckBox(this).apply {
                text = prereq
                setTextColor(androidx.core.content.ContextCompat.getColor(this@DtcActivity, R.color.text_primary))
                textSize = 13f
                setPadding(12, 12, 12, 12)
            }
            containerPrereqs.addView(cb)
        }

        dialogView.findViewById<View>(R.id.btnCloseDialog).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirmPrep).setOnClickListener { dialog.dismiss() }

        dialog.show()
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

    private fun setupDtcSelector() {
        val openDropdown = {
            binding.dtcAutoComplete.showDropDown()
        }
        binding.dtcAutoComplete.setOnClickListener { openDropdown() }
        binding.menuDtc.setOnClickListener { openDropdown() }

        binding.dtcAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedText = binding.dtcAutoComplete.adapter.getItem(position)?.toString()
            val entry = dtcList.find { "${it.code} — ${it.name}" == selectedText } ?: dtcList.getOrNull(position) ?: dtcList.firstOrNull()
            if (entry != null) {
                dtcConfirmedInSession = true
                loadDtcEntry(entry)
            }
        }

        refreshDtcListForActiveVehicle()
    }

    private fun refreshDtcListForActiveVehicle() {
        val activeVehicle = VehicleManager.getActiveVehicle(this)
        dtcList = getDtcGuidesForVehicle(activeVehicle.id)

        if (dtcList.isEmpty()) {
            binding.dtcAutoComplete.setSimpleItems(emptyArray())
            binding.dtcAutoComplete.setText("No DTC guides for ${activeVehicle.name.replace("Toyota ", "")}", false)
            loadPreviewModel()
            return
        }

        binding.dtcAutoComplete.setSimpleItems(dtcList.map { "${it.code} — ${it.name}" }.toTypedArray())

        val currentMatchesVehicle = currentEntry?.vehicleId?.equals(activeVehicle.id, ignoreCase = true) == true

        if (currentEntry == null || !currentMatchesVehicle || !dtcConfirmedInSession) {
            val firstGuide = dtcList[0]
            binding.dtcAutoComplete.setText("${firstGuide.code} — ${firstGuide.name}", false)
            dtcConfirmedInSession = true
            loadDtcEntry(firstGuide)
        }
    }

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

        binding.btnPrev.alpha = 1f
        binding.btnNext.alpha = 1f
    }

    // -------------------------------------------------------------------------
    // Slide navigation
    // -------------------------------------------------------------------------

    private fun goToSlide(index: Int, animated: Boolean, applySlideCamera: Boolean = true) {
        val entry = currentEntry ?: return
        val slide = entry.slides[index]

        binding.slideTitle.text   = slide.title
        
        // Update Progress Bar
        val progress = ((index + 1).toFloat() / entry.slides.size * 100).toInt()
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

        populateChecklist(slide, index)
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

    private fun populateChecklist(slide: DtcSlide, slideIndex: Int) {
        binding.checklistContainer.removeAllViews()

        // ── Dependency & Teardown Path Callouts ──────────────────────────────
        if (!slide.removeFirst.isNullOrEmpty()) {
            val tagView = TextView(this).apply {
                text = "🔒 REMOVE FIRST: ${slide.removeFirst}"
                setTextColor(androidx.core.content.ContextCompat.getColor(this@DtcActivity, R.color.theme_red))
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 8.toPx())
            }
            binding.checklistContainer.addView(tagView)
        }

        if (!slide.teardownPath.isNullOrEmpty()) {
            val pathView = TextView(this).apply {
                text = "🛤 Sequence: ${slide.teardownPath}"
                setTextColor(androidx.core.content.ContextCompat.getColor(this@DtcActivity, R.color.text_secondary))
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

        val checklistPrefs = getSharedPreferences(MAINTAINANCEActivity.PREFS_CHECKLIST, MODE_PRIVATE)
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

            val stepKey = MAINTAINANCEActivity.getStepKey(currentEntry?.code ?: "", slideIndex, stepIndex)
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
