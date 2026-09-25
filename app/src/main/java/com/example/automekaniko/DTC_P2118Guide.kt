package com.example.automekaniko

// P2118 — Throttle Actuator Control Motor Current Range / Performance

private fun f(frame: Int): Float = frame / 24f

val P2118Guide = DtcGuide(
    code        = "P2118",
    name        = "Throttle Actuator Control Motor Range",
    description = "This code indicates the electronic throttle body motor is not " +
            "operating correctly. Common causes include a failing throttle body, " +
            "carbon buildup, wiring issues, or low battery voltage.",
    parts       = listOf(
        "Throttle body assembly",
        "Throttle actuator motor",
        "Throttle position sensor",
        "Throttle body gasket",
        "Electrical connector"
    ),
    glbFile     = "DTC P2118 (Throttle Body).glb",
    animationClipStartTimes = listOf(f(0)),
    vehicleId   = VehicleManager.VIOS.id,
    requiredTools = listOf("10mm & 12mm Sockets", "Throttle Body Cleaner", "Soft Toothbrush", "New Throttle Gasket", "Digital Multimeter"),
    estimatedTime = "40–60 mins",
    difficulty  = "Moderate",
    prerequisites = listOf(
        "Disconnect battery negative terminal to reset ECM throttle learn values",
        "Never spray cleaner directly into the electronic motor connector",
        "Handle throttle plate gently without forcing metal internal gears"
    ),
    slides      = listOf(

        DtcSlide(
            title               = "Overview",
            description         = "Initial overview of the engine and throttle body location.",
            eye                 = Vec3(-2.20f, 1.20f, -2.40f),
            lookAt              = Vec3(0.00f, 0.20f, 0.00f),
            animationStartTime  = f(0),
            animationTime       = f(0),
            animationDurationMs = 700L,
            steps               = listOf(
                ChecklistStep(""),
                ChecklistStep(""),
                ChecklistStep("")
            ),
            infoTitle = "P2118 Throttle Actuator Diagnostics",

        ),

        DtcSlide(
            title               = "Overview 2",
            description         = "Initial overview of the engine and throttle body location.",
            eye                 = Vec3(-0.07f, 0.35f, -0.54f),
            lookAt              = Vec3(-0.02f, -1.05f, 0.01f),
            animationStartTime  = f(1),
            animationTime       = f(60),
            animationDurationMs = 700L,
            steps               = listOf(
                ChecklistStep("Open the hood safely", "Ensure the car is on level ground and the parking brake is set."),
                ChecklistStep("Locate the throttle body near the intake hose", "It's a large aluminum component connected to the air intake filter box."),
                ChecklistStep("Inspect for visible damage or loose connectors", "Check if the 6-pin connector is securely pushed in.")
            ),
            infoTitle = "P2118 Throttle Actuator Diagnostics",
            infoItems = listOf(
                MAINTAINANCEActivity.InfoItem("Failure Symptoms", "Vehicle enters Limp Home Mode (max 15 mph), pedal non-responsive, harsh idle surge, check engine & VSC lights on."),
                MAINTAINANCEActivity.InfoItem("Motor Resistance Test", "Measure resistance between actuator terminals 1 & 2. Spec is 0.3–100 Ω at 20°C. Open circuit (∞ Ω) indicates burned motor coils."),
                MAINTAINANCEActivity.InfoItem("ETCS Fuse Check", "Inspect 10A ETCS fuse in engine bay fuse box. A blown fuse cuts power to throttle motor."),
                MAINTAINANCEActivity.InfoItem("Throttle Relearn", "After cleaning or replacement, disconnect battery (-) for 10 mins, then idle engine for 15 mins with A/C off so ECM relearns throttle stops.")
            )
        ),

        DtcSlide(
            title               = "Remove Air Intake Hose",
            description         = "Disconnect the intake hose to access the throttle body.",
            eye                 = Vec3(-0.07f, 0.30f, -0.54f),
            lookAt              = Vec3(0.05f, -0.67f, -0.03f),
            animationStartTime  = f(70),
            animationTime       = f(130),
            animationDurationMs = 800L,
            steps               = listOf(
                ChecklistStep("Loosen the intake hose clamp", "Use a 10mm socket or a large flat-head screwdriver."),
                ChecklistStep("Disconnect vacuum lines if needed", "Pull them gently; if they are stuck, use a small pick to loosen the rubber."),
                ChecklistStep("Carefully pull the intake hose away", "Move it aside to clear a path to the throttle body entrance.")
            )
        ),



        DtcSlide(
            title               = "Remove Throttle Body",
            description         = "Unbolt and remove the throttle body assembly.",
            eye                 = Vec3(-0.07f, 0.30f, -0.54f),
            lookAt              = Vec3(0.05f, -0.67f, -0.03f),
            animationStartTime  = f(140),
            animationTime       = f(200),
            animationDurationMs = 900L,
            removeFirst         = "Electrical Harness & Coolant Bypass Hoses",
            teardownPath        = "Intake Hose ➔ Electrical Harness ➔ 4x 10mm Mounting Bolts ➔ Throttle Body",
            steps               = listOf(
                ChecklistStep("Remove the mounting bolts", "Usually four 10mm or 12mm bolts. Loosen them in a cross pattern."),
                ChecklistStep("Carefully pull out the throttle body", "Be aware that some coolant may leak if it has coolant bypass lines."),
                ChecklistStep("Remove the old gasket if necessary", "Scrape off any remaining rubber bits from the intake manifold.")
            )
        ),



        DtcSlide(
            title               = "Install New Throttle Body",
            description         = "Install the replacement throttle body assembly.",
            eye                 = Vec3(-0.07f, 0.30f, -0.54f),
            lookAt              = Vec3(0.05f, -0.67f, -0.03f),
            animationStartTime  = f(210),
            animationTime       = f(270),
            animationDurationMs = 1000L,
            steps               = listOf(
                ChecklistStep("Install new gasket", "Never reuse an old gasket; it will cause a vacuum leak and high idle."),
                ChecklistStep("Position the new throttle body", "Make sure the orientation matches the one you removed."),
                ChecklistStep("Tighten mounting bolts evenly", "Snug them all up first, then torque to around 10-15 Nm.")
            )
        ),

        DtcSlide(
            title               = "Reconnect Components",
            description         = "Reconnect the electrical connector and intake hose.",
            eye                 = Vec3(-0.07f, 0.30f, -0.54f),
            lookAt              = Vec3(0.05f, -0.67f, -0.03f),
            animationStartTime  = f(280),
            animationTime       = f(330),
            animationDurationMs = 800L,
            steps               = listOf(
                ChecklistStep("Reconnect the throttle connector", "Push it in until you hear a distinct click."),
                ChecklistStep("Reinstall intake hose", "Ensure it is seated fully over the throttle body mouth."),
                ChecklistStep("Secure all hose clamps properly", "A loose clamp after the MAF sensor causes lean codes.")
            )
        ),



    )
)
