package com.example.automekaniko

data class MaintenanceGuide(
    val name: String,
    val glbFile: String,
    val slides: List<MAINTAINANCEActivity.CameraSlide>,
    val requiredTools: List<String> = listOf("Basic Hand Tools", "Safety Glasses"),
    val estimatedTime: String = "20–30 mins",
    val difficulty: String = "Easy",
    val prerequisites: List<String> = listOf("Park on level ground", "Allow engine to cool completely")
)
