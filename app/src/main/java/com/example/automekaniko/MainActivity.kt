package com.example.automekaniko

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: SceneView
    private lateinit var modelLoader: ModelLoader
    private lateinit var modelSpinner: Spinner

    private var currentModelNode: ModelNode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView)
        modelSpinner = findViewById(R.id.modelSpinner)
        modelLoader = ModelLoader(sceneView.engine, this)

        setupModelSelector()
    }

    private fun setupModelSelector() {
        // Dynamically list all .glb files from the assets folder
        val models = assets.list("")
            ?.filter { it.endsWith(".glb") }
            ?.sorted()
            ?: emptyList()

        if (models.isEmpty()) {
            // No models found — show a placeholder
            val emptyAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("No models found")
            )
            modelSpinner.adapter = emptyAdapter
            return
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            models
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        modelSpinner.adapter = adapter

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                // Style the selected item text gold
                (view as? TextView)?.setTextColor(0xFFFFD700.toInt())
                loadModel(models[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadModel(fileName: String) {
        lifecycleScope.launch {
            // Remove previous model if any
            currentModelNode?.let {
                sceneView.removeChildNode(it)
                it.destroy()
            }

            val modelInstance = modelLoader.createModelInstance(
                assetFileLocation = fileName
            )
            val modelNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 1.5f
            ).apply {
                isEditable = true
            }

            sceneView.addChildNode(modelNode)
            currentModelNode = modelNode
        }
    }
}