package com.example.automekaniko

data class MaintenanceGuide(
    val name: String,
    val glbFile: String,
    val slides: List<MAINTAINANCEActivity.CameraSlide>
)
