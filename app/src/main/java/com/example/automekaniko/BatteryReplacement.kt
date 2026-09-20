package com.example.automekaniko

private typealias ReplaceBatterySlide = MaintenanceFragment.CameraSlide
private typealias ReplaceBatteryVec3 = Vec3

private fun f(frame: Int): Float = frame / 24f

val BatteryReplacementGuide = MaintenanceGuide(
    name = "Battery Replacement",
    glbFile = "BatteryReplacement.glb",
    slides = listOf(
        ReplaceBatterySlide(
            title = "Car Overview",
            description = "Start with a cool engine and safety gear.",
            eye = ReplaceBatteryVec3(-1.50f, 0.80f, -1.40f),
            lookAt = ReplaceBatteryVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                GuideStep("Safety First", "Wear protective gloves and eye protection. Batteries contain sulfuric acid and can emit explosive gases."),
                GuideStep("Engine Off", "Ensure the ignition is off and the key is removed. This prevents electrical spikes during disconnection.")
            ),
            animationStartTime = f(1),
            animationTime = f(1),
            infoTitle = "Battery 101",
            infoItems = listOf(
                InfoItem("What is CCA?", "Cold Cranking Amps measures a battery's ability to start an engine in cold temperatures. Higher is usually better."),
                InfoItem("Battery Life", "Most car batteries last 3 to 5 years. Heat is actually more damaging to batteries than cold."),
                InfoItem("Recycling", "Lead-acid batteries are 99% recyclable. Always return your old battery to a parts store for proper disposal.")
            )
        ),
        ReplaceBatterySlide(
            title = "Open The Hood",
            description = "Locate the battery in the engine bay.",
            eye = ReplaceBatteryVec3(0.00f, 0.50f, -1.00f),
            lookAt = ReplaceBatteryVec3(0.00f, 0.20f, 0.00f),
            steps = listOf(
                GuideStep("Pull Release", "Pull the hood release lever inside the cabin (usually located under the dashboard on the driver's side)."),
                GuideStep("Secure Hood", "Lift the hood and secure it with the prop rod. Ensure it's stable before leaning into the engine bay.")
            ),
            markerPos = ReplaceBatteryVec3(0.12f, 0.35f, -0.65f),
            animationStartTime = f(1),
            animationTime = f(60),
            infoTitle = "Pro Tip",
            infoItems = listOf(
                InfoItem("Battery Location", "Most batteries are in the engine bay, but some cars hide them in the trunk or under the rear seat!"),
                InfoItem("Terminal ID", "The Positive (+) terminal is usually red or has a red plastic cover. The Negative (-) is black.")
            )
        ),
        ReplaceBatterySlide(
            title = "Remove Negative Terminal",
            description = "Always disconnect the negative (-) cable first.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                GuideStep("Loosen Nut", "Use a 10mm wrench to loosen the nut on the negative (black/minus) terminal. You don't need to remove the nut completely."),
                GuideStep("Lift Cable", "Wiggle the cable off the terminal. Tuck it away so it cannot accidentally touch any metal part of the car.")
            ),
            markerPos = ReplaceBatteryVec3(0.20f, 0.34f, -0.55f),
            animationStartTime = f(120),
            animationTime = f(180),
            infoTitle = "Safety Knowledge",
            infoItems = listOf(
                InfoItem("Why Negative First?", "If your wrench touches the car's body while loosening the negative, nothing happens. If you did the positive first, it would create a massive spark!"),
                InfoItem("Memory Saver", "Some modern cars may lose radio presets or clock settings when the battery is disconnected.")
            )
        ),
        ReplaceBatterySlide(
            title = "Remove Positive Terminal",
            description = "Now disconnect the positive (+) cable.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                GuideStep("Flip Cover", "If present, flip back the red plastic protective cover from the positive terminal."),
                GuideStep("Remove Cable", "Loosen the nut and remove the positive (red/plus) cable from the battery. Avoid letting it touch the negative post.")
            ),
            markerPos = ReplaceBatteryVec3(0.05f, 0.34f, -0.55f),
            animationStartTime = f(230),
            animationTime = f(290),
            infoTitle = "Clean Terminals",
            infoItems = listOf(
                InfoItem("Corrosion", "If you see white/blue powder, clean it with a mix of baking soda and water or a wire battery brush."),
                InfoItem("Terminal Health", "A clean connection ensures the alternator can properly charge the battery while you drive.")
            )
        ),
        ReplaceBatterySlide(
            title = "Replace Battery",
            description = "Swap the old battery with a new one.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                GuideStep("Lift Out", "Unbolt the battery hold-down bracket. Carefully lift the heavy battery out of the tray and set it aside."),
                GuideStep("Place New", "Place the new battery into the tray. Ensure the terminals are facing the same way as the old ones.")
            ),
            markerPos = ReplaceBatteryVec3(0.12f, 0.25f, -0.55f),
            animationStartTime = f(360),
            animationTime = f(490),
            infoTitle = "Expert Tip",
            infoItems = listOf(
                InfoItem("Hold-Down", "Never drive without the hold-down bracket secure. Vibration can damage the internal plates of the battery."),
                InfoItem("Protectors", "Apply a spray-on terminal protector or a thin layer of petroleum jelly to the posts to prevent future corrosion.")
            )
        )
    )
)
