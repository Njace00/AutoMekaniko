package com.example.automekaniko

// P0301 — Cylinder 1 Misfire Detected
// Spark plug replacement guide for Vios engine.
//
// To tune camera angles: run the app in Camera FREE mode,
// move to the angle you want, then read the eye/lookAt values
// from tvCameraInfo and paste them into the slide below.

val P0301Guide = DtcGuide(
    code        = "P0301",
    name        = "Cylinder 1 Misfire Detected",
    description = "A misfire in cylinder 1 means the fuel-air mixture is not igniting " +
            "correctly. Common causes: faulty spark plug, ignition coil, or injector.",
    parts       = listOf("Spark plug", "Ignition coil", "Fuel injector", "Coil boot"),
    glbFile     = "vios engine-tutor(P0301)misfire.glb",
    slides      = listOf(

        DtcSlide(
            title       = "Overview",
            description = "Identify cylinder 1 on the engine bank.",
            eye    = Vec3(-1.57f, 0.77f, -1.34f),
            lookAt = Vec3(0.00f,  0.10f,  0.00f),
            steps  = listOf(
                "Open the hood",
                "Locate the engine bank",
                "Find cylinder 1 (front-most)"
            )
        ),

        DtcSlide(
            title       = "Remove Engine Top Cover",
            description = "Remove the plastic top cover to access the ignition coils.",
            eye    = Vec3(0.04f, 0.39f, -0.84f),
            lookAt = Vec3(0.10f, 0.20f,  0.00f),
            steps  = listOf(
                "Locate the plastic engine cover",
                "Pull upward firmly to unclip",
                "Set aside safely"
            )
        ),

        DtcSlide(
            title       = "Remove Ignition Coil",
            description = "Disconnect and remove the ignition coil for cylinder 1.",
            eye    = Vec3(0.04f,  0.33f, -0.7774f),
            lookAt = Vec3(0.10f,  0.20f,  0.00f),
            steps  = listOf(
                "Disconnect coil electrical connector",
                "Remove the coil bolt (10mm)",
                "Pull the coil straight up"
            )
        ),

        DtcSlide(
            title       = "Remove Spark Plug",
            description = "Use a spark plug socket to remove the old plug.",
            eye    = Vec3(0.08f,  0.47f, -0.64f),
            lookAt = Vec3(0.10f, -0.30f,  0.00f),
            steps  = listOf(
                "Attach spark plug socket to extension",
                "Turn counter-clockwise to loosen",
                "Remove plug carefully"
            )
        ),

        DtcSlide(
            title       = "Inspect and Replace",
            description = "Check gap and install the new spark plug.",
            eye    = Vec3(-0.03f, 0.50f, -0.77f),
            lookAt = Vec3(0.30f, -0.20f,  0.00f),
            steps  = listOf(
                "Check gap on new plug (0.8-1.0mm)",
                "Thread in new plug by hand",
                "Torque to spec (20-25 Nm)",
                "Reinstall coil and connector"
            )
        ),

        DtcSlide(
            title       = "Clear Code and Test",
            description = "Clear the DTC and verify the fix.",
            eye    = Vec3(-1.57f, 0.77f, -1.34f),
            lookAt = Vec3(0.00f,  0.10f,  0.00f),
            steps  = listOf(
                "Use OBD scanner to clear P0301",
                "Start engine and let it idle",
                "Check for misfire on live data",
                "Test drive and rescan"
            )
        )

    )
)