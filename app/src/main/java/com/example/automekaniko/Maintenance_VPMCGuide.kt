package com.example.automekaniko

private typealias MaintenanceSlide = MAINTAINANCEActivity.CameraSlide
private typealias MaintenanceVec3 = MAINTAINANCEActivity.Vec3
private typealias MaintenanceInfoItem = MAINTAINANCEActivity.InfoItem

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
            animationDurationMs = 650L,
            infoTitle = "Battery Maintenance",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use a digital multimeter for voltage and a wire brush for cleaning terminals."),
                MaintenanceInfoItem("What NOT to Use", "Never use a flame near the battery (explosive gases) or a standard wrench without insulation."),
                MaintenanceInfoItem("Voltage Check", "A healthy battery should read 12.4V to 12.7V when the engine is off."),
                MaintenanceInfoItem("Corrosion", "Clean terminals if you see white/blue powdery deposits to ensure good contact.")
            )
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
            animationDurationMs = 650L,
            infoTitle = "Lighting System",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use high-quality halogen or LED bulbs matching your vehicle's specifications. Wear gloves."),
                MaintenanceInfoItem("What NOT to Use", "Do not touch the glass part of a new halogen bulb with bare fingers; oils can cause it to burst."),
                MaintenanceInfoItem("Headlights", "Check both low and high beams. Dim lights may indicate a failing bulb or battery."),
                MaintenanceInfoItem("Signals", "Ensure all 4 turn signals blink at a normal rate. Fast blinking means a bulb is out.")
            )
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
            animationDurationMs = 650L,
            infoTitle = "Types of Oil & Recommended",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Always use the viscosity grade (e.g., 5W-30) specified in your owner's manual."),
                MaintenanceInfoItem("What NOT to Use", "Do not use oil additives unless recommended by the manufacturer. Avoid mixing different oil types."),
                MaintenanceInfoItem("Conventional Oil", "Standard motor oil made from refined crude oil. Provides basic protection.", R.drawable.conventionaloil),
                MaintenanceInfoItem("Full Synthetic Oil", "Chemically engineered for higher performance and superior protection.", R.drawable.fullysynthetic),
                MaintenanceInfoItem("High Mileage Oil", "Designed specifically for vehicles with over 75,000 miles.", R.drawable.highmilleage)
            )
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
            animationDurationMs = 650L,
            infoTitle = "Coolant & Cooling",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use a 50/50 mix of distilled water and the specific coolant type (HOAT, OAT, etc.) for your car."),
                MaintenanceInfoItem("What NOT to Use", "Never use 100% tap water (causes scale) or 100% coolant (freezes/overheats easily)."),
                MaintenanceInfoItem("Reservoir Level", "Never open the radiator cap when the engine is hot. Check the plastic reservoir instead."),
                MaintenanceInfoItem("Coolant Color", "Should be bright green, orange, or pink. If it looks rusty or oily, seek service.")
            )
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
            animationDurationMs = 1050L,
            infoTitle = "Brake Safety",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use the specific brake fluid grade (DOT 3, 4, or 5.1) listed on your reservoir cap."),
                MaintenanceInfoItem("What NOT to Use", "Never use DOT 5 (silicone-based) in a system designed for DOT 3 or 4. Do not use old, opened fluid."),
                MaintenanceInfoItem("Brake Fluid", "Check level in the master cylinder. Low fluid can mean worn pads or a leak."),
                MaintenanceInfoItem("Pad Thickness", "If pads are less than 1/4 inch (6mm) thick, they should be replaced soon.")
            )
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
            animationDurationMs = 1250L,
            infoTitle = "Tire Maintenance",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use a reliable tire pressure gauge. Inflate to the PSI listed on the door jamb sticker."),
                MaintenanceInfoItem("What NOT to Use", "Do not inflate to the 'Max PSI' listed on the tire sidewall; that is the tire's burst limit."),
                MaintenanceInfoItem("Proper PSI", "Check the sticker inside the driver's door for the recommended pressure."),
                MaintenanceInfoItem("Tread Depth", "Use a coin to check tread depth. Worn tires are dangerous in wet conditions.")
            )
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
            animationDurationMs = 1850L,
            infoTitle = "Engine Inspection",
            infoItems = listOf(
                MaintenanceInfoItem("What to Do", "Start the engine when cold and listen carefully. Check for smooth idle, no rough sounds, and normal exhaust color."),
                MaintenanceInfoItem("What NOT to Do", "Never touch the engine when running or hot. Do not remove the radiator cap while the engine is warm."),
                MaintenanceInfoItem("Unusual Sounds", "Knocking, pinging, or grinding noises may indicate serious problems. Squealing usually means a worn belt."),
                MaintenanceInfoItem("Fluid Leaks", "Look for oil, coolant, or transmission fluid leaks under the engine. Any puddle warrants immediate attention."),
                MaintenanceInfoItem("Belts & Hoses", "Check for cracks, fraying, or soft spots. Belts should feel firm and hoses should have minimal give.")
            )
        )
    )
)

