package com.example.automekaniko

private typealias MaintenanceSlide = MAINTAINANCEActivity.CameraSlide
private typealias MaintenanceVec3 = MAINTAINANCEActivity.Vec3
private typealias MaintenanceInfoItem = MAINTAINANCEActivity.InfoItem

private fun f(frame: Int): Float = frame / 24f

val VPMCGuide = MaintenanceGuide(
    name = "Vehicle Preventive Maintenance Checklist (VPMC)",
    glbFile = "VPMC(Vehicle Preventive Maintainance Checklist).glb",
    requiredTools = listOf("Tire Pressure Gauge", "Tread Depth Tool / Penny", "Dipstick Rag", "Flashlight"),
    estimatedTime = "15–20 mins",
    difficulty = "Easy",
    prerequisites = listOf(
        "Park vehicle on flat, level ground",
        "Set emergency parking brake",
        "Turn off engine and let it cool for 15 minutes before checking oil"
    ),
    slides = listOf(
        MaintenanceSlide(
            title = "Vehicle Overview",
            description = "This is the Preview of the Vehicle...",
            eye = MaintenanceVec3(-1.57f, 0.77f, -1.34f),
            lookAt = MaintenanceVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                ChecklistStep("Walk around the vehicle", "Check for any obvious body damage or items leaning against the car."),
                ChecklistStep("Check for visible damage", "Look for dents, deep scratches, or cracked lights."),
                ChecklistStep("Inspect undercarriage", "Check for hanging parts or obvious fluid puddles under the car."),
            ),
            animationStartTime = f(1),
            animationTime = f(1),
            animationDurationMs = 650L,
        ),
        MaintenanceSlide(
            title = "Vehicle Overview2",
            description = "This is the Preview of the Vehicle...",
            eye = MaintenanceVec3(-1.57f, 0.77f, -1.34f),
            lookAt = MaintenanceVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                ChecklistStep("Check body panels", "Ensure all panels are securely attached and aligned."),
                ChecklistStep("Inspect windshield and glass", "Look for chips or cracks that could impair visibility."),
                ChecklistStep("Check mirrors and wipers", "Verify mirrors are secure and wipers aren't brittle."),
            ),
            animationStartTime = f(1),
            animationTime = f(60),
            animationDurationMs = 650L,
        ),
        MaintenanceSlide(
            title = "Battery",
            description = "Check Battery...",
            eye = MaintenanceVec3(-0.15f, 0.37f, -0.74f),
            lookAt = MaintenanceVec3(0.00f, 0.10f, 0.00f),
            steps = listOf(
                ChecklistStep("Check terminal connections", "Ensure terminals are tight and won't move by hand."),
                ChecklistStep("Look for corrosion or leaks", "White or blue powder indicates corrosion; clean with a wire brush."),
                ChecklistStep("Verify voltage is 12.4-12.7V", "Use a multimeter. If below 12.2V, the battery needs charging."),
                ChecklistStep("Inspect battery case for swelling", "A swollen case indicates overcharging or extreme heat damage."),
            ),
            animationStartTime = f(70),
            animationTime = f(120),
            animationDurationMs = 650L,
            infoTitle = "Battery Maintenance",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use a digital multimeter for voltage and a wire brush for cleaning terminals."),
                MaintenanceInfoItem("What NOT to Use", "Never use a flame near the battery (explosive gases) or a standard wrench without insulation."),
                MaintenanceInfoItem("Voltage Check", "A healthy battery should read 12.4V to 12.7V when the engine is off."),
                MaintenanceInfoItem("Corrosion", "Clean terminals if you see white/blue powdery deposits to ensure good contact."),
            ),
        ),
        MaintenanceSlide(
            title = "Lights",
            description = "Check all Lights:...",
            eye = MaintenanceVec3(-0.01f, 0.55f, -1.65f),
            lookAt = MaintenanceVec3(-0.01f, 0.10f, 0.00f),
            steps = listOf(
                ChecklistStep("Test headlights (low & high beam)", "Check both settings; if one is out, replace the bulb immediately."),
                ChecklistStep("Check tail lights and brake lights", "Have someone step on the brakes while you check the rear."),
                ChecklistStep("Test turn signals front and rear", "Ensure all four blinkers work correctly."),
                ChecklistStep("Check reverse and hazard lights", "Verify the white reverse lights and red hazards are functional."),
            ),
            animationStartTime = f(140),
            animationTime = f(265),
            animationDurationMs = 650L,
            infoTitle = "Lighting System",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use high-quality halogen or LED bulbs matching your vehicle's specifications. Wear gloves."),
                MaintenanceInfoItem("What NOT to Use", "Do not touch the glass part of a new halogen bulb with bare fingers; oils can cause it to burst."),
                MaintenanceInfoItem("Headlights", "Check both low and high beams. Dim lights may indicate a failing bulb or battery."),
                MaintenanceInfoItem("Signals", "Ensure all 4 turn signals blink at a normal rate. Fast blinking means a bulb is out."),
            ),
        ),
        MaintenanceSlide(
            title = "Oil",
            description = "Check your oil, and oil level..",
            eye = MaintenanceVec3(0.07f, 0.37f, -0.74f),
            lookAt = MaintenanceVec3(0.00f, -0.3f, 0.00f),
            steps = listOf(
                ChecklistStep("Pull out dipstick and wipe clean", "Use a lint-free cloth or paper towel."),
                ChecklistStep("Reinsert and check oil level", "The oil should be between the 'Min' and 'Max' dots."),
                ChecklistStep("Check oil color (should be amber)", "Dark black oil is old; milky oil indicates a coolant leak."),
                ChecklistStep("Look for milky or gritty texture", "Grittiness means metal wear; milky texture means water/coolant mix."),
            ),
            animationStartTime = f(270),
            animationTime = f(315),
            animationDurationMs = 650L,
            infoTitle = "Types of Oil & Recommended",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Always use the viscosity grade (e.g., 5W-30) specified in your owner's manual."),
                MaintenanceInfoItem("What NOT to Use", "Do not use oil additives unless recommended by the manufacturer. Avoid mixing different oil types."),
                MaintenanceInfoItem("Conventional Oil", "Standard motor oil made from refined crude oil. Provides basic protection.", R.drawable.conventionaloil),
                MaintenanceInfoItem("Full Synthetic Oil", "Chemically engineered for higher performance and superior protection.", R.drawable.fullysynthetic),
                MaintenanceInfoItem("High Mileage Oil", "Designed specifically for vehicles with over 75,000 miles.", R.drawable.highmilleage),
            ),
        ),
        MaintenanceSlide(
            title = "Water",
            description = "Check Water Radiator Level...",
            eye = MaintenanceVec3(-0.10f, 0.37f, -0.69f),
            lookAt = MaintenanceVec3(0.00f, -0.6f, 0.00f),
            steps = listOf(
                ChecklistStep("Check coolant reservoir level", "Should be at the 'Full' line when cold."),
                ChecklistStep("Inspect for leaks around hoses", "Look for white/pink crusty residue at hose clamps."),
                ChecklistStep("Check radiator cap condition", "Ensure the rubber seal isn't cracked or missing."),
                ChecklistStep("Verify coolant color is clean", "Should be bright green, pink, or blue depending on the car."),
            ),
            animationStartTime = f(330),
            animationTime = f(385),
            animationDurationMs = 650L,
            infoTitle = "Coolant & Cooling",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use a 50/50 mix of distilled water and the specific coolant type (HOAT, OAT, etc.) for your car."),
                MaintenanceInfoItem("What NOT to Use", "Never use 100% tap water (causes scale) or 100% coolant (freezes/overheats easily).."),
                MaintenanceInfoItem("Reservoir Level", "Never open the radiator cap when the engine is hot. Check the plastic reservoir instead."),
                MaintenanceInfoItem("Coolant Color", "Should be bright green, orange, or pink. If it looks rusty or oily, seek service."),
            ),
        ),
        MaintenanceSlide(
            title = "Brake",
            description = "Check Brake...",
            eye = MaintenanceVec3(-0.7f, 0.15f, -1.2f),
            lookAt = MaintenanceVec3(0.00f, 0f, 0.00f),
            steps = listOf(
                ChecklistStep("Inspect brake pad thickness", "Pads should be at least 1/4 inch (6mm) thick."),
                ChecklistStep("Check rotor surface for grooves", "Deep grooves indicate the pads are worn to the metal."),
                ChecklistStep("Look for brake fluid leaks", "Check for wet spots on the inner wheel or master cylinder."),
                ChecklistStep("Test brake pedal feel and travel", "Should feel firm; a spongy pedal means air in the lines."),
            ),
            animationStartTime = f(400),
            animationTime = f(440),
            animationDurationMs = 1050L,
            infoTitle = "Brake Safety",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use the specific brake fluid grade (DOT 3, 4, or 5.1) listed on your reservoir cap."),
                MaintenanceInfoItem("What NOT to Use", "Never use DOT 5 (silicone-based) in a system designed for DOT 3 or 4. Do not use old, opened fluid."),
                MaintenanceInfoItem("Brake Fluid", "Check level in the master cylinder. Low fluid can mean worn pads or a leak."),
                MaintenanceInfoItem("Pad Thickness", "If pads are less than 1/4 inch (6mm) thick, they should be replaced soon."),
            ),
        ),
        MaintenanceSlide(
            title = "Tire Air Pressure",
            description = "Check tire...",
            eye = MaintenanceVec3(-0.7f, 0.15f, -1.2f),
            lookAt = MaintenanceVec3(0.00f, 0f, 0.00f),
            steps = listOf(
                ChecklistStep("Check pressure on all 4 tires", "Use a gauge; match the PSI on the door jamb sticker."),
                ChecklistStep("Inspect tread depth", "Use the penny test; if you see Lincoln's head, you need tires."),
                ChecklistStep("Look for cracks or bulges", "Sidewall damage is dangerous and can lead to a blowout."),
                ChecklistStep("Check spare tire pressure", "Don't forget the spare! It often loses air over time."),
            ),
            animationStartTime = f(470),
            animationTime = f(500),
            animationDurationMs = 1250L,
            infoTitle = "Tire Maintenance",
            infoItems = listOf(
                MaintenanceInfoItem("What to Use", "Use a reliable tire pressure gauge. Inflate to the PSI listed on the door jamb sticker."),
                MaintenanceInfoItem("What NOT to Use", "Do not inflate to the 'Max PSI' listed on the tire sidewall; that is the tire's burst limit."),
                MaintenanceInfoItem("Proper PSI", "Check the sticker inside the driver's door for the recommended pressure."),
                MaintenanceInfoItem("Tread Depth", "Use a coin to check tread depth. Worn tires are dangerous in wet conditions."),
            ),
        ),
        MaintenanceSlide(
            title = "Engine",
            description = "Inspect for Unusual Engine Behaviors and Sounds",
            eye = MaintenanceVec3(-0.03f, 0.50f, -0.770f),
            lookAt = MaintenanceVec3(0.10f, -0.20f, 0.00f),
            steps = listOf(
                ChecklistStep("Listen for unusual sounds", "Squealing means belt issues; knocking means serious internal wear."),
                ChecklistStep("Check for smoke or burning smell", "Acrid smoke is oil; sweet smell is coolant; rubber is belts."),
                ChecklistStep("Inspect belts and hoses", "Look for cracks in belts and soft spots in coolant hoses."),
                ChecklistStep("Check air filter condition", "A dirty filter reduces gas mileage and performance."),
            ),
            animationStartTime = f(470),
            animationTime = f(500),
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
