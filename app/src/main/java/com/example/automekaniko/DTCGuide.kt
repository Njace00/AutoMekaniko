package com.example.automekaniko

import com.google.android.filament.gltfio.Animator

// Shared data classes for the guide system.
data class Vec3(val x: Float, val y: Float, val z: Float)

data class GuideStep(
    val title: String,
    val description: String = ""
)

data class InfoItem(
    val title: String,
    val description: String,
    val imageResId: Int? = null
)

data class DtcSlide(
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

data class DtcGuide(
    val code: String,
    val name: String,
    val description: String,
    val parts: List<String>,
    val glbFile: String,
    val animationClipStartTimes: List<Float> = emptyList(),
    val slides: List<DtcSlide>
)

fun applyAnimationClips(
    animator: Animator,
    time: Float,
    clipStartTimes: List<Float>
) {
    repeat(animator.animationCount) { i ->
        val clipStartTime = clipStartTimes.getOrNull(i) ?: 0f
        if (time >= clipStartTime) {
            val duration = animator.getAnimationDuration(i)
            animator.applyAnimation(i, time.coerceIn(0f, duration))
        }
    }
}
