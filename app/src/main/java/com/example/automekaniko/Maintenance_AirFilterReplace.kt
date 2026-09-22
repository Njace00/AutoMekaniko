package com.example.automekaniko

private typealias AirFilterSlide = MAINTAINANCEActivity.CameraSlide
private typealias AirFilterVec3 = MAINTAINANCEActivity.Vec3
private typealias AirFilterInfoItem = MAINTAINANCEActivity.InfoItem

private fun f(frame: Int): Float = frame / 24f

val AirFilterReplacementGuide = MaintenanceGuide(
    name = "Air Filter Replacement",
    glbFile = "Engine Air Filter Replacement.glb",
    requiredTools = listOf("10mm Socket / Flathead Screwdriver", "New Engine Air Filter", "Clean Shop Towel"),
    estimatedTime = "10–15 mins",
    difficulty = "Easy",
    prerequisites = listOf(
        "Turn off engine and remove keys from ignition",
        "Pop open the hood and secure with prop rod"
    ),
    slides = listOf(
        AirFilterSlide(
            title = "Vehicle Overview",
            description = "Before starting, take a look at the engine bay and locate the air intake system.",
            eye = AirFilterVec3(-1.57f, 0.77f, -1.34f),
            lookAt = AirFilterVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                ChecklistStep("Park on level ground and turn off the engine", "Prevents the vehicle from rolling and ensures safety during inspection."),
                ChecklistStep("Let the engine bay cool down", "The intake area can get hot; wait at least 15-20 minutes."),
                ChecklistStep("Locate the air intake hose and air filter box", "Look for a large black plastic box connected to a thick rubber hose.")
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
                ChecklistStep("Trace the intake hose from the throttle body", "Follow the thick hose back from the engine to the plastic box."),
                ChecklistStep("Find the air filter box near the fender", "It is usually located on the driver or passenger side corner of the engine bay."),
                ChecklistStep("Check surrounding clips and hose clamps", "Identify how the lid is secured—usually by 2 to 4 metal or plastic clips.")
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
                ChecklistStep("Unclip or unscrew the air box latches", "Use your hands for metal clips or a screwdriver for screw-type fasteners."),
                ChecklistStep("Disconnect any sensor wiring on the cover", "If there is a wire going into the box, press the tab and pull it out gently."),
                ChecklistStep("Lift the cover away from the housing", "You may need to pull it up and slightly sideways to clear the internal hinges."),
                ChecklistStep("Set the cover aside without straining the hose", "Rest it against a stable part of the engine bay if it's still attached to the hose.")
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
                ChecklistStep("Lift the old filter straight out of the housing", "Note which side is facing up so you install the new one correctly."),
                ChecklistStep("Check for dirt, debris, or discoloration", "Leaves, bugs, or gray/black soot mean the filter is clogged."),
                ChecklistStep("Hold it up to light to check airflow blockage", "If you can't see light through the pleats, it's time for a change."),
                ChecklistStep("Wipe out any dust left inside the housing", "Use a damp cloth to clean the bottom part of the box where the fresh air enters.")
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
                ChecklistStep("Check the new filter's orientation before placing it", "The rubber seal usually faces upward or fits into a specific groove."),
                ChecklistStep("Set the new filter squarely into the housing", "Make sure the corners match the shape of the box."),
                ChecklistStep("Ensure the rubber edge forms a complete seal", "There should be no gaps where unfiltered air could leak past."),
                ChecklistStep("Confirm it sits flush with no gaps", "Press down on the edges to seat the gasket firmly.")
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
                ChecklistStep("Place the cover back onto the housing", "Align the hinge tabs first before lowering the front of the lid."),
                ChecklistStep("Reconnect any sensor wiring", "Push the connector in until it clicks to ensure a solid connection."),
                ChecklistStep("Re-clip or re-screw the latches shut", "The clips should require a bit of force to snap over the lid."),
                ChecklistStep("Start the engine and check for unusual intake noise", "Listen for whistling or sucking sounds that might indicate a leak.")
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
