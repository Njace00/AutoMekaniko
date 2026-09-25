package com.example.automekaniko

private typealias WigoSlide = MAINTAINANCEActivity.CameraSlide
private typealias WigoVec3 = MAINTAINANCEActivity.Vec3

private fun f(frame: Int): Float = frame / 24f

val WigoVPMCGuide = MaintenanceGuide(
    name = "Toyota Wigo Preventive Maintenance (VPMC)",
    glbFile = "wigo.glb",
    vehicleId = VehicleManager.WIGO.id,
    requiredTools = listOf("Tire Pressure Gauge", "Dipstick Rag", "Flashlight"),
    estimatedTime = "15–20 mins",
    difficulty = "Easy",
    prerequisites = listOf(
        "Park Toyota Wigo on flat, level ground",
        "Set handbrake firmly and turn off ignition"
    ),
    slides = listOf(
        WigoSlide(
            title = "Wigo 1.0L Vehicle Overview",
            description = "General inspection walkthrough for Toyota Wigo 1.0L 3-cylinder engine and fluid levels.",
            eye = WigoVec3(-2.20f, 1.20f, 2.20f),
            lookAt = WigoVec3(0.00f, 0.20f, 0.00f),
            steps = listOf(
                ChecklistStep("Inspect Oil Dipstick", "Pull 1KR-VE dipstick, wipe clean, reinsert and confirm oil level is between MIN and MAX marks."),
                ChecklistStep("Check Coolant Reservoir", "Ensure pink SLLC coolant level is near FULL line in translucent reservoir."),
                ChecklistStep("Check Battery & Terminals", "Inspect Wigo 40B19L battery terminals for corrosion or loose clamps.")
            ),
            animationStartTime = f(0),
            animationTime = f(0),
            animationDurationMs = 650L,
            infoTitle = "Wigo 1.0L Maintenance Specs",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("Engine Oil", "3.1 Liters 0W-20 Full Synthetic"),
                MAINTAINANCEActivity.InfoItem("Battery Size", "40B19L 12V Battery"),
                MAINTAINANCEActivity.InfoItem("Spark Plugs", "3x Long Reach Iridium Plugs")
            )
        ),

        WigoSlide(
            title = "Wigo 1.0L Vehicle Overview",
            description = "General inspection walkthrough for Toyota Wigo 1.0L 3-cylinder engine and fluid levels.",
            eye = WigoVec3(-0.14f, 0.47f, 1.60f),
            lookAt = WigoVec3(0.26f,  0.29f,  -5.00f),
            steps = listOf(
                ChecklistStep("Inspect Oil Dipstick", "Pull 1KR-VE dipstick, wipe clean, reinsert and confirm oil level is between MIN and MAX marks."),
                ChecklistStep("Check Coolant Reservoir", "Ensure pink SLLC coolant level is near FULL line in translucent reservoir."),
                ChecklistStep("Check Battery & Terminals", "Inspect Wigo 40B19L battery terminals for corrosion or loose clamps.")
            ),
            animationStartTime = f(0),
            animationTime = f(0),
            animationDurationMs = 650L,
            infoTitle = "Wigo 1.0L Maintenance Specs",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("Engine Oil", "3.1 Liters 0W-20 Full Synthetic"),
                MAINTAINANCEActivity.InfoItem("Battery Size", "40B19L 12V Battery"),
                MAINTAINANCEActivity.InfoItem("Spark Plugs", "3x Long Reach Iridium Plugs")
            )
        ),

        WigoSlide(
            title = "Wigo 1.0L Vehicle Overview",
            description = "General inspection walkthrough for Toyota Wigo 1.0L 3-cylinder engine and fluid levels.",
            eye = WigoVec3(-0.14f, 0.47f, 1.60f),
            lookAt = WigoVec3(0.26f,  0.29f,  -5.00f),
            steps = listOf(
                ChecklistStep("Inspect Oil Dipstick", "Pull 1KR-VE dipstick, wipe clean, reinsert and confirm oil level is between MIN and MAX marks."),
                ChecklistStep("Check Coolant Reservoir", "Ensure pink SLLC coolant level is near FULL line in translucent reservoir."),
                ChecklistStep("Check Battery & Terminals", "Inspect Wigo 40B19L battery terminals for corrosion or loose clamps.")
            ),
            animationStartTime = f(0),
            animationTime = f(0),
            animationDurationMs = 650L,
            infoTitle = "Wigo 1.0L Maintenance Specs",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("Engine Oil", "3.1 Liters 0W-20 Full Synthetic"),
                MAINTAINANCEActivity.InfoItem("Battery Size", "40B19L 12V Battery"),
                MAINTAINANCEActivity.InfoItem("Spark Plugs", "3x Long Reach Iridium Plugs")
            )
        ),




    )
)
