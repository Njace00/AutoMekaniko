package com.example.automekaniko

private typealias MaintenanceSlide = MAINTAINANCEActivity.CameraSlide
private typealias MaintenanceVec3 = MAINTAINANCEActivity.Vec3

private fun f(frame: Int): Float = frame / 24f

val VPMCGuide = MaintenanceGuide(
    name = "Vehicle Preventive Maintenance Checklist (VPMC)",
    glbFile = "Vehicle Preventive Maintenance Checklist (VPMC).glb",
    slides = listOf(
        MaintenanceSlide(
            title = "Vehicle Overview",
            description = "This is the Preview of the Vehicle...",
            eye = MaintenanceVec3(-1.57f, 0.77f, -1.34f),
            lookAt = MaintenanceVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                "Walk around the vehicle",
                "Check for visible damage",
                "Inspect undercarriage"
            ),
            animationStartTime = f(1),
            animationTime = f(1),
            animationDurationMs = 650L
        ),
        MaintenanceSlide(
            title = "Vehicle Overview2",
            description = "This is the Preview of the Vehicle...",
            eye = MaintenanceVec3(-1.57f, 0.77f, -1.34f),
            lookAt = MaintenanceVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                "Check body panels",
                "Inspect windshield and glass",
                "Check mirrors and wipers"
            ),
            animationStartTime = f(1),
            animationTime = f(60),
            animationDurationMs = 650L
        ),
        MaintenanceSlide(
            title = "Battery",
            description = "Check Battery...",
            eye = MaintenanceVec3(-0.15f, 0.37f, -0.74f),
            lookAt = MaintenanceVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                "Check terminal connections",
                "Look for corrosion or leaks",
                "Verify voltage is 12.4-12.7V",
                "Inspect battery case for swelling"
            ),
            animationStartTime = f(60),
            animationTime = f(100),
            animationDurationMs = 650L
        ),
        MaintenanceSlide(
            title = "Lights",
            description = "Check all Lights:...",
            eye = MaintenanceVec3(-0.01f, 0.55f, -1.65f),
            lookAt = MaintenanceVec3(-0.01f, 0.10f, 0.00f),
            steps = listOf(
                "Test headlights (low & high beam)",
                "Check tail lights and brake lights",
                "Test turn signals front and rear",
                "Check reverse and hazard lights"
            ),
            animationStartTime = f(100),
            animationTime = f(170),
            animationDurationMs = 650L
        ),
        MaintenanceSlide(
            title = "Oil",
            description = "Check your oil, and oil level..",
            eye = MaintenanceVec3(0.07f, 0.37f, -0.74f),
            lookAt = MaintenanceVec3(0.00f, -0.3f, 0.00f),
            steps = listOf(
                "Pull out dipstick and wipe clean",
                "Reinsert and check oil level",
                "Check oil color (should be amber)",
                "Look for milky or gritty texture"
            ),
            animationStartTime = f(220),
            animationTime = f(240),
            animationDurationMs = 650L
        ),
        MaintenanceSlide(
            title = "Water",
            description = "Check Water Radiator Level...",
            eye = MaintenanceVec3(-0.01f, 0.37f, -0.69f),
            lookAt = MaintenanceVec3(0.00f, -0.6f, 0.00f),
            steps = listOf(
                "Check coolant reservoir level",
                "Inspect for leaks around hoses",
                "Check radiator cap condition",
                "Verify coolant color is clean"
            ),
            animationStartTime = f(240),
            animationTime = f(275),
            animationDurationMs = 650L
        ),
        MaintenanceSlide(
            title = "Brake",
            description = "Check Brake...",
            eye = MaintenanceVec3(-0.7f, 0.15f, -1.2f),
            lookAt = MaintenanceVec3(0.00f, 0f, 0.00f),
            steps = listOf(
                "Inspect brake pad thickness",
                "Check rotor surface for grooves",
                "Look for brake fluid leaks",
                "Test brake pedal feel and travel"
            ),
            animationStartTime = f(310),
            animationTime = f(320),
            animationDurationMs = 1050L
        ),
        MaintenanceSlide(
            title = "Tire Air Pressure",
            description = "Check tire...",
            eye = MaintenanceVec3(0.7f, 0.15f, -1.2f),
            lookAt = MaintenanceVec3(0.00f, 0f, 0.00f),
            steps = listOf(
                "Check pressure on all 4 tires",
                "Inspect tread depth",
                "Look for cracks or bulges",
                "Check spare tire pressure"
            ),
            animationStartTime = f(315),
            animationTime = f(380),
            animationDurationMs = 1250L
        ),
        MaintenanceSlide(
            title = "Engine",
            description = "Inspect for Unusual Engine Behaviors and Sounds",
            eye = MaintenanceVec3(-0.03f, 0.50f, -0.770f),
            lookAt = MaintenanceVec3(0.10f, -0.20f, 0.00f),
            steps = listOf(
                "Listen for unusual sounds",
                "Check for smoke or burning smell",
                "Inspect belts and hoses",
                "Check air filter condition"
            ),
            animationStartTime = f(340),
            animationTime = f(405),
            animationDurationMs = 1850L
        )
    )
)

