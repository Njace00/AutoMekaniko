package com.example.automekaniko

// Wigo Dummy Guide — P0100 Mass Air Flow Circuit Inspection (Wigo 1.0L)

private fun f(frame: Int): Float = frame / 24f

val WigoP0100Guide = DtcGuide(
    code        = "P0100",
    name        = "MAF Circuit Malfunction (Wigo 1.0L)",
    description = "Mass or Volume Air Flow Circuit Malfunction on Toyota Wigo 1.0L. " +
            "Inspect intake ducting, sensor connector, and clean MAF sensor element.",
    parts       = listOf("MAF Sensor", "Intake Hose", "Air Filter Box", "Wiring Connector"),
    glbFile     = "Vehicle Preventive Maintenance Checklist (VPMC).glb",
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
            eye                = Vec3(-1.57f, 0.77f, -1.34f),
            lookAt             = Vec3(0.00f,  0.10f,  0.00f),
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
