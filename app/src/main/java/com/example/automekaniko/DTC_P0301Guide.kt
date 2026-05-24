package com.example.automekaniko

// P0301 — Cylinder 1 Misfire Detected
// GLB: vios_engine-tutor-Wanimation_P0301.glb
// Total animation duration: 6.25s across 6 tracks (all scrubbed together)
//
// HOW TO TUNE PER-SLIDE ANIMATION:
//   Each slide has two time values:
//     animationStartTime — the animation instantly jumps HERE when you enter the slide
//     animationTime      — then scrubs smoothly TO HERE and freezes
//     animationDurationMs — how long the scrub takes in milliseconds
//
//   Use f(frame) to convert frame numbers → seconds (assumes 24 fps).
//   Example: frame 40 = f(40) = 40 / 24f = 1.667s
//
//   To find the right values:
//     1. Run the app, go to this guide
//     2. Tap through slides and watch the animation
//     3. Adjust animationStartTime / animationTime / animationDurationMs, rebuild, repeat

private fun f(frame: Int): Float = frame / 24f

val P0301Guide = DtcGuide(
    code        = "P0301",
    name        = "Cylinder 1 Misfire Detected",
    description = "A misfire in cylinder 1 means the fuel-air mixture is not igniting " +
            "correctly. Common causes: faulty spark plug, ignition coil, or injector.",
    parts       = listOf("Spark plug", "Ignition coil", "Fuel injector", "Coil boot"),
    glbFile     = "vios engine-tutor-Wanimation_P0301.glb",
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
                "Ensure the engine is cool and the ignition is off",
                "Open the hood and visually inspect the engine bay",
                "Locate cylinder 1 — the first cylinder nearest the front of the engine"
            )
        ),


        DtcSlide(
            title              = "Remove Engine Top Cover",
            description        = "Remove the plastic top cover to access the ignition coils.",
            eye                = Vec3(0.04f, 0.39f, -0.84f),
            lookAt             = Vec3(0.10f, 0.20f,  0.00f),
            animationStartTime = f(1),
            animationTime      = f(50),
            animationDurationMs = 650L,
            steps              = listOf(
                "Locate the plastic engine top cover",
                "Unclip or unscrew the cover retaining bolts",
                "Lift and set the cover aside in a safe place"
            )
        ),


        DtcSlide(
            title              = "Remove Ignition Coil",
            description        = "Disconnect and remove the ignition coil for cylinder 1.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(70),
            animationTime      = f(90),
            animationDurationMs = 650L,
            steps              = listOf(
                "Locate the ignition coil on cylinder 1",
                "Press the tab and disconnect the electrical connector",
                "Remove the coil retaining bolt using an 8mm socket",
                "Pull the coil straight up and out of the spark plug well"
            )
        ),


        DtcSlide(
            title              = "Remove Spark Plug",
            description        = "Use a spark plug socket to remove the old plug.",
            eye                = Vec3(0.08f,  0.47f, -0.64f),
            lookAt             = Vec3(0.10f, -0.30f,  0.00f),
            animationStartTime = f(100),
            animationTime      = f(130),
            animationDurationMs = 650L,
            steps              = listOf(
                "Attach a spark plug socket (16mm) to an extension bar",
                "Insert it into the spark plug well and turn counter-clockwise",
                "Carefully remove the spark plug and set it aside for inspection"
            )
        ),


        DtcSlide(
            title              = "Inspect",
            description        = "Examine the old spark plug for wear, fouling, or damage.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(130),
            animationTime      = f(200),
            animationDurationMs = 650L,
            steps              = listOf(
                "Check the electrode for excessive wear or erosion",
                "Look for black carbon deposits (rich mixture) or white residue (lean/overheating)",
                "Check the gap using a feeler gauge — correct gap is typically 1.0–1.1mm",
                "Replace the plug if worn, fouled, or gap is out of spec"
            )
        ),


        DtcSlide(
            title              = "Replace with the new one",
            description        = "Install the new spark plug with the correct torque.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(200),
            animationTime      = f(250),
            animationDurationMs = 1500L,
            steps              = listOf(
                "Thread the new spark plug in by hand to avoid cross-threading",
                "Tighten with a spark plug socket to 18–25 Nm (do not overtighten)",
                "Verify the plug is seated flush and secure",
                "Apply a small amount of dielectric grease inside the coil boot (optional)"
            )
        ),


        DtcSlide(
            title              = "Put back the Ignition Coil",
            description        = "Reinstall the ignition coil onto cylinder 1.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(250),
            animationTime      = f(295),
            animationDurationMs = 650L,
            steps              = listOf(
                "Lower the ignition coil back into the spark plug well",
                "Press it firmly until it seats onto the plug",
                "Reinstall and tighten the retaining bolt",
                "Reconnect the electrical connector until it clicks"
            )
        ),


        DtcSlide(
            title              = "Assemble the Engine Top Cover Again",
            description        = "Reinstall the engine cover and verify the repair.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(300),
            animationTime      = f(318),
            animationDurationMs = 650L,
            steps              = listOf(
                "Place the engine top cover back into position",
                "Clip or bolt it down securely",
                "Start the engine and listen for smooth idle",
                "Use an OBD scanner to clear the P0301 code and confirm no reoccurrence"
            )
        )

    )
)