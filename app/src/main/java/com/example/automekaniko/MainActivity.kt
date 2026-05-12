package com.example.automekaniko

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {

    data class Vec3(val x: Float, val y: Float, val z: Float)

    data class CameraSlide(
        val title: String,
        val description: String,
        val eye: Vec3,
        val lookAt: Vec3
    )

    private lateinit var sceneView: SceneView
    private lateinit var modelLoader: ModelLoader
    private lateinit var modelSpinner: Spinner

    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnCameraLock: Button
    private lateinit var slideTitle: TextView
    private lateinit var slideDesc: TextView
    private lateinit var tvCameraInfo: TextView
    private lateinit var lockOverlay: View

    private var currentModelNode: ModelNode? = null
    private var currentSlideIndex = 0
    private var isCameraLocked = true

    private var cameraAnimJob: Job? = null
    private var cameraInfoJob: Job? = null

    private var currentCameraEye = Vec3(0f, 0f, 0f)
    private var currentOrbitTarget = Vec3(0f, 0.5f, 0f)

    private val startEye = Vec3(0f, 0.945f, -1.22f)
    private val startLookAt = Vec3(0f, 0.5f, 0f)

    private var savedManipulator: CameraGestureDetector.CameraManipulator? = null
    private var manipulatorCaptured = false

    // Keep hood closed by forcing this clip to frame 0.
    private val hoodAnimationIndex = 0

    private val slides = listOf(
        CameraSlide(
            title = "Vehicle Overview",
            description = "This is the Preview of the Vehicle...",
            eye = Vec3(-1.57f, 0.77f, -1.34f),
            lookAt = Vec3(0.00f, 0.10f, 0.00f)
        ),
        CameraSlide(
            title = "Vehicle Overview2",
            description = "This is the Preview of the Vehicle...",
            eye = Vec3(-1.57f, 0.77f, -1.34f),
            lookAt = Vec3(0.00f, 0.10f, 0.00f)
        ),
        CameraSlide(
            title = "Battery",
            description = "Check Battery...",
            eye = Vec3(-0.15f, 0.37f, -0.74f),
            lookAt = Vec3(0.00f, 0.10f, 0.00f)
        ),
        CameraSlide(
            title = "Lights",
            description = "Check all Lights:...",
            eye = Vec3(-0.01f, 0.55f, -1.25f),
            lookAt = Vec3(-0.01f, 0.10f, 0.00f)
        ),
        CameraSlide(
            title = "Oil",
            description = "Chech your oil, and oil level..",
            eye = Vec3(0.07f, 0.37f, -0.74f),
            lookAt = Vec3(0.00f, -0.3f, 0.00f)
        ),
        CameraSlide(
            title = "Water",
            description = "Check Water Radiator Level...",
            eye = Vec3(-0.01f, 0.37f, -0.69f),
            lookAt = Vec3(0.00f, -0.6f, 0.00f)
        ),

        CameraSlide(
            title = "Brake",
            description = "Check Brake...",
            eye = Vec3(-0.7f, 0.15f, -1.2f),
            lookAt = Vec3(0.00f, 0f, 0.00f)
        ),

        CameraSlide(
            title = "Tire Air Pressure",
            description = "Check tire...",
            eye = Vec3(0.7f, 0.15f, -1.2f),
            lookAt = Vec3(0.00f, 0f, 0.00f)
        ),

        CameraSlide(
            title = "Engine",
            description = "Inspect for Unusual Engine Behaviors and Sounds",
            eye = Vec3(-0.03f, 0.50f, -0.770f),
            lookAt = Vec3(0.10f, -0.20f, 0.00f)
        ),


        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView)
        modelSpinner = findViewById(R.id.modelSpinner)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnCameraLock = findViewById(R.id.btnCameraLock)
        slideTitle = findViewById(R.id.slideTitle)
        slideDesc = findViewById(R.id.slideDesc)
        tvCameraInfo = findViewById(R.id.tvCameraInfo)
        lockOverlay = findViewById(R.id.lockOverlay)

        modelLoader = ModelLoader(sceneView.engine, this)

        // Force hood closed every render frame.
        sceneView.onFrame = {
            enforceHoodPoseBySlide()
        }



        captureManipulatorOnce()
        setupModelSelector()
        setupControls()
        setCameraLockState(true)
        startCameraInfoUpdates()
    }

    override fun onDestroy() {
        cameraAnimJob?.cancel()
        cameraInfoJob?.cancel()
        super.onDestroy()
    }

    private fun captureManipulatorOnce() {
        if (!manipulatorCaptured) {
            savedManipulator = sceneView.cameraManipulator
            manipulatorCaptured = true
        }
    }

    private fun setupModelSelector() {
        val models = assets.list("")?.filter { it.endsWith(".glb") }?.sorted() ?: emptyList()

        if (models.isEmpty()) {
            modelSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("No models found")
            )
            return
        }

        modelSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            models
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(0xFFFFD700.toInt())
                loadModel(models[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }

    private fun setupControls() {
        btnPrev.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            val to = (currentSlideIndex - 1).coerceAtLeast(0)

            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
            updateUiState()
        }

        btnNext.setOnClickListener {
            if (!isCameraLocked) return@setOnClickListener
            val to = (currentSlideIndex + 1).coerceAtMost(slides.lastIndex)

            sceneView.cameraManipulator = null
            currentSlideIndex = to
            goToSlide(to, animated = true)
            updateUiState()
        }

        btnCameraLock.setOnClickListener {
            setCameraLockState(!isCameraLocked)
        }

        updateUiState()
    }

    private fun setCameraLockState(locked: Boolean) {
        isCameraLocked = locked

        if (locked) {
            if (savedManipulator == null) savedManipulator = sceneView.cameraManipulator
            sceneView.cameraManipulator = null
            lockOverlay.visibility = View.VISIBLE
            currentModelNode?.isEditable = false
        } else {
            if (sceneView.cameraManipulator == null && savedManipulator != null) {
                sceneView.cameraManipulator = savedManipulator
            }
            lockOverlay.visibility = View.GONE
            currentModelNode?.isEditable = true

            val p = sceneView.cameraNode.position
            currentCameraEye = Vec3(p.x, p.y, p.z)
        }

        updateUiState()
    }

    private fun updateUiState() {
        btnCameraLock.text = if (isCameraLocked) "Camera: LOCK" else "Camera: FREE"
        btnPrev.isEnabled = isCameraLocked && currentSlideIndex > 0
        btnNext.isEnabled = isCameraLocked && currentSlideIndex < slides.lastIndex
    }

    private fun loadModel(fileName: String) {
        lifecycleScope.launch {
            currentModelNode?.let {
                sceneView.removeChildNode(it)
                it.destroy()
            }

            val instance = modelLoader.createModelInstance(assetFileLocation = fileName)
            val modelNode = ModelNode(instance, scaleToUnits = 1.5f).apply {
                isEditable = !isCameraLocked
            }

            // Initialize all clips to frame 0 once.
            modelNode.modelInstance.animator?.let { animator ->
                repeat(animator.animationCount) { i ->
                    animator.applyAnimation(i, 0f)
                }
                animator.updateBoneMatrices()
            }

            sceneView.addChildNode(modelNode)
            currentModelNode = modelNode

            applyCustomStartCamera()

            currentSlideIndex = 0
            slideTitle.text = slides[currentSlideIndex].title
            slideDesc.text = slides[currentSlideIndex].description
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

    private fun goToSlide(index: Int, animated: Boolean) {
        val slide = slides[index]
        slideTitle.text = slide.title
        slideDesc.text = slide.description

        if (animated) {
            animateCameraPose(
                startEye = currentCameraEye,
                startLook = currentOrbitTarget,
                endEye = slide.eye,
                endLook = slide.lookAt,
                durationMs = 650L
            )
        } else {
            setCamera(slide.eye, slide.lookAt)
        }
    }

    private fun enforceHoodPoseBySlide() {

        val modelNode = currentModelNode ?: return
        val animator = modelNode.modelInstance.animator ?: return
        val duration = animator.getAnimationDuration(hoodAnimationIndex)
        val end = (duration - 0.001f).coerceAtLeast(0f)
        val t = if (currentSlideIndex >= 1) end else 0f
        animator.applyAnimation(hoodAnimationIndex, t)
        animator.updateBoneMatrices()

    }



    private fun setCamera(cameraPos: Vec3, lookTarget: Vec3) {
        currentCameraEye = cameraPos
        currentOrbitTarget = lookTarget
        sceneView.cameraNode.position = Position(cameraPos.x, cameraPos.y, cameraPos.z)
        sceneView.cameraNode.lookAt(Position(lookTarget.x, lookTarget.y, lookTarget.z))
    }

    private fun animateCameraPose(
        startEye: Vec3,
        startLook: Vec3,
        endEye: Vec3,
        endLook: Vec3,
        durationMs: Long
    ) {
        cameraAnimJob?.cancel()
        cameraAnimJob = lifecycleScope.launch {
            val steps = 30
            val stepDelay = (durationMs / steps).coerceAtLeast(1L)

            repeat(steps) { i ->
                val t = (i + 1) / steps.toFloat()
                val eased = easeInOutCubic(t)

                val eye = Vec3(
                    lerp(startEye.x, endEye.x, eased),
                    lerp(startEye.y, endEye.y, eased),
                    lerp(startEye.z, endEye.z, eased)
                )
                val look = Vec3(
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

    private fun startCameraInfoUpdates() {
        cameraInfoJob?.cancel()
        cameraInfoJob = lifecycleScope.launch {
            while (true) {
                val p = sceneView.cameraNode.position
                val cam = Vec3(p.x, p.y, p.z)
                if (!isCameraLocked) currentCameraEye = cam

                val target = currentOrbitTarget
                val dx = cam.x - target.x
                val dy = cam.y - target.y
                val dz = cam.z - target.z

                val distance = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(0.0001f)
                val yaw = Math.toDegrees(atan2(dx, dz).toDouble()).toFloat()
                val pitch = Math.toDegrees(asin((dy / distance).toDouble())).toFloat()

                tvCameraInfo.text =
                    "eye=(%.2f, %.2f, %.2f) target=(%.2f, %.2f, %.2f) d=%.2f yaw=%.1f pitch=%.1f"
                        .format(cam.x, cam.y, cam.z, target.x, target.y, target.z, distance, yaw, pitch)

                delay(120L)
            }
        }
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    private fun easeInOutCubic(t: Float): Float {
        return if (t < 0.5f) 4f * t * t * t
        else 1f - ((-2f * t + 2f).let { it * it * it } / 2f)
    }
}