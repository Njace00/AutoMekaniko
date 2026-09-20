package com.example.automekaniko

private fun f(frame: Int): Float = frame / 24f

val P2118Guide = DtcGuide(
    code        = "P2118",
    name        = "Throttle Actuator Motor Performance",
    description = "This code means the electronic throttle motor is having current/performance issues, often putting the car in 'Limp Mode'.",
    parts       = listOf("Throttle body", "Actuator motor", "TPS sensor", "ETCS Fuse"),
    glbFile     = "vios_engine-tutor-Wanimation_P2118.glb",
    animationClipStartTimes = listOf(f(0)),
    slides      = listOf(
        DtcSlide(
            title               = "Overview",
            description         = "Locate the electronic throttle body assembly.",
            eye                 = Vec3(-1.40f, 0.75f, -1.30f),
            lookAt              = Vec3(0.00f, 0.15f, 0.00f),
            steps               = listOf(
                GuideStep("Safety First", "Ensure the ignition is OFF. Never touch the internal throttle plate while the key is ON."),
                GuideStep("Locate Part", "The throttle body is connected to the large rubber air intake hose.")
            ),
            infoTitle = "Drive-by-Wire",
            infoItems = listOf(
                InfoItem("What is P2118?", "Modern cars don't use a cable. A motor moves the throttle plate based on your pedal position."),
                InfoItem("Limp Mode", "If the computer detects a throttle fault, it limits engine speed to protect you from unintended acceleration."),
                InfoItem("Hidden Culprit", "Check the 'ETCS' fuse in the engine bay fuse box first. A blown fuse often triggers this code.")
            )
        ),
        DtcSlide(
            title               = "Remove Air Intake Hose",
            description         = "Clear the path to reach the throttle body.",
            eye                 = Vec3(0.15f, 0.42f, -0.90f),
            lookAt              = Vec3(0.20f, 0.18f, 0.00f),
            steps               = listOf(
                GuideStep("Loosen Clamps", "Use a screwdriver or 10mm socket to loosen the metal bands on both ends of the hose."),
                GuideStep("Remove Hose", "Gently pull the hose off the throttle body and the air filter box. Inspect for cracks.")
            ),
            markerPos = Vec3(0.35f, 0.40f, -0.75f),
            animationStartTime  = f(5),
            animationTime       = f(55),
            infoTitle = "Maintenance Tip",
            infoItems = listOf(
                InfoItem("Vacuum Leaks", "Cracks in this hose can allow 'unmetered air' into the engine, causing rough idle or stalling."),
                InfoItem("Cleaning", "While the hose is off, it's a great time to clean the throttle plate with a specialized cleaner.")
            )
        ),
        DtcSlide(
            title               = "Inspect Throttle Body",
            description         = "Check for carbon buildup or mechanical sticking.",
            eye                 = Vec3(0.12f, 0.35f, -0.72f),
            lookAt              = Vec3(0.18f, 0.20f, 0.00f),
            steps               = listOf(
                GuideStep("Check Wiring", "Unplug the electrical connector and check for green corrosion on the pins."),
                GuideStep("Plate Movement", "Gently push the plate with your finger. It should move smoothly and spring back.")
            ),
            markerPos = Vec3(0.15f, 0.35f, -0.65f),
            animationStartTime  = f(60),
            animationTime       = f(95),
            infoTitle = "Expert Cleaning",
            infoItems = listOf(
                InfoItem("Solvents", "Only use 'Throttle Body Cleaner'. Brake cleaner or carb cleaner can damage the special coating on the plate."),
                InfoItem("Gentle Touch", "Do not spray liquid directly into the motor housing. Spray a cloth and wipe the edges of the plate.")
            )
        )
    )
)
