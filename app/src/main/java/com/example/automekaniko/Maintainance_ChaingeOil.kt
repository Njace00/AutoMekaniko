package com.example.automekaniko

private typealias ChangeOilSlide = MAINTAINANCEActivity.CameraSlide
private typealias ChangeOilVec3 = MAINTAINANCEActivity.Vec3

// CHANGE OIL - Engine Oil Maintenance

private fun f(frame: Int): Float = frame / 24f

val ChangeOilGuide = MaintenanceGuide(
    name = "Engine Oil Change",
    glbFile = "vios_engine-tutor-Wanimation_ChangeOil.glb",
    requiredTools = listOf("Oil Wrench", "14mm Socket & Ratchet", "Oil Drain Pan", "Funnel", "New Filter & Fresh Oil"),
    estimatedTime = "30–45 mins",
    difficulty = "Moderate",
    prerequisites = listOf(
        "Park on level surface and engage handbrake",
        "Warm up engine for 2-3 mins so oil drains freely",
        "Wear nitrile gloves and safety glasses"
    ),
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
                ChecklistStep("Park on a level surface", "Ensures accurate oil level reading and vehicle stability."),
                ChecklistStep("Turn off the engine", "Let it cool for at least 15 minutes to avoid burns from hot oil."),
                ChecklistStep("Prepare oil drain pan and tools", "You will need a socket wrench (usually 14mm), filter wrench, and gloves."),
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
                ChecklistStep("Lift the vehicle safely if necessary", "Use jack stands if you are going fully underneath; never trust a jack alone."),
                ChecklistStep("Place oil drain pan underneath", "Position it slightly forward of the plug to catch the initial splash."),
                ChecklistStep("Locate the drain plug on the oil pan", "It is the single bolt on the lowest part of the engine block.")
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
                ChecklistStep("Loosen the drain plug carefully", "Use the wrench to break it loose, then unscrew by hand."),
                ChecklistStep("Allow old oil to fully drain", "Wait until the steady stream turns into slow drips."),
                ChecklistStep("Inspect the drain plug and washer", "The crush washer should ideally be replaced every time.")
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
                ChecklistStep("Remove the old oil filter", "Expect some oil to leak out when you loosen it."),
                ChecklistStep("Apply fresh oil to new filter gasket", "Smear a bit of new oil on the rubber ring to ensure a good seal."),
                ChecklistStep("Install the new oil filter securely", "Tighten by hand only; do not use a tool to tighten it.")
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
                ChecklistStep("Install new drain plug washer if needed", "Ensures a leak-free seal on the oil pan."),
                ChecklistStep("Thread the drain plug carefully", "Always start by hand to avoid stripping the threads."),
                ChecklistStep("Tighten to proper torque specification", "Snug it up firmly with the wrench (approx 30-40 Nm).")
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
                ChecklistStep("Open the oil filler cap", "Located on top of the engine valve cover."),
                ChecklistStep("Pour the correct amount of oil", "Check your manual; usually between 3.3L to 4.0L for a Vios."),
                ChecklistStep("Check oil level using dipstick", "Pull it out, wipe, reinsert, and check it's at the 'Full' mark.")
            ),
            infoTitle = "Types of Oil & Recommended",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("What to Use", "Always use the viscosity grade (e.g., 5W-30) specified in your owner's manual."),
                MAINTAINANCEActivity.InfoItem("What NOT to Use", "Do not use oil additives unless recommended by the manufacturer. Avoid mixing different oil types."),
                MAINTAINANCEActivity.InfoItem("Conventional Oil", "Standard motor oil made from refined crude oil. Provides basic protection.", R.drawable.conventionaloil),
                MAINTAINANCEActivity.InfoItem("Full Synthetic Oil", "Chemically engineered for higher performance and superior protection.", R.drawable.fullysynthetic),
                MAINTAINANCEActivity.InfoItem("High Mileage Oil", "Designed specifically for vehicles with over 75,000 miles.", R.drawable.highmilleage)
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
                ChecklistStep("Start the engine", "Let it run for a minute to circulate the new oil through the filter."),
                ChecklistStep("Inspect for oil leaks", "Check around the drain plug and the new filter for any drips."),
                ChecklistStep("Recheck oil level after a few minutes", "Oil level drops slightly after the filter fills up."),
                ChecklistStep("Dispose of old oil properly", "Bring it to a local auto parts store or recycling center.")
            )
        )
    )
)
