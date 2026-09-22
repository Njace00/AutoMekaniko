package com.example.automekaniko

private typealias ReplaceBatterySlide = MAINTAINANCEActivity.CameraSlide
private typealias ReplaceBatteryVec3 = MAINTAINANCEActivity.Vec3
private typealias ReplaceBatteryInfoItem = MAINTAINANCEActivity.InfoItem

private fun f(frame: Int): Float = frame / 24f

val BatteryReplacementGuide = MaintenanceGuide(
    name = "Battery Replacement",
    glbFile = "BatteryReplacement.glb",
    requiredTools = listOf("10mm & 12mm Wrench / Sockets", "Wire Terminal Brush", "Safety Goggles & Gloves", "New 12V Battery"),
    estimatedTime = "20–25 mins",
    difficulty = "Moderate",
    prerequisites = listOf(
        "Ensure ignition and all electronics are OFF",
        "Always disconnect NEGATIVE (-) terminal first",
        "Do NOT allow metal tools to bridge positive terminal to frame"
    ),
    slides = listOf(
        ReplaceBatterySlide(
            title = "Car Overview",
            description = "Inspect engine compartment and locate the 12V battery tray.",
            eye = ReplaceBatteryVec3(-1.50f, 0.80f, -1.40f),
            lookAt = ReplaceBatteryVec3(0.00f, 0.10f, 0.00f),
            targetPartName = "Engine Bay & 12V Battery",
            targetPartLocationNote = "Located in the front engine compartment on the driver's side tray",
            steps = listOf(
                ChecklistStep("Check vehicle positioning", "Park on flat, level ground and engage emergency handbrake."),
                ChecklistStep("Turn off engine & lights", "Remove keys from ignition to prevent electrical spikes or battery sparks."),
                ChecklistStep("Ensure safety gear", "Wear nitrile gloves and safety goggles to protect against corrosive battery acid.")
            ),
            animationStartTime = f(1),
            animationTime = f(1),
            animationDurationMs = 700L,
            infoTitle = "12V Battery Basics",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Battery Purpose", "Powers starter motor, ECM electronics, headlights, and cabin accessories."),
                ReplaceBatteryInfoItem("Average Lifespan", "3 to 5 years depending on climate, heat exposure, and driving habits.")
            )
        ),

        ReplaceBatterySlide(
            title = "Open The Hood",
            description = "Release the interior latch and secure the hood prop rod.",
            eye = ReplaceBatteryVec3(0.00f, 0.50f, -1.00f),
            lookAt = ReplaceBatteryVec3(0.00f, 0.20f, 0.00f),
            targetPartName = "Interior Hood Release Lever",
            targetPartLocationNote = "Located under dashboard on driver's side footwell",
            steps = listOf(
                ChecklistStep("Pull interior hood release lever", "Located under the left side of the dashboard near footwell."),
                ChecklistStep("Release safety latch under hood center", "Reach under front edge of hood, press lever right/up."),
                ChecklistStep("Secure hood with prop rod", "Insert prop rod firmly into designated arrow hole.")
            ),
            animationStartTime = f(1),
            animationTime = f(60),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Prop Rod Safety", "Ensure the prop rod is seated in its slot so hood cannot fall while working.")
            )
        ),

        ReplaceBatterySlide(
            title = "Loosen Terminal Connector Bolts",
            description = "Prepare terminal clamps for safe disconnection.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            targetPartName = "Battery Terminal Fasteners",
            targetPartLocationNote = "10mm clamping bolts on negative (-) and positive (+) battery posts",
            steps = listOf(
                ChecklistStep("Inspect terminal clamps for corrosion", "Look for white/blue powder or rust around terminal bolts."),
                ChecklistStep("Select 10mm wrench or socket", "Ensure wrench fits snug to prevent rounding the bolt heads."),
                ChecklistStep("Spray penetrating oil if corroded", "If bolts are seized, apply WD-40 or PB Blaster and wait 2 minutes.")
            ),
            animationStartTime = f(70),
            animationTime = f(100),
            animationDurationMs = 900L,
            infoTitle = "Terminal Care",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Corrosion Cause", "White powder is lead sulfate / copper corrosion caused by acid vapor venting.")
            )
        ),

        ReplaceBatterySlide(
            title = "Remove the Negative First",
            description = "Always disconnect the negative (-) terminal first to prevent short circuits.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            targetPartName = "Negative (-) Battery Terminal",
            targetPartLocationNote = "Black cable clamp connected to negative (-) battery post",
            removeFirst = "Hood Prop & Safety Glasses",
            teardownPath = "Hood Latch ➔ Neg. Terminal (-) ➔ Pos. Terminal (+) ➔ Hold-down Bracket",
            steps = listOf(
                ChecklistStep("Loosen 10mm nut on negative clamp", "Turn counter-clockwise 3–4 full turns until clamp expands.", warning = "Always disconnect Negative (-) FIRST! Touching frame metal while loosening positive first creates a dangerous short circuit!"),
                ChecklistStep("Wiggle clamp free from negative post", "Twist gently side-to-side while pulling upward."),
                ChecklistStep("Isolate negative cable away from battery", "Tuck cable behind battery tray so it cannot accidentally touch post.")
            ),
            animationStartTime = f(120),
            animationTime = f(180),
            animationDurationMs = 180L,
            infoTitle = "Electrical Safety",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Why Negative First?", "Disconnecting negative opens the ground path to the car body, eliminating spark risk if tools touch metal chassis.")
            )
        ),

        ReplaceBatterySlide(
            title = "Loosen Positive Terminal Bolt",
            description = "Prepare positive (+) terminal clamp for removal.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            targetPartName = "Positive (+) Terminal Bolt",
            targetPartLocationNote = "Red covered clamp on positive (+) battery post",
            removeFirst = "Negative (-) Terminal Cable",
            teardownPath = "Negative (-) Cable ➔ Positive (+) Terminal Bolt ➔ Hold-Down Bracket",
            steps = listOf(
                ChecklistStep("Flip back red plastic protective cover", "Exposes the 10mm positive clamp nut."),
                ChecklistStep("Loosen 10mm positive terminal nut", "Turn counter-clockwise until clamp expands freely.")
            ),
            animationStartTime = f(190),
            animationTime = f(220),
            animationDurationMs = 900L,
            infoTitle = "Terminal Identification",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Positive (+) Markings", "Positive post is slightly larger in diameter than negative post and stamped with '+' or RED cover.")
            )
        ),

        ReplaceBatterySlide(
            title = "Remove Positive Terminal",
            description = "Disconnect and isolate positive (+) battery clamp.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            targetPartName = "Positive (+) Battery Terminal",
            targetPartLocationNote = "Red cable clamp connected to positive (+) post",
            removeFirst = "Negative (-) Terminal Cable",
            teardownPath = "Negative (-) ➔ Positive (+) ➔ Hold-down Bracket ➔ Old Battery",
            steps = listOf(
                ChecklistStep("Wiggle and lift positive clamp off post", "Pull straight up away from positive terminal.", warning = "Avoid touching positive clamp to any metal engine components!"),
                ChecklistStep("Cover positive clamp with red shield", "Prevents any stray voltage or static discharge.")
            ),
            animationStartTime = f(230),
            animationTime = f(290),
            animationDurationMs = 900L,
            infoTitle = "Pro Tip",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Isolate Clamps", "Wrap clean rags over cable ends if battery replacement takes more than a few minutes.")
            )
        ),

        ReplaceBatterySlide(
            title = "Remove the Hold-Down Bracket",
            description = "Unbolt metal bracket securing battery to tray.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            targetPartName = "Battery Hold-Down Bracket",
            targetPartLocationNote = "Metal bar across top or base of battery held by 10mm J-hooks",
            removeFirst = "Both Terminal Clamps",
            teardownPath = "Terminal Clamps ➔ 10mm J-Hook Nuts ➔ Metal Crossbar",
            steps = listOf(
                ChecklistStep("Unbolt two 10mm J-hook nuts", "Loosen nuts until J-hooks can unlatch from tray bottom."),
                ChecklistStep("Remove metal crossbar and J-hooks", "Lift bracket away and set aside with bolts.")
            ),
            animationStartTime = f(300),
            animationTime = f(350),
            animationDurationMs = 900L,
            infoTitle = "Bracket Safety",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Hold-Down Importance", "A loose battery vibrates, damaging internal lead plates and shorting cell walls.")
            )
        ),

        ReplaceBatterySlide(
            title = "Remove the Old Battery",
            description = "Lift the heavy old battery out of the engine tray.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            targetPartName = "Old 12V Battery",
            targetPartLocationNote = "Driver side battery tray in engine bay",
            removeFirst = "Hold-Down Bracket & Clamps",
            teardownPath = "Terminal Clamps ➔ Hold-Down Bracket ➔ Lift Battery Out",
            steps = listOf(
                ChecklistStep("Use carrying handle or grip base firmly", "Batteries weigh 30–45 lbs (14–20 kg); keep back straight.", warning = "Keep battery upright at all times! Tilting old lead-acid batteries can spill sulfuric acid!"),
                ChecklistStep("Lift battery straight out of tray", "Set battery safely on ground away from vehicle."),
                ChecklistStep("Clean battery tray and corrosion", "Use wire brush and baking soda solution to neutralize white acid crust.")
            ),
            animationStartTime = f(360),
            animationTime = f(490),
            animationDurationMs = 900L,
            infoTitle = "Recycling",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Battery Core Return", "Old car batteries are 99% recyclable! Return old battery to auto shop for core deposit refund.")
            )
        ),

        ReplaceBatterySlide(
            title = "Install New Battery",
            description = "Place fresh battery into tray and secure hold-down bracket.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            targetPartName = "New 12V Battery",
            targetPartLocationNote = "Positioned in battery tray matching original terminal orientation",
            removeFirst = "Old Battery & Corroded Debris",
            teardownPath = "Clean Tray ➔ New Battery ➔ Hold-Down Bracket ➔ Positive (+) ➔ Negative (-)",
            steps = listOf(
                ChecklistStep("Verify terminal orientation matches old battery", "Ensure Positive (+) and Negative (-) terminals face correct cables."),
                ChecklistStep("Lower new battery into tray", "Ensure it sits flat and flush in plastic tray base."),
                ChecklistStep("Reinstall hold-down bracket & J-hooks", "Tighten 10mm nuts until battery is firmly locked without bulging case.")
            ),
            animationStartTime = f(500),
            animationTime = f(560),
            animationDurationMs = 900L,
            infoTitle = "Battery Specs",
            infoItems = listOf(
                ReplaceBatteryInfoItem("CCA & Group Size", "Match Cold Cranking Amps (CCA) and Group Size (e.g. Group 35, 51R, 24) to factory owner's manual.")
            )
        ),

        ReplaceBatterySlide(
            title = "Re-Attach Positive Terminal",
            description = "Connect and tighten positive (+) terminal cable FIRST.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            targetPartName = "Positive (+) Battery Terminal",
            targetPartLocationNote = "Red cable clamp on positive (+) battery post",
            removeFirst = "New Battery Secured in Tray",
            teardownPath = "Hold-Down Bracket ➔ Positive (+) Terminal ➔ Negative (-) Terminal",
            steps = listOf(
                ChecklistStep("Clean positive post with wire terminal brush", "Ensures 100% metal-to-metal electrical contact."),
                ChecklistStep("Push positive clamp all the way down post", "Clamp should sit flush with post base.", warning = "When reconnecting, ALWAYS attach POSITIVE (+) FIRST, then NEGATIVE (-) last!"),
                ChecklistStep("Tighten 10mm positive clamp nut", "Snug firmly (approx. 5–7 Nm) so clamp cannot twist by hand.")
            ),
            animationStartTime = f(570),
            animationTime = f(630),
            animationDurationMs = 900L,
            infoTitle = "Contact Quality",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Tight Clamp", "A loose positive terminal causes clicking starter solenoids, slow crank, or dead alternator charging.")
            )
        ),

        ReplaceBatterySlide(
            title = "Re-Attach Negative Terminal",
            description = "Connect negative (-) cable last to complete electrical circuit.",
            eye = ReplaceBatteryVec3(-0.12f, 0.34f, -0.56f),
            lookAt = ReplaceBatteryVec3(0.04f, -2.34f, 1.64f),
            targetPartName = "Negative (-) Battery Terminal",
            targetPartLocationNote = "Black cable clamp on negative (-) battery post",
            removeFirst = "Positive (+) Terminal Secured",
            teardownPath = "Positive (+) Terminal ➔ Negative (-) Terminal ➔ Ignition Test",
            steps = listOf(
                ChecklistStep("Clean negative post and clamp inside", "Remove any oxidation or grease."),
                ChecklistStep("Push negative clamp onto post", "A small spark is normal as electronics initialize."),
                ChecklistStep("Tighten 10mm negative clamp nut", "Ensure clamp cannot move or rotate on post."),
                ChecklistStep("Apply dielectric grease or anti-corrosion spray", "Protects terminals from future acid bloom.")
            ),
            animationStartTime = f(640),
            animationTime = f(670),
            animationDurationMs = 900L,
            infoTitle = "Final Verification",
            infoItems = listOf(
                ReplaceBatteryInfoItem("Test Ignition", "Start engine and check dashboard for battery warning light. Voltage should read 13.8V–14.4V with alternator running.")
            )
        )
    )
)
