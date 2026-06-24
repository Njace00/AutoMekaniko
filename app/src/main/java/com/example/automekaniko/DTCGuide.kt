package com.example.automekaniko

// Shared data classes for the DTC guide system only.
// Vec3 here is top-level — MAINTAINANCEActivity and SceneViewActivity
// have their OWN inner Vec3 scoped inside those classes, so no conflict.

data class Vec3(val x: Float, val y: Float, val z: Float)

data class DtcSlide(
    val title: String,
    val description: String,
    val eye: Vec3,
    val lookAt: Vec3,
    val steps: List<String> = emptyList(),
    val animationStartTime: Float = 0f,
    val animationTime: Float = 0f,
    val animationDurationMs: Long = 650L,
    val infoTitle: String? = null,
    val infoItems: List<MAINTAINANCEActivity.InfoItem> = emptyList()
)

data class DtcGuide(
    val code: String,
    val name: String,
    val description: String,
    val parts: List<String>,
    val glbFile: String,
    val animationClipStartTimes: List<Float> = emptyList(),
    val slides: List<DtcSlide>
)
