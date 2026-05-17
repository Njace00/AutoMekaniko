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
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

class DtcActivity : AppCompatActivity() {

    data class Vec3(val x: Float, val y: Float, val z: Float)

    data class CameraSlide(
        val title: String,
        val description: String,
        val eye: Vec3,
        val lookAt: Vec3,
        val steps: List<String> = emptyList()
    )

    data class DtcEntry(
        val code: String,
        val name: String,
        val description: String,
        //val severity: String,
        val parts: List<String>,
        val glbFile: String,
        val slides: List<CameraSlide>
    )

    // =========================================================================
    // DTC CATALOGUE
    // To add a new code: copy a DtcEntry block, fill in your values,
    // drop your GLB into assets/, and append the entry to this list.
    // =========================================================================
    private val dtcList = listOf(

        DtcEntry(
            code        = "P0301",
            name        = "Cylinder 1 Misfire Detected",
            description = "A misfire in cylinder 1 means the fuel-air mixture is not igniting " +
                    "correctly. Common causes: faulty spark plug, ignition coil, or injector.",
            //severity    = "Medium severity",
            parts       = listOf("Spark plug", "Ignition coil", "Fuel injector", "Coil boot"),
            glbFile     = "vios engine-tutor(P0301)misfire.glb",
            slides      = listOf(
                CameraSlide(
                    title       = "Locate Cylinder 1",
                    description = "Identify cylinder 1 on the engine bank.",
                    eye    = Vec3(-1.57f, 0.77f, -1.34f),
                    lookAt = Vec3(0.00f,  0.10f,  0.00f),
                    steps  = listOf(
                        "Open the hood",
                        "Locate the engine bank",
                        "Find cylinder 1 (front-most)"
                    )
                ),
                CameraSlide(
                    title       = "Remove Ignition Coil",
                    description = "Disconnect and remove the ignition coil.",
                    eye    = Vec3(-0.15f, 0.37f, -0.74f),
                    lookAt = Vec3(0.00f,  0.10f,  0.00f),
                    steps  = listOf(
                        "Disconnect coil electrical connector",
                        "Remove the coil bolt (10mm)",
                        "Pull the coil straight up"
                    )
                ),
                CameraSlide(
                    title       = "Remove Spark Plug",
                    description = "Use a spark plug socket to remove the old plug.",
                    eye    = Vec3(0.07f,  0.37f, -0.74f),
                    lookAt = Vec3(0.00f, -0.30f,  0.00f),
                    steps  = listOf(
                        "Attach spark plug socket to extension",
                        "Turn counter-clockwise to loosen",
                        "Remove plug carefully"
                    )
                ),
                CameraSlide(
                    title       = "Inspect and Replace",
                    description = "Check gap and install the new spark plug.",
                    eye    = Vec3(-0.03f, 0.50f, -0.77f),
                    lookAt = Vec3(0.10f, -0.20f,  0.00f),
                    steps  = listOf(
                        "Check gap on new plug (0.8-1.0mm)",
                        "Thread in new plug by hand",
                        "Torque to spec (20-25 Nm)",
                        "Reinstall coil and connector"
                    )
                ),
                CameraSlide(
                    title       = "Clear Code and Test",
                    description = "Clear the DTC and verify the fix.",
                    eye    = Vec3(-1.57f, 0.77f, -1.34f),
                    lookAt = Vec3(0.00f,  0.10f,  0.00f),
                    steps  = listOf(
                        "Use OBD scanner to clear P0301",
                        "Start engine and let it idle",
                        "Check for misfire on live data",
                        "Test drive and rescan"
                    )
                )
            )
        )

        // Add more DTC codes here:
        // DtcEntry(
        //     code        = "P0420",
        //     name        = "Catalyst System Efficiency Below Threshold",
        //     description = "...",
        //     severity    = "Low severity",
        //     parts       = listOf("Catalytic converter", "O2 sensor"),
        //     glbFile     = "your_model.glb",
        //     slides      = listOf( ... )
        // ),

    )

    // =========================================================================

    private lateinit var sceneView:   SceneView
    private lateinit var modelLoader: ModelLoader
    private lateinit var dtcSpinner:  Spinner
    private lateinit var topScroll:   ScrollView

    private lateinit var slidePanel:        ConstraintLayout
    private lateinit var checklistOverlay:  LinearLayout

    private lateinit var btnPrev:       Button
    private lateinit var btnNext:       Button
    //private lateinit var btnCameraLock: Button
    //private lateinit var tvCameraInfo: TextView
    private lateinit var slideTitle:    TextView
    private lateinit var slideDesc:     TextView

    private lateinit var lockOverlay:   View

    //private lateinit var tab3D:  TextView
    //private lateinit var tabOBD: TextView

    private lateinit var overlayTitle:       TextView
    private lateinit var checklistContainer: LinearLayout

    private lateinit var infoCard:       CardView
    private lateinit var tvDtcCode:      TextView
    private lateinit var tvDtcName:      TextView
    //private lateinit var tvSeverity:     TextView
    private lateinit var tvDtcDesc:      TextView
    private lateinit var partsSection:   LinearLayout
    private lateinit var partsContainer: LinearLayout

    private var currentModelNode:  ModelNode? = null
    private var currentEntry:      DtcEntry?  = null
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

        btnPrev       = findViewById(R.id.btnPrev)
        btnNext       = findViewById(R.id.btnNext)
        //btnCameraLock = findViewById(R.id.btnCameraLock)
        slideTitle    = findViewById(R.id.slideTitle)
        slideDesc     = findViewById(R.id.slideDesc)
        //  = findViewById(R.id.tvCameraInfo)
        lockOverlay   = findViewById(R.id.lockOverlay)
        //tab3D         = findViewById(R.id.tab3D)
        //tabOBD        = findViewById(R.id.tabOBD)

        overlayTitle       = findViewById(R.id.overlayTitle)
        checklistContainer = findViewById(R.id.checklistContainer)

        infoCard       = findViewById(R.id.infoCard)
        tvDtcCode      = findViewById(R.id.tvDtcCode)
        tvDtcName      = findViewById(R.id.tvDtcName)
        //tvSeverity     = findViewById(R.id.tvSeverity)
        tvDtcDesc      = findViewById(R.id.tvDtcDesc)
        partsSection   = findViewById(R.id.partsSection)
        partsContainer = findViewById(R.id.partsContainer)

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
    // DTC Spinner
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
    // Load a DTC entry
    // -------------------------------------------------------------------------

    private fun loadDtcEntry(entry: DtcEntry) {
        currentEntry      = entry
        currentSlideIndex = 0

        tvDtcCode.text  = entry.code
        tvDtcName.text  = entry.name
        tvDtcDesc.text  = entry.description
        //tvSeverity.text = entry.severity

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
    // Model loading — no GLB animation
    // -------------------------------------------------------------------------

    private fun loadModel(fileName: String, slides: List<CameraSlide>) {
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
                startEye  = currentCameraEye,
                startLook = currentOrbitTarget,
                endEye    = slide.eye,
                endLook   = slide.lookAt,
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