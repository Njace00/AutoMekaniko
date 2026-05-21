package com.example.automekaniko

// Shared data classes — all DTC guide files and DtcActivity use these.
// Do not put any logic here, just data shapes.

data class Vec3(val x: Float, val y: Float, val z: Float)

data class DtcSlide(
    val title: String,
    val description: String,
    val eye: Vec3,
    val lookAt: Vec3,
    val steps: List<String> = emptyList()
)

data class DtcGuide(
    val code: String,
    val name: String,
    val description: String,
    val parts: List<String>,
    val glbFile: String,
    val slides: List<DtcSlide>
)