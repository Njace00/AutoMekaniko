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
    glbFile     = "vios_engine-tutor-Wanimation_P2118.glb",
    animationClipStartTimes = listOf(f(0)),
    slides      = listOf(

        DtcSlide(
            title               = "Overview",
            description         = "Initial overview of the engine and throttle body location.",
            eye                 = Vec3(-1.40f, 0.75f, -1.30f),
            lookAt              = Vec3(0.00f, 0.15f, 0.00f),
            animationStartTime  = f(0),
            animationTime       = f(0),
            animationDurationMs = 700L,
            steps               = listOf(
                ChecklistStep("Open the hood safely", "Ensure the car is on level ground and the parking brake is set."),
                ChecklistStep("Locate the throttle body near the intake hose", "It's a large aluminum component connected to the air intake filter box."),
                ChecklistStep("Inspect for visible damage or loose connectors", "Check if the 6-pin connector is securely pushed in.")
            )
        ),

        DtcSlide(
            title               = "Remove Air Intake Hose",
            description         = "Disconnect the intake hose to access the throttle body.",
            eye                 = Vec3(0.15f, 0.42f, -0.90f),
            lookAt              = Vec3(0.20f, 0.18f, 0.00f),
            animationStartTime  = f(5),
            animationTime       = f(55),
            animationDurationMs = 800L,
            steps               = listOf(
                ChecklistStep("Loosen the intake hose clamp", "Use a 10mm socket or a large flat-head screwdriver."),
                ChecklistStep("Disconnect vacuum lines if needed", "Pull them gently; if they are stuck, use a small pick to loosen the rubber."),
                ChecklistStep("Carefully pull the intake hose away", "Move it aside to clear a path to the throttle body entrance.")
            )
        ),

        DtcSlide(
            title               = "Disconnect Throttle Body Connector",
            description         = "Disconnect the electronic connector from the throttle body.",
            eye                 = Vec3(0.12f, 0.35f, -0.72f),
            lookAt              = Vec3(0.18f, 0.20f, 0.00f),
            animationStartTime  = f(60),
            animationTime       = f(95),
            animationDurationMs = 700L,
            steps               = listOf(
                ChecklistStep("Press the connector lock tab", "Push the tab down until you feel a click before pulling."),
                ChecklistStep("Gently disconnect the connector", "Do not pull by the wires! Pull only by the plastic housing."),
                ChecklistStep("Inspect terminals for corrosion or damage", "Look for green crust or bent pins inside the connector.")
            )
        ),

        DtcSlide(
            title               = "Remove Throttle Body",
            description         = "Unbolt and remove the throttle body assembly.",
            eye                 = Vec3(0.08f, 0.38f, -0.60f),
            lookAt              = Vec3(0.12f, 0.15f, 0.00f),
            animationStartTime  = f(100),
            animationTime       = f(150),
            animationDurationMs = 900L,
            steps               = listOf(
                ChecklistStep("Remove the mounting bolts", "Usually four 10mm or 12mm bolts. Loosen them in a cross pattern."),
                ChecklistStep("Carefully pull out the throttle body", "Be aware that some coolant may leak if it has coolant bypass lines."),
                ChecklistStep("Remove the old gasket if necessary", "Scrape off any remaining rubber bits from the intake manifold.")
            )
        ),

        DtcSlide(
            title               = "Inspect and Clean",
            description         = "Inspect the throttle plate and clean carbon buildup.",
            eye                 = Vec3(0.05f, 0.45f, -0.55f),
            lookAt              = Vec3(0.10f, 0.10f, 0.00f),
            animationStartTime  = f(155),
            animationTime       = f(210),
            animationDurationMs = 1200L,
            steps               = listOf(
                ChecklistStep("Inspect throttle plate movement", "It should be spring-loaded and not feel gritty when moved by hand."),
                ChecklistStep("Clean carbon deposits carefully", "Use a dedicated throttle body cleaner and a soft toothbrush."),
                ChecklistStep("Check for sticking or motor failure", "If it's stuck open or shut, the internal motor gears are likely stripped."),
                ChecklistStep("Replace throttle body if defective", "Cleaning doesn't fix a dead motor; if P2118 persists, replace it.")
            )
        ),

        DtcSlide(
            title               = "Install New Throttle Body",
            description         = "Install the replacement throttle body assembly.",
            eye                 = Vec3(0.08f, 0.38f, -0.60f),
            lookAt              = Vec3(0.12f, 0.15f, 0.00f),
            animationStartTime  = f(215),
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
            eye                 = Vec3(0.12f, 0.35f, -0.72f),
            lookAt              = Vec3(0.18f, 0.20f, 0.00f),
            animationStartTime  = f(275),
            animationTime       = f(320),
            animationDurationMs = 800L,
            steps               = listOf(
                ChecklistStep("Reconnect the throttle connector", "Push it in until you hear a distinct click."),
                ChecklistStep("Reinstall intake hose", "Ensure it is seated fully over the throttle body mouth."),
                ChecklistStep("Secure all hose clamps properly", "A loose clamp after the MAF sensor causes lean codes.")
            )
        ),

        DtcSlide(
            title               = "Throttle Relearn and Verification",
            description         = "Perform idle relearn and verify the repair.",
            eye                 = Vec3(-1.20f, 0.70f, -1.10f),
            lookAt              = Vec3(0.00f, 0.15f, 0.00f),
            animationStartTime  = f(325),
            animationTime       = f(360),
            animationDurationMs = 1500L,
            steps               = listOf(
                ChecklistStep("Reconnect battery if disconnected", "Ensure terminals are clean and tight."),
                ChecklistStep("Start the engine and let it idle", "Do not touch the gas pedal for at least 5 minutes."),
                ChecklistStep("Clear the DTC using an OBD2 scanner", "Delete the code and wait for the dashboard light to go out."),
                ChecklistStep("Verify that P2118 does not return", "Take a short test drive and check for normal throttle response.")
            )
        )

    )
)
