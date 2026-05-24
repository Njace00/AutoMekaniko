package com.example.automekaniko

// P2118 — Throttle Actuator Control Motor Current Range / Performance
// GLB: vios_engine-tutor-Wanimation_P2118.glb
// Total animation duration: 7.50s across multiple tracks
//
// HOW TO TUNE PER-SLIDE ANIMATION:
//   Each slide has two time values:
//     animationStartTime — the animation instantly jumps HERE when you enter the slide
//     animationTime      — then scrubs smoothly TO HERE and freezes
//     animationDurationMs — how long the scrub takes in milliseconds
//
//   Use f(frame) to convert frame numbers → seconds (assumes 24 fps).
//   Example: frame 48 = f(48) = 48 / 24f = 2.0s

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
                "Open the hood safely",
                "Locate the throttle body near the intake hose",
                "Inspect for visible damage or loose connectors"
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
                "Loosen the intake hose clamp",
                "Disconnect vacuum lines if needed",
                "Carefully pull the intake hose away"
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
                "Press the connector lock tab",
                "Gently disconnect the connector",
                "Inspect terminals for corrosion or damage"
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
                "Remove the mounting bolts",
                "Carefully pull out the throttle body",
                "Remove the old gasket if necessary"
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
                "Inspect throttle plate movement",
                "Clean carbon deposits carefully",
                "Check for sticking or motor failure",
                "Replace throttle body if defective"
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
                "Install new gasket",
                "Position the new throttle body",
                "Tighten mounting bolts evenly"
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
                "Reconnect the throttle connector",
                "Reinstall intake hose",
                "Secure all hose clamps properly"
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
                "Reconnect battery if disconnected",
                "Start the engine and let it idle",
                "Clear the DTC using an OBD2 scanner",
                "Verify that P2118 does not return"
            )
        )

    )
)