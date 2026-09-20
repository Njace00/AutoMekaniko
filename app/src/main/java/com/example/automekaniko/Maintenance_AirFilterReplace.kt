package com.example.automekaniko

private typealias AirFilterSlide = MaintenanceFragment.CameraSlide
private typealias AirFilterVec3 = Vec3

private fun f(frame: Int): Float = frame / 24f

val AirFilterReplacementGuide = MaintenanceGuide(
    name = "Air Filter Replacement",
    glbFile = "Engine Air Filter Replacement.glb",
    slides = listOf(
        AirFilterSlide(
            title = "Vehicle Overview",
            description = "Identify the air intake system.",
            eye = AirFilterVec3(-1.57f, 0.77f, -1.34f),
            lookAt = AirFilterVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                GuideStep("Cool Down", "Ensure the engine is off and cool. The air box is often near hot radiator hoses."),
                GuideStep("Locate Box", "Find the large, black plastic box on the side of the engine bay.")
            ),
            infoTitle = "Engine Breathing",
            infoItems = listOf(
                InfoItem("Why Replace?", "A dirty filter chokes your engine, reducing horsepower and gas mileage."),
                InfoItem("Interval", "Most filters should be replaced every 15,000 to 30,000 km, or more often in dusty areas.")
            )
        ),
        AirFilterSlide(
            title = "Open Air Box",
            description = "Release the clips to access the filter.",
            eye = AirFilterVec3(-0.08f, 0.42f, -0.74f),
            lookAt = AirFilterVec3(-0.26f, -0.42f, 0.24f),
            steps = listOf(
                GuideStep("Unsnap Clips", "Usually, there are 2 to 4 metal or plastic snap-clips. Pop them open with your fingers."),
                GuideStep("Lift Lid", "Lift the top of the box just enough to see the filter. Be careful not to strain the sensor wires.")
            ),
            markerPos = AirFilterVec3(-0.05f, 0.35f, -0.65f),
            animationStartTime = f(70),
            animationTime = f(125),
            infoTitle = "Pro Tip",
            infoItems = listOf(
                InfoItem("MAF Sensor", "If your box has a wire plugged into it, that's the Mass Air Flow sensor. It's very delicate—don't touch the inside!"),
                InfoItem("Housing Clean", "Wipe out any leaves or sand from the bottom of the box before putting the new filter in.")
            )
        )
    )
)
