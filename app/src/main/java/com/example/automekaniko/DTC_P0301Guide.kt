package com.example.automekaniko

// P0301 — Cylinder 1 Misfire Detected
// GLB: vios_engine-tutor-Wanimation_P0301.glb
// Total animation duration: 6.25s across 6 tracks (all scrubbed together)
//
// HOW TO TUNE PER-SLIDE ANIMATION:
//   Each slide has two time values:
//     animationStartTime — the animation instantly jumps HERE when you enter the slide
//     animationTime      — then scrubs smoothly TO HERE and freezes
//
//   Use f(frame) to convert frame numbers → seconds (assumes 24 fps).
//   Example: frame 40 = f(40) = 40 / 24f = 1.667s
//
//   To find the right values:
//     1. Run the app, go to this guide
//     2. Tap through slides and watch the animation
//     3. Adjust animationStartTime / animationTime, rebuild, repeat

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
            steps              = listOf(
                "test",
                "test",
                "test"
            )
        ),


        DtcSlide(
            title              = "Remove Engine Top Cover",
            description        = "Remove the plastic top cover to access the ignition coils.",
            eye                = Vec3(0.04f, 0.39f, -0.84f),
            lookAt             = Vec3(0.10f, 0.20f,  0.00f),
            animationStartTime = f(1),
            animationTime      = f(50),
            steps              = listOf(
                "test",
                "test",
                "test"
            )
        ),


        DtcSlide(
            title              = "Remove Ignition Coil",
            description        = "Disconnect and remove the ignition coil for cylinder 1.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(70),
            animationTime      = f(90),
            steps              = listOf(
                "test",
                "test",
                "test"
            )
        ),


        DtcSlide(
            title              = "Remove Spark Plug",
            description        = "Use a spark plug socket to remove the old plug.",
            eye                = Vec3(0.08f,  0.47f, -0.64f),
            lookAt             = Vec3(0.10f, -0.30f,  0.00f),
            animationStartTime = f(100),
            animationTime      = f(130),
            steps              = listOf(
                "test",
                "test",
                "test"
            )
        ),


        DtcSlide(
            title              = "Inspect",
            description        = "Check gap and install the new spark plug.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(130),
            animationTime      = f(200),
            steps              = listOf(
                "test",
                "test",
                "test",
                "test"
            )
        ),


        DtcSlide(
            title              = "Replace with the new one",
            description        = "Clear the DTC and verify the fix.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(200),
            animationTime      = f(250),
            steps              = listOf(
                "test",
                "test",
                "test",
                "test"
            )
        ),


        DtcSlide(
            title              = "Put back the Ignition Coil",
            description        = "Clear the DTC and verify the fix.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(250),
            animationTime      = f(295),
            steps              = listOf(
                "test",
                "test",
                "test",
                "test"
            )
        ),


        DtcSlide(
            title              = "Assemble the Engine Top Cover Again",
            description        = "Clear the DTC and verify the fix.",
            eye                = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt             = Vec3(0.10f,  0.20f,  0.00f),
            animationStartTime = f(300),
            animationTime      = f(318),
            steps              = listOf(
                "test",
                "test",
                "test",
                "test"
            )
        )

    )
)
