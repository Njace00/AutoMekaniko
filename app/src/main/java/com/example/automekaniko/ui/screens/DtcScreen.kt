package com.example.automekaniko.ui.screens

import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.automekaniko.ChecklistStep
import com.example.automekaniko.DtcGuide
import com.example.automekaniko.R
import com.example.automekaniko.VehicleProfile
import com.example.automekaniko.tutorialTarget
import com.example.automekaniko.ui.components.BottomNavBar
import com.example.automekaniko.ui.components.BrandedTopBar
import com.example.automekaniko.ui.components.InfoSection
import com.example.automekaniko.ui.components.NavTab
import com.example.automekaniko.ui.components.RepairChecklistDialog
import com.example.automekaniko.ui.components.TechnicalInfoDialog
import com.example.automekaniko.ui.components.ToolsPrepDialog
import com.example.automekaniko.ui.components.TutorialOverlay
import com.example.automekaniko.ui.components.VehicleSelectorDialog
import com.example.automekaniko.ui.theme.ThemeRed
import com.example.automekaniko.ui.theme.ThemeRedLight

@Composable
fun DtcScreen(
    activeVehicle: VehicleProfile,
    availableDtcGuides: List<DtcGuide>,
    selectedDtcGuide: DtcGuide?,
    currentSlideIndex: Int,
    sceneViewInstance: View,
    checkedStepIndices: Set<Int>,
    onVehicleUpdated: (VehicleProfile) -> Unit,
    onDtcGuideSelected: (DtcGuide) -> Unit,
    onStepCheckedChange: (Int, Boolean) -> Unit,
    onNextStepClick: () -> Unit,
    onPrevStepClick: () -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    var showVehicleSelectorDialog by remember { mutableStateOf(false) }
    var showToolsPrepDialog by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val vehicleDisplayName = activeVehicle.name.replace("Toyota ", "")
    val guideTitle = selectedDtcGuide?.let { "${it.code} — ${it.name}" } ?: "Select DTC Code Problem"
    val currentSlide = selectedDtcGuide?.slides?.getOrNull(currentSlideIndex)
    val totalSlides = selectedDtcGuide?.slides?.size ?: 1

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                BrandedTopBar(onTutorialClick = null)
            },
            bottomBar = {
                BottomNavBar(
                    selectedTab = NavTab.HOME,
                    onTabSelected = { tab ->
                        if (tab == NavTab.HOME) onHomeClick() else if (tab == NavTab.SETTINGS) onSettingsClick()
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Sub Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_red),
                            contentDescription = "Back",
                            tint = ThemeRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "DTC Guides",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Clickable Vehicle Profile Badge Chip
                    Surface(
                        onClick = { showVehicleSelectorDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = ThemeRedLight,
                        border = BorderStroke(1.dp, ThemeRed.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_car),
                                contentDescription = "Vehicle",
                                tint = ThemeRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = vehicleDisplayName,
                                color = ThemeRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // DTC Dropdown Selector Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .tutorialTarget(R.id.menuDtc)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.2.dp, ThemeRed)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = guideTitle,
                                color = if (selectedDtcGuide == null) ThemeRed else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_back_red),
                                contentDescription = "Dropdown",
                                tint = ThemeRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        availableDtcGuides.forEach { guide ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${guide.code} — ${guide.name}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    onDtcGuideSelected(guide)
                                    showToolsPrepDialog = true
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Action Chips Row (Difficulty & Tools Prep Button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ThemeRedLight
                        ) {
                            Text(
                                text = selectedDtcGuide?.difficulty ?: "Moderate",
                                color = ThemeRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "⏱ ${selectedDtcGuide?.estimatedTime ?: "30–40 mins"}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    // Tools & Prep Button
                    if (selectedDtcGuide != null) {
                        Surface(
                            onClick = { showToolsPrepDialog = true },
                            modifier = Modifier.tutorialTarget(R.id.btnToolsPrep),
                            shape = RoundedCornerShape(12.dp),
                            color = ThemeRedLight,
                            border = BorderStroke(1.dp, ThemeRed)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🧰 Tools & Prep",
                                    color = ThemeRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3D SceneView Viewport Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                        .tutorialTarget(R.id.navButtonsSection),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Embedded 3D SceneView Viewport
                        AndroidView(
                            factory = { sceneViewInstance },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (selectedDtcGuide != null) {
                            // Floating Action Buttons (Top-Right)
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Info Button FAB
                                Surface(
                                    onClick = { showInfoDialog = true },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .tutorialTarget(R.id.btnInfoModern),
                                    shape = CircleShape,
                                    color = Color.White,
                                    border = BorderStroke(1.5.dp, ThemeRed),
                                    shadowElevation = 6.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "i",
                                            color = ThemeRed,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Checklist Button FAB
                                Surface(
                                    onClick = { showChecklistDialog = true },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .tutorialTarget(R.id.btnOverviewModern),
                                    shape = CircleShape,
                                    color = Color.White,
                                    border = BorderStroke(1.5.dp, ThemeRed),
                                    shadowElevation = 6.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_check_white),
                                            contentDescription = "Checklist",
                                            tint = ThemeRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Bottom Step Action Row (Prev & Next Step Buttons)
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Prev Step Button
                                if (currentSlideIndex > 0) {
                                    OutlinedButton(
                                        onClick = onPrevStepClick,
                                        modifier = Modifier
                                            .weight(0.35f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.5.dp, ThemeRed),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.9f))
                                    ) {
                                        Text(
                                            text = "Prev",
                                            color = ThemeRed,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Next Step Button
                                Button(
                                    onClick = onNextStepClick,
                                    modifier = Modifier
                                        .weight(if (currentSlideIndex > 0) 0.65f else 1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeRed)
                                ) {
                                    Text(
                                        text = if (currentSlideIndex >= totalSlides - 1) "Finish" else "Next Step",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // Prompt Overlay when no guide is selected yet
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 20.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.85f),
                                border = BorderStroke(1.5.dp, ThemeRed)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Select a DTC Code Problem above",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Choose a diagnostic trouble code to view tools, safety prep, and 3D step-by-step repair guides.",
                                        color = Color.White.copy(alpha = 0.90f),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 21.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Onboarding Tutorial Overlay
        TutorialOverlay()

        // Vehicle Selector Modal Dialog
        if (showVehicleSelectorDialog) {
            VehicleSelectorDialog(
                currentVehicle = activeVehicle,
                onVehicleSelected = { updatedVehicle ->
                    onVehicleUpdated(updatedVehicle)
                },
                onDismiss = { showVehicleSelectorDialog = false }
            )
        }

        // Tools & Prep Modal Dialog
        if (showToolsPrepDialog && selectedDtcGuide != null) {
            ToolsPrepDialog(
                difficultyText = selectedDtcGuide.difficulty,
                estTimeText = selectedDtcGuide.estimatedTime,
                toolsList = selectedDtcGuide.requiredTools,
                safetyChecklist = selectedDtcGuide.prerequisites,
                onReadyStart = { showToolsPrepDialog = false },
                onDismiss = { showToolsPrepDialog = false }
            )
        }

        // Repair Checklist Dialog
        if (showChecklistDialog && selectedDtcGuide != null) {
            val sampleChecklist = currentSlide?.steps?.ifEmpty { null } ?: listOf(
                ChecklistStep("Ensure the engine is cool and the ignition is off", "Working on a hot engine can cause severe burns. Wait at least 30 minutes."),
                ChecklistStep("Open the hood and visually inspect the engine bay", "Look for loose wires, signs of rodents, or obvious fluid leaks."),
                ChecklistStep("Locate cylinder 1 — the first cylinder nearest the front of the engine", "Cylinder 1 is almost always the one closest to the drive belt/pulley side.")
            )

            RepairChecklistDialog(
                slideTitle = currentSlide?.title ?: "Overview",
                checklistItems = sampleChecklist,
                checkedStepIndices = checkedStepIndices,
                onStepCheckedChange = onStepCheckedChange,
                onDismiss = { showChecklistDialog = false }
            )
        }

        // Technical Diagnostic Info Dialog
        if (showInfoDialog && selectedDtcGuide != null) {
            val infoSectionsList = currentSlide?.infoItems?.map { InfoSection(it.title, it.description) }?.ifEmpty { null }
                ?: listOf(
                    InfoSection("Symptoms", selectedDtcGuide.description),
                    InfoSection("Coil Swap Test", "Swap Coil #1 with Coil #2. Clear DTC and test drive. If code changes to P0302, Coil #1 is bad!"),
                    InfoSection("Multimeter Spec", "Coil Primary Resistance: 0.5–1.2 Ω across terminals 1 & 2. Secondary: 10k–16k Ω."),
                    InfoSection("Plug Gap & Torque", "Set gap to 1.1mm (0.044 in) with wire gauge. Torque plug to 18–25 Nm."),
                    InfoSection("Safety Warning", "Never remove spark plugs from a warm aluminum head to prevent thread stripping.")
                )

            TechnicalInfoDialog(
                sections = infoSectionsList,
                onDismiss = { showInfoDialog = false }
            )
        }
    }
}
