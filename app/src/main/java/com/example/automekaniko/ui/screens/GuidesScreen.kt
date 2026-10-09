package com.example.automekaniko.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automekaniko.R
import com.example.automekaniko.tutorialTarget
import com.example.automekaniko.ui.components.BottomNavBar
import com.example.automekaniko.ui.components.BrandedTopBar
import com.example.automekaniko.ui.components.GlassCard
import com.example.automekaniko.ui.components.NavTab
import com.example.automekaniko.ui.components.TutorialOverlay
import com.example.automekaniko.ui.theme.ThemeRed

@Composable
fun GuidesScreen(
    dtcSummaryText: String,
    maintSummaryText: String,
    onDtcClick: () -> Unit,
    onMaintClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDtcExpanded by remember { mutableStateOf(false) }
    var isMaintExpanded by remember { mutableStateOf(false) }

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
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {

                // ── CARD 1: DTC CODES ───────────────────────────────────────
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .tutorialTarget(R.id.cardDtc),
                    borderColor = MaterialTheme.colorScheme.outline,
                    borderWidth = 1.2.dp,
                    cornerRadius = 20.dp,
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Clickable Header Action Bar
                        Surface(
                            onClick = onDtcClick,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, ThemeRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ThemeRed,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_sensors),
                                            contentDescription = "DTC Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "DTC Codes",
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Step-by-Step Repair to Fix DTC Error Codes",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Text(
                                    text = "›",
                                    color = ThemeRed,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        // Trouble Codes Overview Button
                        Surface(
                            onClick = { isDtcExpanded = !isDtcExpanded },
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Black,
                            border = BorderStroke(1.dp, ThemeRed),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isDtcExpanded) "📋 Hide Trouble Codes ▴" else "📋 Trouble Codes Overview ▾",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Peek Preview Row
                        if (!isDtcExpanded) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable { isDtcExpanded = true },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PeekChip(text = "P0301", isHighlighted = true)
                                PeekChip(text = "P2118", isHighlighted = false)
                                PeekChip(text = "P0100", isHighlighted = false)
                                Text(
                                    text = "+More ▾",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Inline Expandable Container
                        AnimatedVisibility(
                            visible = isDtcExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = "ACTIVE TROUBLE CODES BREAKDOWN",
                                    color = ThemeRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                CodeDetailItem("P0301 — Cylinder 1 Misfire Detected", "Inspect ignition coil #1, spark plug condition & gap, and fuel injector circuit.")
                                CodeDetailItem("P2118 — Throttle Control Motor Power", "Inspect 10A ETCS fuse in engine bay fuse box and check throttle actuator connector.")
                                CodeDetailItem("P0100 — Mass Air Flow Sensor Circuit", "Check MAF sensor harness, clean sensing wire, and inspect air filter box.")

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "INCLUDED REPAIR FEATURES",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FeatureChip(text = "3D Model", isPrimary = true)
                                    FeatureChip(text = "Root Cause Analysis", isPrimary = false)
                                    FeatureChip(text = "Step-by-Step Fix", isPrimary = false)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Summary Footnote
                        Text(
                            text = dtcSummaryText,
                            color = ThemeRed,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.clickable { onDtcClick() }
                        )
                    }
                }

                // ── CARD 2: MAINTENANCE ─────────────────────────────────────
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tutorialTarget(R.id.cardMaintenance),
                    borderColor = MaterialTheme.colorScheme.outline,
                    borderWidth = 1.2.dp,
                    cornerRadius = 20.dp,
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Clickable Header Action Bar
                        Surface(
                            onClick = onMaintClick,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, ThemeRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ThemeRed,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_settings),
                                            contentDescription = "Maintenance Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Maintenance",
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Step-by-Step Repair Guides & Checklist",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Text(
                                    text = "›",
                                    color = ThemeRed,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        // Toolkit & Procedures Overview Button
                        Surface(
                            onClick = { isMaintExpanded = !isMaintExpanded },
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Black,
                            border = BorderStroke(1.dp, ThemeRed),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isMaintExpanded) "🧰 Hide Toolkit & Procedures ▴" else "🧰 Toolkit & Procedures ▾",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Peek Preview Row
                        if (!isMaintExpanded) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable { isMaintExpanded = true },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PeekChip(text = "Oil & Filter", isHighlighted = false)
                                PeekChip(text = "Air Filter", isHighlighted = false)
                                PeekChip(text = "Battery", isHighlighted = false)
                                Text(
                                    text = "+More ▾",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Inline Expandable Container
                        AnimatedVisibility(
                            visible = isMaintExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = "GUIDED MAINTENANCE PROCEDURES",
                                    color = ThemeRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                CodeDetailItem("Engine Oil & Filter Change", "Inspect dipstick level, viscosity color, drain plug gasket & filter torque.")
                                CodeDetailItem("Air Intake Filter Inspection", "Check filter element cleanliness, housing clips, and intake duct sealing.")
                                CodeDetailItem("12V Battery & Terminal Maintenance", "Test 12.4V-12.7V rest voltage, clean terminal corrosion & inspect tray mount.")

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "REQUIRED TOOLKIT & SUPPLIES",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FeatureChip(text = "Wrench Set", isPrimary = true)
                                    FeatureChip(text = "Jack Stand", isPrimary = true)
                                    FeatureChip(text = "Screwdriver", isPrimary = true)
                                    FeatureChip(text = "Gloves", isPrimary = true)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Summary Footnote
                        Text(
                            text = maintSummaryText,
                            color = ThemeRed,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.clickable { onMaintClick() }
                        )
                    }
                }
            }
        }

        // Onboarding Tutorial Overlay
        TutorialOverlay()
    }
}

@Composable
private fun PeekChip(text: String, isHighlighted: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isHighlighted) Color.Black else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isHighlighted) ThemeRed else MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text,
            color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FeatureChip(text: String, isPrimary: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isPrimary) Color.Black else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isPrimary) ThemeRed else MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text,
            color = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CodeDetailItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
