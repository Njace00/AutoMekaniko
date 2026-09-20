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
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.automekaniko.databinding.Activity3dMaintainanceBinding
import io.github.sceneview.SceneView
import io.github.sceneview.gesture.CameraGestureDetector
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.ViewNode2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MaintenanceFragment : Fragment() {

    private var _binding: Activity3dMaintainanceBinding? = null
    private val binding get() = _binding!!
    private val guideList = maintenanceGuides

    data class CameraSlide(
        val title: String,
        val description: String,
        val eye: Vec3,
        val lookAt: Vec3,
        val steps: List<GuideStep> = emptyList(),
        val markerPos: Vec3? = null,
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
    private var isBottomDrawerOpen: Boolean = false
    private var guideLogged = false
    private var currentMarkerNode: ViewNode2? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Activity3dMaintainanceBinding.inflate(inflater, container, false)
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

        sceneView    = binding.sceneView

        binding.closeTab.setOnClickListener { toggleInfoPanel() }
        binding.progressSection.setOnClickListener { toggleBottomDrawer() }

        modelLoader = ModelLoader(sceneView.engine, requireContext())

        sceneView.onFrame = { _ ->
            if (!isScrubbing) {
                applyAnimationTime(lockedAnimTime)
            }
        }

        captureManipulatorOnce()
        setupModelSelector()
        setupControls()
        setupCameraDebug()
        setCameraLockState(true)
        startCameraInfoUpdates()

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
        cameraInfoJob?.cancel()
        animScrubJob?.cancel()
        currentMarkerNode?.destroy()
        _binding = null
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

    private fun captureManipulatorOnce() {
        if (!manipulatorCaptured) {
            savedManipulator = sceneView.cameraManipulator
            manipulatorCaptured = true
        }
    }

    private fun setupModelSelector() {
        val names = if (guideList.isEmpty()) listOf("No maintenance guides found") else guideList.map { it.name }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_menu, names)
        binding.modelAutoComplete.setAdapter(adapter)

        // Set initial text and load first guide
        if (guideList.isNotEmpty()) {
            binding.modelAutoComplete.setText(guideList[0].name, false)
            loadGuide(guideList[0])
        }

        binding.modelAutoComplete.setOnItemClickListener { _, _, position, _ ->
            if (guideList.isNotEmpty()) loadGuide(guideList[position])
        }
    }

    private fun loadGuide(guide: MaintenanceGuide) {
        currentGuide = guide
        currentSlides = guide.slides.map { s ->
            CameraSlide(
                title = s.title,
                description = s.description,
                eye = Vec3(s.eye.x, s.eye.y, s.eye.z),
                lookAt = Vec3(s.lookAt.x, s.lookAt.y, s.lookAt.z),
                steps = s.steps,
                animationStartTime = s.animationStartTime,
                animationTime = s.animationTime,
                animationDurationMs = s.animationDurationMs,
                infoTitle = s.infoTitle,
                infoItems = s.infoItems.map { i -> InfoItem(i.title, i.description, i.imageResId) }
            )
        }
        currentSlideIndex = 0
        checkedStepsBySlide.clear()
        guideLogged = false
        binding.tvMaintainanceTitle.text = "Maintainance"
        loadModel(guide.glbFile)
    }

    private fun setupControls() {
        binding.btnPrev.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            if (currentSlideIndex > 0) {
                currentSlideIndex--
                goToSlide(currentSlideIndex, animated = true)
                updateUiState()
            }
        }

        binding.btnNext.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            if (currentSlideIndex < currentSlides.size - 1) {
                currentSlideIndex++
                goToSlide(currentSlideIndex, animated = true)
                updateUiState()
            }
        }

        binding.btnDebugCamera.setOnClickListener {
            binding.debugPanel.visibility = if (binding.debugPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        binding.btnCloseDebug.setOnClickListener { binding.debugPanel.visibility = View.GONE }
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
        }
        updateUiState()
    }

    private fun updateUiState() {
        binding.btnPrev.isEnabled = isCameraLocked && currentSlideIndex > 0
        binding.btnNext.isEnabled = isCameraLocked && currentSlideIndex < currentSlides.size - 1
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
                Toast.makeText(requireContext(), "Error loading model: $fileName", Toast.LENGTH_LONG).show()
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

    private fun scrubAnimationTo(slideStartTime: Float, targetTime: Float, durationMs: Long) {
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
        
        // Marker logic
        if (slide.markerPos != null) {
            showMarker(slide.markerPos)
        } else {
            hideMarker()
        }

        binding.checklistContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val checkedSteps = checkedStepsBySlide.getOrPut(index) { mutableSetOf() }

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
            checkboxIcon.setImageResource(if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked)

            checkboxIcon.setOnClickListener {
                isChecked = !isChecked
                if (isChecked) checkedSteps.add(stepIndex) else checkedSteps.remove(stepIndex)
                checkboxIcon.setImageResource(if (isChecked) R.drawable.checkbox_red_checked else R.drawable.checkbox_red_unchecked)
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

        updateInfoPanel(slide)
        val targetEye = slide.eye
        val targetLook = slide.lookAt

        if (animated) {
            animateCameraPose(currentCameraEye, currentOrbitTarget, targetEye, targetLook, 650L)
            scrubAnimationTo(slide.animationStartTime, slide.animationTime, slide.animationDurationMs)
        } else {
            if (applySlideCamera) setCamera(targetEye, targetLook)
            applyAnimationTime(slide.animationTime)
        }

        if (binding.debugPanel.visibility == View.VISIBLE) updateSlidersFromCamera(targetEye, targetLook)
    }

    private fun updateInfoPanel(slide: CameraSlide) {
        val fallbackItems = mutableListOf<InfoItem>()
        if (slide.infoItems.isEmpty()) {
            currentGuide?.let {
                fallbackItems.add(InfoItem("Maintenance", it.name))
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

            leftContainer.addView(TextView(requireContext()).apply { 
                text = item.title
                setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_primary))
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD) 
            })

            if (item.imageResId != null) {
                leftContainer.addView(ImageView(requireContext()).apply { 
                    layoutParams = LinearLayout.LayoutParams(80.toPx(), 80.toPx()).apply { 
                        setMargins(0, 4.toPx(), 0, 0) 
                    }
                    setImageResource(item.imageResId)
                    scaleType = ImageView.ScaleType.FIT_CENTER 
                })
            }

            itemLayout.addView(leftContainer)

            itemLayout.addView(TextView(requireContext()).apply { 
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = item.description
                setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_secondary))
                textSize = 11f 
            })
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

    private fun setCamera(cameraPos: Vec3, lookTarget: Vec3) {
        currentCameraEye = cameraPos
        currentOrbitTarget = lookTarget
        sceneView.cameraNode.position = Position(cameraPos.x, cameraPos.y, cameraPos.z)
        sceneView.cameraNode.lookAt(Position(lookTarget.x, lookTarget.y, lookTarget.z))
    }

    private fun animateCameraPose(startEye: Vec3, startLook: Vec3, endEye: Vec3, endLook: Vec3, durationMs: Long) {
        cameraAnimJob?.cancel()
        cameraAnimJob = lifecycleScope.launch {
            val steps = 30
            val stepDelay = (durationMs / steps).coerceAtLeast(1L)
            repeat(steps) { i ->
                val t = (i + 1) / steps.toFloat()
                val eased = easeInOutCubic(t)
                setCamera(Vec3(lerp(startEye.x, endEye.x, eased), lerp(startEye.y, endEye.y, eased), lerp(startEye.z, endEye.z, eased)),
                    Vec3(lerp(startLook.x, endLook.x, eased), lerp(startLook.y, endLook.y, eased), lerp(startLook.z, endLook.z, eased)))
                delay(stepDelay)
            }
            setCamera(endEye, endLook)
        }
    }

    private fun startCameraInfoUpdates() {
        cameraInfoJob?.cancel()
        cameraInfoJob = lifecycleScope.launch {
            while (true) {
                val p = sceneView.cameraNode.position
                if (!isCameraLocked) {
                    currentCameraEye = Vec3(p.x, p.y, p.z)
                    if (binding.debugPanel.visibility == View.VISIBLE) updateSlidersFromCamera(currentCameraEye, currentOrbitTarget)
                }
                updateDebugText(currentCameraEye, currentOrbitTarget)
                delay(100L)
            }
        }
    }

    private fun setupCameraDebug() {
        listOf(binding.sbEyeX, binding.sbEyeY, binding.sbEyeZ, binding.sbLookAtX, binding.sbLookAtY, binding.sbLookAtZ).forEach {
            it.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, b: Boolean) { if (b) updateCameraFromSliders() }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
    }

    private fun updateSlidersFromCamera(eye: Vec3, look: Vec3) {
        fun f(v: Float) = ((v + 5f) / 10f * 1000f).toInt().coerceIn(0, 1000)
        binding.sbEyeX.progress = f(eye.x); binding.sbEyeY.progress = f(eye.y); binding.sbEyeZ.progress = f(eye.z)
        binding.sbLookAtX.progress = f(look.x); binding.sbLookAtY.progress = f(look.y); binding.sbLookAtZ.progress = f(look.z)
    }

    private fun updateCameraFromSliders() {
        fun g(p: Int) = (p / 1000f * 10f) - 5f
        currentCameraEye = Vec3(g(binding.sbEyeX.progress), g(binding.sbEyeY.progress), g(binding.sbEyeZ.progress))
        currentOrbitTarget = Vec3(g(binding.sbLookAtX.progress), g(binding.sbLookAtY.progress), g(binding.sbLookAtZ.progress))
        setCamera(currentCameraEye, currentOrbitTarget)
    }

    private fun updateDebugText(eye: Vec3, look: Vec3) {
        binding.tvDebugEyeValue.text = "Eye: (%.2f, %.2f, %.2f)".format(eye.x, eye.y, eye.z)
        binding.tvDebugLookAtValue.text = "Look: (%.2f, %.2f, %.2f)".format(look.x, look.y, look.z)
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    private fun easeInOutCubic(t: Float) = if (t < 0.5f) 4f * t * t * t else 1f - ((-2f * t + 2f).let { it * it * it } / 2f)
}
