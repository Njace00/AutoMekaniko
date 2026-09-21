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
    slides      = listOf(


        DtcSlide(
            title              = "Overview",
            description        = "Full view of the engine before any parts are removed.",
            eye                = Vec3(-1.57f, 0.77f, -1.34f),
            lookAt             = Vec3(0.00f,  0.10f,  0.00f),
            animationStartTime = f(0),
            animationTime      = f(0),
            animationDurationMs = 650L,
            steps              = listOf(
                ChecklistStep("Ensure the engine is cool and the ignition is off", "Working on a hot engine can cause severe burns. Wait at least 30 minutes."),
                ChecklistStep("Open the hood and visually inspect the engine bay", "Look for loose wires, signs of rodents, or obvious fluid leaks."),
                ChecklistStep("Locate cylinder 1 — the first cylinder nearest the front of the engine", "Cylinder 1 is almost always the one closest to the drive belt/pulley side.")
            ),
            infoTitle = "Cylinder 1 Misfire",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("What to Use", "Use a high-quality OBD2 scanner and specialized spark plug tools."),
                MAINTAINANCEActivity.InfoItem("What NOT to Use", "Do not handle ignition components with wet hands or while the engine is hot."),
                MAINTAINANCEActivity.InfoItem("Diagnostic", "Check for physical damage to the ignition coil and wiring harness."),
                MAINTAINANCEActivity.InfoItem("Safety", "Always disconnect the battery before working on electrical components.")
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
            steps              = listOf(
                ChecklistStep("Locate the ignition coil on cylinder 1", "It's the component connected to the electrical wire going into the engine head."),
                ChecklistStep("Press the tab and disconnect the electrical connector", "Be gentle; these plastic tabs can become brittle and break easily."),
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
            animationDurationMs = 650L,
            steps              = listOf(
                ChecklistStep("Attach a spark plug socket (16mm) to an extension bar", "The socket should have a rubber insert or magnet to hold the plug."),
                ChecklistStep("Insert it into the spark plug well and turn counter-clockwise", "If it's very tight, use a bit of penetrating oil and wait."),
                ChecklistStep("Carefully remove the spark plug and set it aside for inspection", "Check the tip for soot, oil, or melted electrodes.")
            )
        ),




        DtcSlide(
            title              = "Replace with the new one",
            description        = "Install the new spark plug with the correct torque.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(240),
            animationTime      = f(300),
            animationDurationMs = 1500L,
            steps              = listOf(
                ChecklistStep("Thread the new spark plug in by hand to avoid cross-threading", "You should be able to turn it several times without any tools."),
                ChecklistStep("Tighten with a spark plug socket to 18–25 Nm (do not overtighten)", "Overtightening can strip the threads in the aluminum engine head."),
                ChecklistStep("Verify the plug is seated flush and secure", "It should feel solid and not wiggle at all."),
                ChecklistStep("Apply a small amount of dielectric grease inside the coil boot (optional)", "This helps prevent moisture entry and makes future removal easier.")
            )
        ),


        DtcSlide(
            title              = "Put back the Ignition Coil",
            description        = "Reinstall the ignition coil onto cylinder 1.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(310),
            animationTime      = f(360),
            animationDurationMs = 650L,
            steps              = listOf(
                ChecklistStep("Lower the ignition coil back into the spark plug well", "Align it with the hole and the bolt mounting point."),
                ChecklistStep("Press it firmly until it seats onto the plug", "You should feel a slight 'click' as the boot engages the plug top."),
                ChecklistStep("Reinstall and tighten the retaining bolt", "Just snug it up; no need to overtighten an 8mm bolt."),
                ChecklistStep("Reconnect the electrical connector until it clicks", "The click ensures the locking tab is engaged.")
            )
        ),


        DtcSlide(
            title              = "Assemble the Engine Top Cover Again",
            description        = "Reinstall the engine cover and verify the repair.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(370),
            animationTime      = f(430),
            animationDurationMs = 650L,
            steps              = listOf(
                ChecklistStep("Place the engine top cover back into position", "Ensure it's oriented correctly (logo facing up)."),
                ChecklistStep("Clip or bolt it down securely", "Make sure no wires are pinched under the cover."),
                ChecklistStep("Start the engine and listen for smooth idle", "The misfire (shaking) should be gone now."),
                ChecklistStep("Use an OBD scanner to clear the P0301 code and confirm no reoccurrence", "Clear the code and drive for 10 minutes to see if the check engine light returns.")
            ),
            infoTitle = "P0301 Final Check",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("Tip", "If the code returns, try swapping the coil from cylinder 1 to cylinder 2 to see if the code changes to P0302."),
                MAINTAINANCEActivity.InfoItem("Note", "Always use the exact spark plug model recommended by your car manufacturer.")
            )
        )

    )
)
