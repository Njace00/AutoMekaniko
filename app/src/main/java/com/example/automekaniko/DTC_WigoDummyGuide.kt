package com.example.automekaniko

// Wigo DTC P0100 — Mass Air Flow Sensor Circuit Malfunction

private fun f(frame: Int): Float = frame / 24f

val WigoP0100Guide = DtcGuide(
    code        = "P0100",
    name        = "Mass Air Flow Circuit Malfunction",
    description = "P0100 indicates the Engine Control Module (ECM) detects an out-of-range " +
            "voltage signal from the Mass Air Flow (MAF) sensor.",
    parts       = listOf("MAF Sensor", "Air Filter Box", "Wiring Harness", "Intake Duct"),
    glbFile     = "wigo.glb",
    animationClipStartTimes = listOf(f(0)),
    vehicleId   = VehicleManager.WIGO.id,
    requiredTools = listOf("OBD2 Scanner", "10mm Socket", "MAF Cleaner Spray", "Microfiber Cloth"),
    estimatedTime = "15–20 mins",
    difficulty  = "Easy",
    prerequisites = listOf(
        "Park on level ground and switch off ignition",
        "Allow engine bay to cool before touching intake components"
    ),
    slides      = listOf(
        DtcSlide(
            title              = "Wigo Engine Bay Overview",
            description        = "Overview of Toyota Wigo 1.0L 1KR-VE engine bay and MAF sensor position.",
            eye                = Vec3(-4.80f, 2.20f, 4.80f),
            lookAt             = Vec3(0.00f,  0.20f,  0.00f),
            animationStartTime = f(0),
            animationTime      = f(0),
            animationDurationMs = 650L,
            steps              = listOf(
                ChecklistStep("Open Wigo Hood", "Secure the hood prop rod firmly into the designated slot."),
                ChecklistStep("Locate MAF Sensor", "The MAF sensor is mounted on the air intake tube behind the air filter box."),
                ChecklistStep("Inspect Wiring Harness", "Check for loose, corroded, or frayed wires leading to the sensor connector.")
            ),
            infoTitle = "Wigo P0100 Diagnostics",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("Engine Spec", "Toyota Wigo 1.0L 1KR-VE VVT-i 3-Cylinder"),
                MAINTAINANCEActivity.InfoItem("Cleaner Type", "Use dedicated MAF sensor cleaner spray only. Do not touch sensing wire with fingers.")
            )
        )
    )
)
