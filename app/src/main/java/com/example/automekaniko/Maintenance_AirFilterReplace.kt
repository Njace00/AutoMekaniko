package com.example.automekaniko

private typealias AirFilterSlide = MAINTAINANCEActivity.CameraSlide
private typealias AirFilterVec3 = MAINTAINANCEActivity.Vec3
private typealias AirFilterInfoItem = MAINTAINANCEActivity.InfoItem

private fun f(frame: Int): Float = frame / 24f

val AirFilterReplacementGuide = MaintenanceGuide(
    name = "AirFilter Replacement",
    glbFile = "Engine Air Filter Replacement.glb",
    slides = listOf(
        AirFilterSlide(
            title = "Vehicle Overview",
            description = "Before starting, take a look at the engine bay and locate the air intake system.",
            eye = AirFilterVec3(-1.57f, 0.77f, -1.34f),
            lookAt = AirFilterVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                "Park on level ground and turn off the engine",
                "Let the engine bay cool down",
                "Locate the air intake hose and air filter box"
            ),
            animationStartTime = f(1),
            animationTime = f(1),
            animationDurationMs = 650L
        ),
        AirFilterSlide(
            title = "Locate the Air Filter Box",
            description = "Identify the air filter housing and the ducting that connects it to the engine.",
            eye = AirFilterVec3(-0.01f, 0.55f, -1.65f),
            lookAt = AirFilterVec3(-0.01f, 0.10f, 0.00f),
            steps = listOf(
                "Trace the intake hose from the throttle body",
                "Find the air filter box near the fender",
                "Check surrounding clips and hose clamps"
            ),
            animationStartTime = f(1),
            animationTime = f(60),
            animationDurationMs = 650L
        ),
        AirFilterSlide(
            title = "Open the Air Filter Box",
            description = "Release the retaining clips and lift the cover to access the filter element.",
            eye = AirFilterVec3(-0.080f, 0.42f, -0.74f),
            lookAt = AirFilterVec3(-0.26f, -0.42f, 0.24f),
            steps = listOf(
                "Unclip or unscrew the air box latches",
                "Disconnect any sensor wiring on the cover",
                "Lift the cover away from the housing",
                "Set the cover aside without straining the hose"
            ),
            animationStartTime = f(70),
            animationTime = f(125),
            animationDurationMs = 650L,
            infoTitle = "Air Filter Box Basics",
            infoItems = listOf(
                AirFilterInfoItem("What to Use", "Use a clean rag to wipe dust from the housing before opening it fully."),
                AirFilterInfoItem("What NOT to Use", "Never force a latch open; a stuck clip usually just needs to be pressed at a different angle."),
                AirFilterInfoItem("Sensors", "Some housings have a mass airflow sensor connector — unplug it gently before removing the cover."),
                AirFilterInfoItem("Housing Condition", "Check the housing for cracks or a torn gasket while it's open.")
            )
        ),
        AirFilterSlide(
            title = "Remove the Old Air Filter",
            description = "Take out the old filter element and inspect it before installing the replacement.",
            eye = AirFilterVec3(-0.080f, 0.42f, -0.74f),
            lookAt = AirFilterVec3(-0.26f, -0.42f, 0.24f),
            steps = listOf(
                "Lift the old filter straight out of the housing",
                "Check for dirt, debris, or discoloration",
                "Hold it up to light to check airflow blockage",
                "Wipe out any dust left inside the housing"
            ),
            animationStartTime = f(140),
            animationTime = f(310),
            animationDurationMs = 1500L,
            infoTitle = "Inspecting the Old Filter",
            infoItems = listOf(
                AirFilterInfoItem("What to Use", "Use a shop vacuum or damp cloth to clean loose dust from the housing interior."),
                AirFilterInfoItem("What NOT to Use", "Do not use compressed air to blow debris further into the intake tract."),
                AirFilterInfoItem("Visual Check", "A filter that looks dark, clogged, or lets little light through should be replaced."),
                AirFilterInfoItem("Replacement Interval", "Most manufacturers recommend replacing the air filter every 12,000–15,000 km.")
            )
        ),
        AirFilterSlide(
            title = "Install the New Air Filter",
            description = "Seat the new filter into the housing, making sure it fits snugly and sealed.",
            eye = AirFilterVec3(-0.080f, 0.42f, -0.74f),
            lookAt = AirFilterVec3(-0.26f, -0.42f, 0.24f),
            steps = listOf(
                "Check the new filter's orientation before placing it",
                "Set the new filter squarely into the housing",
                "Ensure the rubber edge forms a complete seal",
                "Confirm it sits flush with no gaps"
            ),
            animationStartTime = f(315),
            animationTime = f(360),
            animationDurationMs = 650L,
            infoTitle = "Choosing an Air Filter",
            infoItems = listOf(
                AirFilterInfoItem("What to Use", "Use the OEM-specified filter size and type for the vehicle model."),
                AirFilterInfoItem("What NOT to Use", "Avoid ill-fitting aftermarket filters that leave gaps around the seal."),
                AirFilterInfoItem("Paper Filter", "Standard disposable paper element. Affordable and effective for most driving.", R.drawable.conventionaloil),
                AirFilterInfoItem("Foam/Oiled Filter", "Reusable and washable, often used in performance setups.", R.drawable.fullysynthetic),
                AirFilterInfoItem("High-Flow Filter", "Designed for increased airflow, may need more frequent cleaning.", R.drawable.highmilleage)
            )
        ),
        AirFilterSlide(
            title = "Close the Box and Verify",
            description = "Reinstall the cover, reconnect any wiring, and confirm everything is secure.",
            eye = AirFilterVec3(-0.080f, 0.42f, -0.74f),
            lookAt = AirFilterVec3(-0.26f, -0.42f, 0.24f),
            steps = listOf(
                "Place the cover back onto the housing",
                "Reconnect any sensor wiring",
                "Re-clip or re-screw the latches shut",
                "Start the engine and check for unusual intake noise"
            ),
            animationStartTime = f(360),
            animationTime = f(380),
            animationDurationMs = 650L,
            infoTitle = "Final Checks",
            infoItems = listOf(
                AirFilterInfoItem("What to Use", "Double-check every clip is fully latched to keep the housing airtight."),
                AirFilterInfoItem("What NOT to Use", "Never run the engine with the air box cover off; unfiltered debris can damage the engine."),
                AirFilterInfoItem("Seal Check", "A properly seated cover should show no gaps around its edge."),
                AirFilterInfoItem("Sound Check", "A hissing or whistling sound after startup may mean the housing isn't sealed properly.")
            )
        ),

        )
)