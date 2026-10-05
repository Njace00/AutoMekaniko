package com.example.automekaniko

// P2118 — Throttle Actuator Control Motor Current Range/Performance

private fun f(frame: Int): Float = frame / 24f

val P2118Guide = DtcGuide(
    code        = "P2118",
    name        = "Throttle Control Motor Power Circuit",
    description = "P2118 indicates an electrical current or power supply issue with the " +
            "electronic throttle control actuator motor, forcing limp-mode.",
    parts       = listOf(
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
                ChecklistStep("Inspect engine bay ETCS fuse (10A)", "Locate engine bay fuse box and check 10A ETCS fuse element for blown wire."),
                ChecklistStep("Check battery voltage (12.4V+ required)", "Low battery voltage can trigger false ETCS motor codes.")
            )
        ),

        DtcSlide(
            title               = "Remove Air Intake Hose",
            description         = "Loosen hose clamps and disconnect air duct from throttle body inlet.",
            eye                 = Vec3(0.00f, 0.32f, -0.75f),
            lookAt              = Vec3(-0.08f, 0.12f, -0.05f),
            animationStartTime  = f(1),
            animationTime       = f(110),
            animationDurationMs = 1500L,
            targetPartName      = "Air Intake Hose",
            targetPartLocationNote = "Flexible rubber duct clamped to throttle body",
            steps               = listOf(
                ChecklistStep("Loosen 10mm hose clamp on throttle body inlet", "Unscrew clamp until hose slides off smoothly."),
                ChecklistStep("Disconnect vacuum hoses attached to intake duct", "Mark hose locations if necessary."),
                ChecklistStep("Pull intake hose off throttle body spout", "Inspect interior hose walls for oil contamination or tears.")
            )
        ),

        DtcSlide(
            title               = "Disconnect Harness & Unbolt Throttle Body",
            description         = "Unclip 6-pin ETCS connector and remove 4 retaining bolts.",
            eye                 = Vec3(0.00f, 0.28f, -0.65f),
            lookAt              = Vec3(-0.08f, 0.10f, -0.05f),
            animationStartTime  = f(120),
            animationTime       = f(200),
            animationDurationMs = 1500L,
            targetPartName      = "Throttle Body Assembly",
            targetPartLocationNote = "Secured with four 10mm bolts to intake manifold",
            steps               = listOf(
                ChecklistStep("Press release tab on 6-pin ETCS connector and disconnect", "Inspect pins for green corrosion or bent terminals."),
                ChecklistStep("Remove 4 mounting bolts (10mm) in cross-pattern", "Keep bolts organized."),
                ChecklistStep("Carefully detach throttle body from intake manifold", "Discard old paper/rubber gasket.")
            )
        ),

        DtcSlide(
            title               = "Clean Carbon Deposits & Inspect Motor",
            description         = "Spray throttle cleaner onto cloth and wipe bore and throttle valve edges.",
            eye                 = Vec3(0.02f, 0.25f, -0.58f),
            lookAt              = Vec3(-0.05f, 0.05f, -0.05f),
            animationStartTime  = f(210),
            animationTime       = f(290),
            animationDurationMs = 1500L,
            targetPartName      = "Throttle Valve Plate",
            targetPartLocationNote = "Clean carbon buildup around valve perimeter",
            steps               = listOf(
                ChecklistStep("Spray aerosol cleaner on microfiber rag", "Do NOT spray directly onto electronic motor housing!"),
                ChecklistStep("Wipe dark carbon ridge around valve perimeter", "Ensure plate moves smoothly when gently pushed."),
                ChecklistStep("Measure motor coil resistance with multimeter (0.3–10 Ω)", "If resistance is infinite or zero, replace throttle assembly.")
            )
        ),

        DtcSlide(
            title               = "Reinstall with New Gasket & Calibrate",
            description         = "Install new gasket, torque bolts to 10 Nm, and perform idle relearn.",
            eye                 = Vec3(0.00f, 0.32f, -0.75f),
            lookAt              = Vec3(-0.08f, 0.12f, -0.05f),
            animationStartTime  = f(300),
            animationTime       = f(340),
            animationDurationMs = 1500L,
            targetPartName      = "New Throttle Gasket",
            targetPartLocationNote = "Torqued to 10 Nm (7 ft-lb)",
            steps               = listOf(
                ChecklistStep("Place new gasket on intake manifold mating surface", "Ensure proper orientation."),
                ChecklistStep("Torque 4 mounting bolts in cross-pattern to 10 Nm (7 ft-lb)", "Avoid overtightening into plastic intake manifold."),
                ChecklistStep("Reconnect battery, turn ignition ON for 10s without starting", "Allows ECM to perform initial throttle zero-position calibration.")
            )
        )
    )
)
