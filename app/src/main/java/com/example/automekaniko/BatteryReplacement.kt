package com.example.automekaniko

private typealias ReplaceBatterySlide = MAINTAINANCEActivity.CameraSlide
private typealias ReplaceBatteryVec3 = MAINTAINANCEActivity.Vec3
private typealias ReplaceBatteryInfoItem = MAINTAINANCEActivity.InfoItem

private fun f(frame: Int): Float = frame / 24f

val BatteryReplacementGuide = MaintenanceGuide(
    name = "Battery Replacement",
    glbFile = "BatteryReplacement.glb",
    slides = listOf(
        ReplaceBatterySlide(
            title = "Car Overview",
            description = "Briefly describe what this step involves.",
            eye = ReplaceBatteryVec3(-1.50f, 0.80f, -1.40f),
            lookAt = ReplaceBatteryVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                ChecklistStep("First specific action", "More info here"),
                ChecklistStep("Second specific action", "More info here"),
            ),
            animationStartTime = f(1),
            animationTime = f(1),
            animationDurationMs = 700L,
        ),
        ReplaceBatterySlide(
            title = "Open The Hood",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(0.00f, 0.50f, -1.00f),
            lookAt = ReplaceBatteryVec3(0.00f, 0.20f, 0.00f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(1),
            animationTime = f(60),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),

        ReplaceBatterySlide(
            title = "Loosen the bolt for the terminal connector",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(70),
            animationTime = f(100),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),

        ReplaceBatterySlide(
            title = "Remove the Negative First",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(120),
            animationTime = f(180),
            animationDurationMs = 180L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),

        ReplaceBatterySlide(
            title = "Loosen the Bolt on of the Positive Terminal",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(190),
            animationTime = f(220),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),

        ReplaceBatterySlide(
            title = "Then Remove the Positive Terminals",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(230),
            animationTime = f(290),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),

        ReplaceBatterySlide(
            title = "Remove the Old Battery",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(300),
            animationTime = f(350),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),


        ReplaceBatterySlide(
            title = "Replace the Battery with the new One",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(360),
            animationTime = f(490),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),

        ReplaceBatterySlide(
            title = "Re-Attach the Positive Terminal",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(500),
            animationTime = f(560),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),

        ReplaceBatterySlide(
            title = "Re-Attach the Negative Terminal",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(570),
            animationTime = f(630),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),

        ReplaceBatterySlide(
            title = "Tighten Both Terminal's Bolt",
            description = "Describe where the part is located.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            steps = listOf(
                ChecklistStep("Locate the specific part", "More info here"),
                ChecklistStep("Prepare tools", "More info here"),
            ),
            animationStartTime = f(640),
            animationTime = f(670),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("What to Use", "Recommended tool or fluid."),
                ReplaceBatteryInfoItem("What NOT to Use", "Common mistake to avoid.")
            )
        ),

    )
)
