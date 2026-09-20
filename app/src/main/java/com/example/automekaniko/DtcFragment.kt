package com.example.automekaniko

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.automekaniko.databinding.Activity3dDtcGuideBinding
import io.github.sceneview.SceneView
import io.github.sceneview.gesture.CameraGestureDetector
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.ViewNode2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DtcFragment : Fragment() {

    private var _binding: Activity3dDtcGuideBinding? = null
    private val binding get() = _binding!!
    private var dtcList: List<DtcGuide> = dtcGuides

    private lateinit var sceneView:          SceneView
    private lateinit var modelLoader:        ModelLoader

    private var isInfoOpen = false
    private var isBottomDrawerOpen = false
    private var dtcConfirmedInSession = false

    private val previewGlbFile = "Vehicle Preventive Maintenance Checklist (VPMC).glb"

    private var currentModelNode:  ModelNode? = null
    private var currentEntry:      DtcGuide?  = null
    private var currentSlideIndex: Int        = 0
    private var isCameraLocked:    Boolean    = true
    private val checkedStepsBySlide = mutableMapOf<Int, MutableSet<Int>>()

    private var cameraAnimJob: Job? = null
    private var animScrubJob:  Job? = null

    private var currentCameraEye   = Vec3(0f, 0f, 0f)
    private var currentOrbitTarget = Vec3(0f, 0.5f, 0f)

    private var savedManipulator:    CameraGestureDetector.CameraManipulator? = null
    private var manipulatorCaptured: Boolean = false

    private var currentAnimTime: Float = 0f
    private var lockedAnimTime:  Float = 0f
    private var isScrubbing:     Boolean = false
    private var guideLogged = false
    private var currentMarkerNode: ViewNode2? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Activity3dDtcGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.app_text_primary)
        val redColor = ContextCompat.getColor(requireContext(), R.color.theme_red)
        
        spannable.setSpan(ForegroundColorSpan(primaryColor), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(redColor), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.appTitle.text = spannable

        sceneView = binding.sceneView
        binding.closeTab.setOnClickListener { toggleInfoPanel() }
        binding.progressSection.setOnClickListener { toggleBottomDrawer() }

        modelLoader = ModelLoader(sceneView.engine, requireContext())

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
        setupControls()
        setCameraLockState(true)

        if (dtcList.isNotEmpty()) {
            dtcConfirmedInSession = true
            loadDtcEntry(dtcList[0])
        } else {
            loadPreviewModel()
        }

        binding.backBtn.setOnClickListener { 
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.topBar.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraAnimJob?.cancel()
        animScrubJob?.cancel()
        currentMarkerNode?.destroy()
        _binding = null
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

    private fun toggleInfoPanel() {
        isInfoOpen = !isInfoOpen
        if (isInfoOpen) openInfoPanel() else closeInfoPanel()
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

    private fun updateInfoPanel(slide: DtcSlide) {
        val fallbackItems = mutableListOf<InfoItem>()
        val guide = currentEntry
        if (slide.infoItems.isEmpty()) {
            guide?.let {
                fallbackItems.add(InfoItem("DTC", "${it.code} - ${it.name}"))
                fallbackItems.add(InfoItem("Guide", it.description))
                if (it.parts.isNotEmpty()) {
                    fallbackItems.add(InfoItem("Parts", it.parts.joinToString(", ")))
                }
            }
            if (slide.steps.isNotEmpty()) {
                fallbackItems.add(InfoItem("Current Step", slide.steps.joinToString("\n") { it.title }))
            }
        }

        val items = if (slide.infoItems.isEmpty()) fallbackItems else slide.infoItems
        binding.tvInfoPanelTitle.text = slide.infoTitle ?: "Recommended Info"
        binding.infoItemsContainer.removeAllViews()

        items.forEach { item ->
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16.toPx())
                }
            }

            val leftContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    100.toPx(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val titleTv = TextView(requireContext()).apply {
                text = item.title
                setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_primary))
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            leftContainer.addView(titleTv)

            if (item.imageResId != null) {
                val iv = ImageView(requireContext()).apply {
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

            val descTv = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                text = item.description
                setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_secondary))
                textSize = 11f
            }
            itemLayout.addView(descTv)

            binding.infoItemsContainer.addView(itemLayout)
        }
    }

    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun showMarker(pos: Vec3) {
        currentMarkerNode?.let {
            sceneView.removeChildNode(it)
            it.destroy()
        }

        val markerView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_3d_marker, null)
        val markerNode = ViewNode2(
            engine = sceneView.engine,
            windowManager = sceneView.viewNodeWindowManager ?: ViewNode2.WindowManager(requireContext()),
            materialLoader = sceneView.materialLoader,
            view = markerView,
            unlit = true
        ).apply {
            position = Position(pos.x, pos.y, pos.z)
        }

        sceneView.addChildNode(markerNode)
        currentMarkerNode = markerNode

        // Setup pulsing animation
        val pulseView = markerView.findViewById<View>(R.id.markerPulse)
        pulseView.animate()
            .scaleX(1.4f)
            .scaleY(1.4f)
            .alpha(0f)
            .setDuration(1000L)
            .setInterpolator(android.view.animation.LinearInterpolator())
            .withEndAction { 
                pulseView.scaleX = 1f
                pulseView.scaleY = 1f
                pulseView.alpha = 1f
                restartPulse(pulseView)
            }
            .start()
    }

    private fun restartPulse(view: View) {
        if (_binding == null) return
        view.animate()
            .scaleX(1.4f)
            .scaleY(1.4f)
            .alpha(0f)
            .setDuration(1000L)
            .setInterpolator(android.view.animation.LinearInterpolator())
            .withEndAction { 
                view.scaleX = 1f
                view.scaleY = 1f
                view.alpha = 1f
                restartPulse(view)
            }
            .start()
    }

    private fun hideMarker() {
        currentMarkerNode?.let {
            sceneView.removeChildNode(it)
            it.destroy()
            currentMarkerNode = null
        }
    }

    private fun loadDtcEntry(entry: DtcGuide) {
        currentEntry      = entry
        currentSlideIndex = 0
        checkedStepsBySlide.clear()
        currentAnimTime   = 0f
        lockedAnimTime    = 0f
        guideLogged       = false
        updateUiState()
        loadGlbModel(entry.glbFile) {
            currentSlideIndex = 0
            goToSlide(0, animated = false, applySlideCamera = true)
            updateUiState()
        }
    }

    private fun loadGlbModel(fileName: String, onLoaded: (() -> Unit)? = null) {
        lifecycleScope.launch {
            currentModelNode?.let {
                sceneView.removeChildNode(it)
                it.destroy()
                currentModelNode = null
            }

            Log.d("DtcFragment", "Loading GLB: $fileName")
            val instance = try {
                modelLoader.createModelInstance(assetFileLocation = fileName)
            } catch (e: Exception) {
                Log.e("DtcFragment", "Exception loading GLB: $fileName", e)
                null
            }

            if (instance == null) {
                Log.e("DtcFragment", "GLB not found or failed to load: $fileName")
                Toast.makeText(requireContext(), "Could not load model: $fileName", Toast.LENGTH_LONG).show()
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

    private fun setupDtcSelector() {
        val names = dtcList.map { "${it.code} - ${it.name}" }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_menu, names)
        binding.dtcAutoComplete.setAdapter(adapter)

        // Set initial text and load first guide
        if (dtcList.isNotEmpty()) {
            binding.dtcAutoComplete.setText("${dtcList[0].code} - ${dtcList[0].name}", false)
            loadDtcEntry(dtcList[0])
        }

        binding.dtcAutoComplete.setOnItemClickListener { _, _, position, _ ->
            loadDtcEntry(dtcList[position])
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

    private fun goToSlide(index: Int, animated: Boolean, applySlideCamera: Boolean = true) {
        val entry = currentEntry ?: return
        val slide = entry.slides[index]

        binding.slideTitle.text   = slide.title
        
        // Marker logic
        if (slide.markerPos != null) {
            showMarker(slide.markerPos)
        } else {
            hideMarker()
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
        val inflater = LayoutInflater.from(requireContext())
        val checkedSteps = checkedStepsBySlide.getOrPut(slideIndex) { mutableSetOf() }

        fun updateCompletionText() {
            binding.slideDesc.text = "${slide.description} (${checkedSteps.size}/${slide.steps.size} Done)"

            // Marker visibility logic: hide if all items are checked
            if (checkedSteps.size == slide.steps.size && slide.steps.isNotEmpty()) {
                hideMarker()
            } else if (slide.markerPos != null && currentMarkerNode == null) {
                showMarker(slide.markerPos)
            }
        }

        updateCompletionText()

        slide.steps.forEachIndexed { stepIndex, step ->
            val row = inflater.inflate(R.layout.item_checklist_step, binding.checklistContainer, false)
            val header = row.findViewById<LinearLayout>(R.id.stepHeader)
            val label = row.findViewById<TextView>(R.id.stepLabel)
            val desc = row.findViewById<TextView>(R.id.stepDescription)
            val expandIcon = row.findViewById<ImageView>(R.id.expandIcon)
            val checkboxIcon = row.findViewById<ImageView>(R.id.stepCheckboxIcon)

            label.text = step.title
            if (step.description.isNotEmpty()) {
                desc.text = step.description
                expandIcon.visibility = View.VISIBLE
            }

            var isChecked = stepIndex in checkedSteps
            checkboxIcon.setImageResource(
                if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked
            )

            checkboxIcon.setOnClickListener {
                isChecked = !isChecked
                if (isChecked) checkedSteps.add(stepIndex) else checkedSteps.remove(stepIndex)
                checkboxIcon.setImageResource(
                    if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked
                )
                updateCompletionText()
            }

            header.setOnClickListener {
                if (step.description.isNotEmpty()) {
                    val isVisible = desc.visibility == View.VISIBLE
                    desc.visibility = if (isVisible) View.GONE else View.VISIBLE
                    expandIcon.animate().rotation(if (isVisible) 0f else 180f).start()
                }
            }
            binding.checklistContainer.addView(row)
        }
    }

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
