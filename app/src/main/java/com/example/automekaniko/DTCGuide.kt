package com.example.automekaniko

// Shared data classes for the DTC guide system only.
data class Vec3(val x: Float, val y: Float, val z: Float)

data class DtcSlide(
    val title: String,
    val description: String,
    val eye: Vec3,
    val lookAt: Vec3,
    val steps: List<ChecklistStep> = emptyList(),
    val animationStartTime: Float = 0f,
    val animationTime: Float = 0f,
    val animationDurationMs: Long = 650L,
    val infoTitle: String? = null,
    val infoItems: List<MAINTAINANCEActivity.InfoItem> = emptyList(),
    val removeFirst: String? = null,
    val teardownPath: String? = null,
    val targetPartName: String? = null,
    val targetPartLocationNote: String? = null
)

data class DtcGuide(
    val code: String,
    val name: String,
    val description: String,
    val parts: List<String>,
    val glbFile: String,
    val animationClipStartTimes: List<Float> = emptyList(),
    val slides: List<DtcSlide>,
    val requiredTools: List<String> = listOf("OBD2 Scanner", "10mm Socket / Wrench", "Safety Gloves"),
    val estimatedTime: String = "30–45 mins",
    val difficulty: String = "Moderate",
    val prerequisites: List<String> = listOf("Set parking brake firmly", "Turn off ignition completely", "Disconnect negative battery terminal if working near electrical parts")
)
