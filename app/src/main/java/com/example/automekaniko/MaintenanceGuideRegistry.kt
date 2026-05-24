package com.example.automekaniko

// Maintenance Guide Registry
// --------------------------
// To add a new maintenance guide:
//   1. Create a new file e.g. Maintenance_BrakeGuide.kt
//   2. Define val BrakeMaintenanceGuide = MaintenanceGuide(...) in that file
//   3. Add it to maintenanceGuides below.

val maintenanceGuides: List<MaintenanceGuide> = listOf(
    VPMCGuide,
    ChangeOilGuide,
)
