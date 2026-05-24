package com.example.automekaniko

private typealias ChangeOilSlide = MAINTAINANCEActivity.CameraSlide
private typealias ChangeOilVec3 = MAINTAINANCEActivity.Vec3

// CHANGE OIL - Engine Oil Maintenance
// GLB: vios_engine-tutor-Wanimation_ChangeOil.glb
//
// General maintenance guide for Toyota Vios engine oil replacement.

private fun f(frame: Int): Float = frame / 24f

val ChangeOilGuide = MaintenanceGuide(
    name = "Engine Oil Change",
    glbFile = "vios_engine-tutor-Wanimation_ChangeOil.glb",
    slides = listOf(

        ChangeOilSlide(
            title = "Overview",
            description = "Prepare the vehicle and tools before changing the engine oil.",
            eye = ChangeOilVec3(-1.50f, 0.80f, -1.40f),
            lookAt = ChangeOilVec3(0.00f, 0.10f, 0.00f),
            animationStartTime = f(0),
            animationTime = f(0),
            animationDurationMs = 700L,
            steps = listOf(
                "Park on a level surface",
                "Turn off the engine",
                "Prepare oil drain pan and tools"
            )
        ),

        ChangeOilSlide(
            title = "Locate Drain Plug",
            description = "Locate the engine oil drain plug underneath the vehicle.",
            eye = ChangeOilVec3(0.20f, -0.20f, -0.90f),
            lookAt = ChangeOilVec3(0.00f, -0.30f, 0.00f),
            animationStartTime = f(5),
            animationTime = f(45),
            animationDurationMs = 900L,
            steps = listOf(
                "Lift the vehicle safely if necessary",
                "Place oil drain pan underneath",
                "Locate the drain plug on the oil pan"
            )
        ),

        ChangeOilSlide(
            title = "Drain Old Oil",
            description = "Remove the drain plug and drain the old engine oil.",
            eye = ChangeOilVec3(0.15f, -0.18f, -0.75f),
            lookAt = ChangeOilVec3(0.00f, -0.25f, 0.00f),
            animationStartTime = f(50),
            animationTime = f(95),
            animationDurationMs = 1200L,
            steps = listOf(
                "Loosen the drain plug carefully",
                "Allow old oil to fully drain",
                "Inspect the drain plug and washer"
            )
        ),

        ChangeOilSlide(
            title = "Replace Oil Filter",
            description = "Remove and replace the old oil filter.",
            eye = ChangeOilVec3(0.08f, 0.10f, -0.60f),
            lookAt = ChangeOilVec3(0.05f, 0.00f, 0.00f),
            animationStartTime = f(100),
            animationTime = f(150),
            animationDurationMs = 1000L,
            steps = listOf(
                "Remove the old oil filter",
                "Apply fresh oil to new filter gasket",
                "Install the new oil filter securely"
            )
        ),

        ChangeOilSlide(
            title = "Install Drain Plug",
            description = "Reinstall and tighten the oil drain plug properly.",
            eye = ChangeOilVec3(0.15f, -0.18f, -0.75f),
            lookAt = ChangeOilVec3(0.00f, -0.25f, 0.00f),
            animationStartTime = f(155),
            animationTime = f(190),
            animationDurationMs = 700L,
            steps = listOf(
                "Install new drain plug washer if needed",
                "Thread the drain plug carefully",
                "Tighten to proper torque specification"
            )
        ),

        ChangeOilSlide(
            title = "Add New Engine Oil",
            description = "Refill the engine with the correct oil type and amount.",
            eye = ChangeOilVec3(0.05f, 0.45f, -0.85f),
            lookAt = ChangeOilVec3(0.00f, 0.20f, 0.00f),
            animationStartTime = f(195),
            animationTime = f(245),
            animationDurationMs = 1000L,
            steps = listOf(
                "Open the oil filler cap",
                "Pour the correct amount of oil",
                "Check oil level using dipstick"
            )
        ),

        ChangeOilSlide(
            title = "Final Inspection",
            description = "Start the engine and inspect for leaks.",
            eye = ChangeOilVec3(-1.30f, 0.75f, -1.20f),
            lookAt = ChangeOilVec3(0.00f, 0.10f, 0.00f),
            animationStartTime = f(250),
            animationTime = f(300),
            animationDurationMs = 1200L,
            steps = listOf(
                "Start the engine",
                "Inspect for oil leaks",
                "Recheck oil level after a few minutes",
                "Dispose of old oil properly"
            )
        )
    )
)
