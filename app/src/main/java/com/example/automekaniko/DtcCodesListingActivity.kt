package com.example.automekaniko

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DtcCodesListingActivity : AppCompatActivity() {

    private val dtcList = dtcGuides

    // ---- Views ----
    private lateinit var sceneView: SceneView
    private lateinit var modelLoader: ModelLoader
    private lateinit var btnUp: ImageView
    private lateinit var btnDown: ImageView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvDtcCode: TextView
    private lateinit var tvDtcDescription: TextView
    private lateinit var backBtn: ImageView

    // ---- State ----
    private var currentModelNode: ModelNode? = null
    private var currentDtcIndex: Int = 0
    private var isSceneReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dtc_codes_listing)

        // Set app title with red "Mekaniko"
        val appTitle = findViewById<TextView>(R.id.appTitle)
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFFFFFFFF.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        appTitle.text = spannable
        AppNavigation.wire(this)

        // Initialize views
        sceneView = findViewById(R.id.sceneView)
        btnUp = findViewById(R.id.btnUp)
        btnDown = findViewById(R.id.btnDown)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        tvDtcCode = findViewById(R.id.tvDtcCode)
        tvDtcDescription = findViewById(R.id.tvDtcDescription)
        backBtn = findViewById(R.id.backBtn)

        try {
            modelLoader = ModelLoader(sceneView.engine, this)
            isSceneReady = true
        } catch (e: Exception) {
            Log.e("DtcCodesListing", "Failed to initialize ModelLoader", e)
            isSceneReady = false
        }

        setupControls()
        setupNavigation()
        
        // Load the first DTC entry
        if (dtcList.isNotEmpty()) {
            if (isSceneReady) {
                loadDtcEntry(0)
            } else {
                lifecycleScope.launch {
                    delay(500)
                    try {
                        modelLoader = ModelLoader(sceneView.engine, this@DtcCodesListingActivity)
                        isSceneReady = true
                        loadDtcEntry(0)
                    } catch (e: Exception) {
                        Log.e("DtcCodesListing", "Failed to initialize scene", e)
                    }
                }
            }
        }

        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupControls() {
        btnUp.setOnClickListener {
            navigatePrevious()
        }

        btnDown.setOnClickListener {
            navigateNext()
        }

        btnPrev.setOnClickListener {
            navigatePrevious()
        }

        btnNext.setOnClickListener {
            navigateNext()
        }

        updateButtonStates()
    }

    private fun setupNavigation() {
        // Navigation is handled by AppNavigation.wire() called in onCreate
    }

    private fun navigatePrevious() {
        if (currentDtcIndex > 0) {
            currentDtcIndex--
            loadDtcEntry(currentDtcIndex)
        }
    }

    private fun navigateNext() {
        if (currentDtcIndex < dtcList.size - 1) {
            currentDtcIndex++
            loadDtcEntry(currentDtcIndex)
        }
    }

    private fun loadDtcEntry(index: Int) {
        if (index < 0 || index >= dtcList.size) return

        val entry = dtcList[index]
        currentDtcIndex = index

        // Update UI with DTC info
        tvDtcCode.text = "${entry.code} — ${entry.name}"
        tvDtcDescription.text = entry.description

        // Load the 3D model
        loadModel(entry.glbFile)

        updateButtonStates()
    }

    private fun loadModel(fileName: String) {
        lifecycleScope.launch {
            try {
                // Remove previous model
                currentModelNode?.let {
                    sceneView.removeChildNode(it)
                    it.destroy()
                    currentModelNode = null
                }

                Log.d("DtcCodesListing", "Loading GLB: $fileName")
                
                val instance = modelLoader.createModelInstance(assetFileLocation = fileName)
                Log.d("DtcCodesListing", "Model instance created successfully")
                
                val modelNode = ModelNode(
                    modelInstance = instance,
                    autoAnimate = false,
                    scaleToUnits = 1.5f
                ).apply {
                    isEditable = false
                }

                sceneView.addChildNode(modelNode)
                currentModelNode = modelNode
                Log.d("DtcCodesListing", "Model added to scene")

                // Set default camera position
                setDefaultCameraPosition()
                
            } catch (e: Exception) {
                Log.e("DtcCodesListing", "Failed to load model: $fileName", e)
                e.printStackTrace()
                Toast.makeText(
                    this@DtcCodesListingActivity,
                    "Failed to load model: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setDefaultCameraPosition() {
        try {
            val cameraPos = Position(0f, 0.9f, -1.2f)
            val lookTarget = Position(0f, 0.5f, 0f)
            sceneView.cameraNode.position = cameraPos
            sceneView.cameraNode.lookAt(lookTarget)
        } catch (e: Exception) {
            Log.e("DtcCodesListing", "Error setting camera position", e)
        }
    }

    private fun updateButtonStates() {
        btnUp.isEnabled = currentDtcIndex > 0
        btnDown.isEnabled = currentDtcIndex < dtcList.size - 1
        btnPrev.isEnabled = currentDtcIndex > 0
        btnNext.isEnabled = currentDtcIndex < dtcList.size - 1

        btnUp.alpha = if (btnUp.isEnabled) 1f else 0.5f
        btnDown.alpha = if (btnDown.isEnabled) 1f else 0.5f
        btnPrev.alpha = if (btnPrev.isEnabled) 1f else 0.5f
        btnNext.alpha = if (btnNext.isEnabled) 1f else 0.5f
    }

    override fun onDestroy() {
        currentModelNode?.let {
            try {
                sceneView.removeChildNode(it)
                it.destroy()
            } catch (e: Exception) {
                Log.e("DtcCodesListing", "Error destroying model", e)
            }
        }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}
