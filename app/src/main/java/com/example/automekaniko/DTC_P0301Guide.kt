package com.example.automekaniko

// P0301 — Cylinder 1 Misfire Detected

private fun f(frame: Int): Float = frame / 24f

val P0301Guide = DtcGuide(
    code        = "P0301",
    name        = "Cylinder 1 Misfire Detected",
    description = "A misfire in cylinder 1 means the fuel-air mixture is not igniting " +
            "correctly. Common causes: faulty spark plug, ignition coil, or injector.",
    parts       = listOf("Spark plug", "Ignition coil", "Fuel injector", "Coil boot"),
    glbFile     = "DTC P0301 Misfire (SparkPlug Change).glb",
    animationClipStartTimes = listOf(f(0)),
    vehicleId   = VehicleManager.VIOS.id,
    requiredTools = listOf("OBD2 Scanner", "10mm Socket", "5/8 Spark Plug Socket & Extension", "Torque Wrench", "Dielectric Grease"),
    estimatedTime = "30–40 mins",
    difficulty  = "Moderate",
    prerequisites = listOf(
        "Allow engine to cool down to ambient temperature to prevent thread damage",
        "Disconnect negative battery cable before touching ignition harness",
        "Keep area around spark plug wells free of dirt and debris"
    ),
    slides      = listOf(


        DtcSlide(
            title              = "Overview",
            description        = "Full view of the engine before any parts are removed.",
            eye                = Vec3(-2.20f, 1.20f, -2.40f),
            lookAt             = Vec3(0.00f,  0.20f,  0.00f),
            animationStartTime = f(0),
            animationTime      = f(0),
            animationDurationMs = 650L,
            steps              = listOf(
                ChecklistStep("Ensure the engine is cool and the ignition is off", "Working on a hot engine can cause severe burns. Wait at least 30 minutes."),
                ChecklistStep("Open the hood and visually inspect the engine bay", "Look for loose wires, signs of rodents, or obvious fluid leaks."),
                ChecklistStep("Locate cylinder 1 — the first cylinder nearest the front of the engine", "Cylinder 1 is almost always the one closest to the drive belt/pulley side.")
            ),
            infoTitle = "P0301 Diagnostic Encyclopedia",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("Symptoms", "Flashing Check Engine Light, engine vibration at idle, hesitation during acceleration, smell of unburnt fuel from exhaust."),
                MAINTAINANCEActivity.InfoItem("Coil Swap Test", "Swap Coil #1 with Coil #2. Clear DTC and test drive. If code changes to P0302, Coil #1 is bad! If it stays P0301, check spark plug/injector."),
                MAINTAINANCEActivity.InfoItem("Multimeter Spec", "Coil Primary Resistance: 0.5–1.2 Ω across terminals 1 & 2. Secondary: 10k–16k Ω. Replace if out of range."),
                MAINTAINANCEActivity.InfoItem("Plug Gap & Torque", "Set gap to 1.1mm (0.044 in) with wire gauge. Torque plug to 18–25 Nm (13–18 lb-ft) into aluminum cylinder head."),
                MAINTAINANCEActivity.InfoItem("Safety Warning", "Never remove spark plugs from a warm aluminum head to prevent thread stripping.")
            )
        ),


        DtcSlide(
            title              = "Remove Engine Top Cover",
            description        = "Remove the plastic top cover to access the ignition coils.",
            eye                = Vec3(0.04f, 0.39f, -0.84f),
            lookAt             = Vec3(0.10f, 0.20f,  0.00f),
            animationStartTime = f(1),
            animationTime      = f(130),
            animationDurationMs = 1500L,
            targetPartName     = "Engine Beauty Cover",
            targetPartLocationNote = "Black plastic shroud directly over the cylinder head",
            steps              = listOf(
                ChecklistStep("Locate the plastic engine top cover", "This is the large plastic shroud on top of the actual engine."),
                ChecklistStep("Unclip or unscrew the cover retaining bolts", "Usually 10mm bolts or simple push-clips."),
                ChecklistStep("Lift and set the cover aside in a safe place", "Avoid placing it on the ground where it can be stepped on.")
            ),
            infoTitle = "Engine Cover Removal",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("What to Use", "Use a 10mm socket or screwdriver depending on your model."),
                MAINTAINANCEActivity.InfoItem("What NOT to Use", "Do not force the cover; if it's stuck, check for hidden bolts or clips."),
                MAINTAINANCEActivity.InfoItem("Tip", "Place all bolts in a magnetic tray so you don't lose them.")
            )
        ),


        DtcSlide(
            title              = "Remove Ignition Coil",
            description        = "Disconnect and remove the ignition coil for cylinder 1.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(140),
            animationTime      = f(160),
            animationDurationMs = 650L,
            targetPartName     = "Ignition Coil #1",
            targetPartLocationNote = "Front-most coil pack secured with 8mm bolt",
            steps              = listOf(
                ChecklistStep("Locate the ignition coil on cylinder 1", "It's the component connected to the electrical wire going into the engine head."),
                ChecklistStep("Press the tab and disconnect the electrical connector", "Be gentle; these plastic tabs can become brittle and break easily.", warning = "Pull only by the plastic housing, never pull directly on the wires!"),
                ChecklistStep("Remove the coil retaining bolt using an 8mm socket", "Keep the bolt with the coil so it doesn't get mixed up."),
                ChecklistStep("Pull the coil straight up and out of the spark plug well", "You might feel some resistance from the rubber boot; just pull steadily.")
            )
        ),


        DtcSlide(
            title              = "Remove Spark Plug",
            description        = "Use a spark plug socket to remove the old plug.",
            eye                = Vec3(0.08f,  0.47f, -0.64f),
            lookAt             = Vec3(0.10f, -0.30f,  0.00f),
            animationStartTime = f(170),
            animationTime      = f(220),
            animationDurationMs = 1500L,
            targetPartName     = "Spark Plug #1",
            targetPartLocationNote = "Deep inside cylinder #1 well",
            steps              = listOf(
                ChecklistStep("Attach 5/8 spark plug socket to extension bar", "Ensure socket has rubber insert to hold the plug."),
                ChecklistStep("Lower socket into well and loosen counter-clockwise", "Turn steadily until threads disengage completely."),
                ChecklistStep("Lift spark plug out carefully", "Inspect electrode for black soot, oil, or worn gap.")
            )
        ),


        DtcSlide(
            title              = "Install New Spark Plug & Torque",
            description        = "Hand-thread new plug and torque to factory specs.",
            eye                = Vec3(0.08f,  0.47f, -0.64f),
            lookAt             = Vec3(0.10f, -0.30f,  0.00f),
            animationStartTime = f(230),
            animationTime      = f(270),
            animationDurationMs = 1500L,
            targetPartName     = "New Spark Plug #1",
            targetPartLocationNote = "Torqued to 18–25 Nm (13–18 lb-ft)",
            steps              = listOf(
                ChecklistStep("Check new plug gap with feeler gauge (1.1mm)", "Adjust electrode gently if out of spec."),
                ChecklistStep("Hand-thread new spark plug clockwise", "Hand-threading prevents cross-threading aluminum cylinder head threads."),
                ChecklistStep("Torque with torque wrench to 18–25 Nm (13–18 lb-ft)", "Do not over-tighten!")
            )
        ),


        DtcSlide(
            title              = "Reinstall Ignition Coil & Reconnect",
            description        = "Reinsert coil, tighten 8mm bolt, and reconnect harness.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(280),
            animationTime      = f(310),
            animationDurationMs = 1500L,
            targetPartName     = "Ignition Coil #1",
            targetPartLocationNote = "Reinstalled & harness clipped",
            steps              = listOf(
                ChecklistStep("Apply small dab of dielectric grease inside coil boot", "Prevents moisture ingress and flashover."),
                ChecklistStep("Push coil firmly onto spark plug tip", "Ensure it seats fully into the well."),
                ChecklistStep("Tighten 8mm retaining bolt and click harness connector", "Listen for audible click from plastic harness latch.")
            )
        )
    )
)
