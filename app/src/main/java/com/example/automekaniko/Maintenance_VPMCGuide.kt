package com.example.automekaniko

private typealias MaintenanceSlide = MaintenanceFragment.CameraSlide
private typealias MaintenanceVec3 = Vec3

private fun f(frame: Int): Float = frame / 24f

val VPMCGuide = MaintenanceGuide(
    name = "Vehicle Preventive Maintenance Checklist (VPMC)",
    glbFile = "VPMC(Vehicle Preventive Maintainance Checklist).glb",
    slides = listOf(
        MaintenanceSlide(
            title = "Vehicle Overview",
            description = "Start with a physical walkaround.",
            eye = MaintenanceVec3(-1.57f, 0.77f, -1.34f),
            lookAt = MaintenanceVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                GuideStep("Visual Check", "Walk around the car and look for any fresh puddles of oil or coolant on the ground."),
                GuideStep("Damage Scan", "Check all glass, mirrors, and body panels for new cracks, dents, or loose trim.")
            ),
            infoTitle = "Preventive Care",
            infoItems = listOf(
                InfoItem("Why VPMC?", "Regular checks catch small problems (like a slow leak) before they turn into expensive breakdowns."),
                InfoItem("Tire Health", "While walking around, look at your tires. If they look 'squat', check the pressure immediately.")
            )
        ),
        MaintenanceSlide(
            title = "Battery Check",
            description = "Inspect the battery terminals.",
            eye = MaintenanceVec3(-0.15f, 0.37f, -0.74f),
            lookAt = MaintenanceVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                GuideStep("Check Corrosion", "Look for white or blue crusty deposits. These block electricity and can stop your car from starting."),
                GuideStep("Cable Tightness", "Gently wiggle the cables. They should be rock-solid on the posts.")
            ),
            markerPos = MaintenanceVec3(0.12f, 0.35f, -0.65f),
            animationStartTime = f(70),
            animationTime = f(120),
            infoTitle = "Electrical Health",
            infoItems = listOf(
                InfoItem("Terminal Cleaning", "A mix of baking soda and water is the best way to safely neutralize battery acid corrosion."),
                InfoItem("Voltage", "A healthy battery should read 12.6V when the engine is off and about 14V when running.")
            )
        )
    )
)
