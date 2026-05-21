package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
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

    // Data comes entirely from DtcGuideRegistry.kt — nothing hardcoded here.
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
    private var cameraInfoJob: Job? = null

    private var currentCameraEye   = Vec3(0f, 0f, 0f)
    private var currentOrbitTarget = Vec3(0f, 0.5f, 0f)

    private val startEye    = Vec3(0f, 0.945f, -1.22f)
    private val startLookAt = Vec3(0f, 0.5f, 0f)

    private var savedManipulator:    CameraGestureDetector.CameraManipulator? = null
    private var manipulatorCaptured: Boolean = false

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3d_dtc_guide)

        sceneView        = findViewById(R.id.sceneView)
        dtcSpinner       = findViewById(R.id.dtcSpinner)
        topScroll        = findViewById(R.id.topScroll)
        slidePanel       = findViewById(R.id.slidePanel)
        checklistOverlay = findViewById(R.id.checklistOverlay)
        btnPrev          = findViewById(R.id.btnPrev)
        btnNext          = findViewById(R.id.btnNext)
        slideTitle       = findViewById(R.id.slideTitle)
        slideDesc        = findViewById(R.id.slideDesc)
        lockOverlay      = findViewById(R.id.lockOverlay)
        overlayTitle     = findViewById(R.id.overlayTitle)
        checklistContainer = findViewById(R.id.checklistContainer)
        infoCard         = findViewById(R.id.infoCard)
        tvDtcCode        = findViewById(R.id.tvDtcCode)
        tvDtcName        = findViewById(R.id.tvDtcName)
        tvDtcDesc        = findViewById(R.id.tvDtcDesc)
        partsSection     = findViewById(R.id.partsSection)
        partsContainer   = findViewById(R.id.partsContainer)

        modelLoader = ModelLoader(sceneView.engine, this)

        captureManipulatorOnce()
        setupDtcSpinner()
        setupControls()
        setCameraLockState(true)
    }

    override fun onDestroy() {
        cameraAnimJob?.cancel()
        cameraInfoJob?.cancel()
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // Spinner — populated from dtcGuides registry
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
    // Load a guide entry — populate info card then load GLB
    // -------------------------------------------------------------------------

    private fun loadDtcEntry(entry: DtcGuide) {
        currentEntry      = entry
        currentSlideIndex = 0

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
        topScroll.visibility        = View.VISIBLE
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
            }

            val instance  = modelLoader.createModelInstance(assetFileLocation = fileName)
            val modelNode = ModelNode(instance, scaleToUnits = 1.5f).apply {
                isEditable = !isCameraLocked
            }

            sceneView.addChildNode(modelNode)
            currentModelNode = modelNode

            applyCustomStartCamera()

            currentSlideIndex = 0
            slideTitle.text   = slides[0].title
            slideDesc.text    = slides[0].description
            goToSlide(0, animated = false)
            updateUiState()
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
            updateUiState()
        }

        btnNext.setOnClickListener {
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
        updateUiState()
    }

    private fun updateUiState() {
        val entry = currentEntry
        btnPrev.isEnabled = isCameraLocked && currentSlideIndex > 0
        btnNext.isEnabled = isCameraLocked && entry != null && currentSlideIndex < entry.slides.lastIndex
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
        } else {
            setCamera(slide.eye, slide.lookAt)
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