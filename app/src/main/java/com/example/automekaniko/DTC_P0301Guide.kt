package com.example.automekaniko

private fun f(frame: Int): Float = frame / 24f

val P0301Guide = DtcGuide(
    code        = "P0301",
    name        = "Cylinder 1 Misfire Detected",
    description = "A misfire in cylinder 1 means the fuel-air mixture is not igniting correctly. " +
            "This can cause engine shaking, power loss, and poor fuel economy.",
    parts       = listOf("Spark plug", "Ignition coil", "Fuel injector", "Coil boot"),
    glbFile     = "DTC P0301 Misfire (SparkPlug Change).glb",
    animationClipStartTimes = listOf(f(0)),
    slides      = listOf(
        DtcSlide(
            title              = "Overview",
            description        = "Full view of the engine before any parts are removed.",
            eye                = Vec3(-1.57f, 0.77f, -1.34f),
            lookAt             = Vec3(0.00f,  0.10f,  0.00f),
            steps              = listOf(
                GuideStep("Cool Down", "Ensure the engine is cool to the touch (wait at least 30 mins) before starting work to avoid burns."),
                GuideStep("Safety Gear", "Wear protective gloves and eye protection. Ignition systems carry high voltage.")
            ),
            infoTitle = "Misfire Basics",
            infoItems = listOf(
                InfoItem("What is P0301?", "The '1' in P0301 identifies the specific cylinder. On inline 4-cylinder engines, cylinder 1 is usually closest to the drive belts."),
                InfoItem("Common Causes", "Faulty spark plugs are the #1 cause. Other issues include weak ignition coils or clogged fuel injectors."),
                InfoItem("Diagnostic Tip", "Swap the coil from cylinder 1 to cylinder 2. If the code changes to P0302, your coil is definitely bad.")
            )
        ),
        DtcSlide(
            title              = "Remove Engine Top Cover",
            description        = "Remove the plastic top cover to access the ignition coils.",
            eye                = Vec3(0.04f, 0.39f, -0.84f),
            lookAt             = Vec3(0.10f, 0.20f,  0.00f),
            steps              = listOf(
                GuideStep("Find Bolts", "Locate the 10mm bolts or plastic clips holding the engine cover in place."),
                GuideStep("Lift Carefully", "Lift the cover straight up. If it resists, check for hidden vacuum hoses or wiring attached to it.")
            ),
            markerPos = Vec3(0.10f, 0.45f, -0.50f),
            animationStartTime = f(1),
            animationTime      = f(130),
            infoTitle = "Tool Knowledge",
            infoItems = listOf(
                InfoItem("Wrench Size", "A standard 10mm socket or nut driver is the most common tool for engine covers."),
                InfoItem("Organization", "Place your bolts in a magnetic tray. These small bolts are easily lost in the engine bay.")
            )
        ),
        DtcSlide(
            title              = "Remove Ignition Coil",
            description        = "Disconnect and remove the coil to reach the spark plug.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            steps              = listOf(
                GuideStep("Unplug Connector", "Press the release tab on the wiring harness and pull it away from the coil."),
                GuideStep("Unbolt Coil", "Use an 8mm or 10mm socket to remove the single bolt holding the coil down."),
                GuideStep("Pull Coil", "Gently pull the ignition coil straight up. You may need to wiggle it slightly to break the seal.")
            ),
            markerPos = Vec3(0.15f, 0.35f, -0.55f),
            animationStartTime = f(140),
            animationTime      = f(160),
            infoTitle = "Coil Inspection",
            infoItems = listOf(
                InfoItem("Visual Check", "Look for cracks in the plastic body of the coil or signs of burning (carbon tracking)."),
                InfoItem("The Boot", "Inspect the rubber boot at the bottom. If it's torn or brittle, spark energy can leak to the engine block.")
            )
        ),
        DtcSlide(
            title              = "Replace Spark Plug",
            description        = "Remove the old plug and install a new one.",
            eye                = Vec3(0.08f,  0.47f, -0.64f),
            lookAt             = Vec3(0.10f, -0.30f,  0.00f),
            steps              = listOf(
                GuideStep("Loosen Plug", "Use a 5/8\" (16mm) spark plug socket with an extension to turn the plug counter-clockwise."),
                GuideStep("New Plug", "Thread the new plug in by hand first! Never use a wrench until it's seated to avoid cross-threading."),
                GuideStep("Torque", "Tighten to approximately 18-22 Nm. Do not overtighten, as you could damage the aluminum cylinder head.")
            ),
            markerPos = Vec3(0.15f, 0.25f, -0.55f),
            animationStartTime = f(170),
            animationTime      = f(220),
            infoTitle = "Expert Tip",
            infoItems = listOf(
                InfoItem("Gap Check", "Even 'pre-gapped' plugs should be checked. The standard gap is usually 1.1mm (0.044 inches)."),
                InfoItem("Dielectric Grease", "Apply a tiny dab of dielectric grease inside the coil boot. This prevents the rubber from sticking to the porcelain next time.")
            )
        )
    )
)
