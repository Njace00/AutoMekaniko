package com.example.automekaniko

private typealias ChangeOilSlide = MaintenanceFragment.CameraSlide
private typealias ChangeOilVec3 = Vec3

private fun f(frame: Int): Float = frame / 24f

val ChangeOilGuide = MaintenanceGuide(
    name = "Engine Oil Change",
    glbFile = "vios_engine-tutor-Wanimation_ChangeOil.glb",
    slides = listOf(
        ChangeOilSlide(
            title = "Overview",
            description = "Prepare tools and safety gear.",
            eye = ChangeOilVec3(-1.50f, 0.80f, -1.40f),
            lookAt = ChangeOilVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                GuideStep("Drain Pan", "Place a shallow pan directly under the oil plug area to catch the old oil."),
                GuideStep("Safety Gear", "Wear nitrile gloves. Used engine oil is a skin irritant and can be very hot.")
            ),
            infoTitle = "Oil Knowledge",
            infoItems = listOf(
                InfoItem("Viscosity", "Check your oil cap for the correct weight (e.g., 5W-30). Using the wrong weight can damage your engine."),
                InfoItem("Synthetic vs. Conventional", "Synthetic oil lasts longer and protects better in extreme temperatures, but is more expensive."),
                InfoItem("Disposal", "Never pour oil down the drain! Take it to a recycling center or local auto parts store.")
            )
        ),
        ChangeOilSlide(
            title = "Locate Drain Plug",
            description = "Find the oil drain plug on the engine pan.",
            eye = ChangeOilVec3(0.20f, -0.20f, -0.90f),
            lookAt = ChangeOilVec3(0.00f, -0.30f, 0.00f),
            steps = listOf(
                GuideStep("Identify Plug", "Look for a large bolt on the side or bottom of the lowest part of the engine."),
                GuideStep("Wrench Prep", "Ensure you have the exact size socket (usually 14mm or 17mm) to avoid stripping the bolt.")
            ),
            markerPos = ChangeOilVec3(0.15f, -0.15f, -0.70f),
            animationStartTime = f(5),
            animationTime = f(45),
            infoTitle = "Expert Tip",
            infoItems = listOf(
                InfoItem("Crush Washer", "Most cars use a aluminum or copper washer on the plug. Always use a new one to prevent slow leaks."),
                InfoItem("Oil Warmth", "Change your oil after a short drive. Warm oil flows out much faster and carries more sludge with it.")
            )
        )
    )
)
