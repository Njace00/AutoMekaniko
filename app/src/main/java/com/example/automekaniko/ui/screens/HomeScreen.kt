package com.example.automekaniko.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automekaniko.R
import com.example.automekaniko.VehicleProfile
import com.example.automekaniko.tutorialTarget
import com.example.automekaniko.ui.components.BottomNavBar
import com.example.automekaniko.ui.components.BrandedTopBar
import com.example.automekaniko.ui.components.GlassCard
import com.example.automekaniko.ui.components.NavTab
import com.example.automekaniko.ui.components.TutorialOverlay
import com.example.automekaniko.ui.components.VehicleSelectorDialog
import com.example.automekaniko.ui.theme.ThemeRed
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    activeVehicle: VehicleProfile,
    onVehicleUpdated: (VehicleProfile) -> Unit,
    onGuidesClick: () -> Unit,
    onLiveClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTutorialClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showVehicleSelectorDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                BrandedTopBar(
                    onTutorialClick = onTutorialClick
                )
            },
            bottomBar = {
                BottomNavBar(
                    selectedTab = NavTab.HOME,
                    onTabSelected = { tab ->
                        if (tab == NavTab.SETTINGS) {
                            onSettingsClick()
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->

            // Staggered Entrance Cascade Animations
            val animVehicleAlpha = remember { Animatable(0f) }
            val animVehicleTranslationY = remember { Animatable(30f) }

            val anim3DAlpha = remember { Animatable(0f) }
            val anim3DTranslationY = remember { Animatable(30f) }

            val animLiveAlpha = remember { Animatable(0f) }
            val animLiveTranslationY = remember { Animatable(30f) }

            LaunchedEffect(Unit) {
                // Vehicle card
                delay(40)
                animVehicleAlpha.animateTo(1f, animationSpec = tween(durationMillis = 320))
                animVehicleTranslationY.animateTo(0f, animationSpec = tween(durationMillis = 320))

                // 3D card
                delay(10)
                anim3DAlpha.animateTo(1f, animationSpec = tween(durationMillis = 320))
                anim3DTranslationY.animateTo(0f, animationSpec = tween(durationMillis = 320))

                // Live card
                delay(10)
                animLiveAlpha.animateTo(1f, animationSpec = tween(durationMillis = 320))
                animLiveTranslationY.animateTo(0f, animationSpec = tween(durationMillis = 320))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Logo / Hero
                Image(
                    painter = painterResource(id = R.mipmap.logo),
                    contentDescription = "AutoMekaniko Logo",
                    modifier = Modifier
                        .size(105.dp)
                        .padding(bottom = 12.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "What would you like to do?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    letterSpacing = 0.03.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // ACTIVE VEHICLE CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .graphicsLayer {
                            alpha = animVehicleAlpha.value
                            translationY = animVehicleTranslationY.value * density
                        }
                        .tutorialTarget(R.id.cardActiveVehicle)
                ) {
                    GlassCard(
                        onClick = { showVehicleSelectorDialog = true },
                        borderColor = ThemeRed,
                        borderWidth = 1.5.dp,
                        cornerRadius = 20.dp,
                        elevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_car),
                                contentDescription = "Active Vehicle Image",
                                tint = ThemeRed,
                                modifier = Modifier.size(42.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = activeVehicle.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "${activeVehicle.engine} • ${activeVehicle.oilCapacity} Oil",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            TextButton(
                                onClick = { showVehicleSelectorDialog = true },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Switch",
                                    color = ThemeRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3D REPAIR GUIDES CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                        .graphicsLayer {
                            alpha = anim3DAlpha.value
                            translationY = anim3DTranslationY.value * density
                        }
                        .tutorialTarget(R.id.card3D)
                ) {
                    GlassCard(
                        onClick = onGuidesClick,
                        borderColor = MaterialTheme.colorScheme.outline,
                        borderWidth = 1.2.dp,
                        cornerRadius = 20.dp,
                        elevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_3d_guides),
                                contentDescription = "3D Guides Icon",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(4.dp)
                            )

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "3D Guides",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Maintenance · DTC repair",
                                    color = ThemeRed,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Text(
                                text = "›",
                                color = ThemeRed,
                                fontSize = 32.sp
                            )
                        }
                    }
                }

                // REAL-TIME OBD TELEMETRY CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = animLiveAlpha.value
                            translationY = animLiveTranslationY.value * density
                        }
                        .tutorialTarget(R.id.cardLive)
                ) {
                    GlassCard(
                        onClick = onLiveClick,
                        borderColor = MaterialTheme.colorScheme.outline,
                        borderWidth = 1.2.dp,
                        cornerRadius = 20.dp,
                        elevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_sensors),
                                contentDescription = "Live Data Icon",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(4.dp)
                            )

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Live Data",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "OBD-II · Real-time sensors",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Text(
                                text = "›",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 32.sp
                            )
                        }
                    }
                }
            }
        }

        // Onboarding Tutorial Overlay
        TutorialOverlay()

        // Compose Vehicle Selector Dialog
        if (showVehicleSelectorDialog) {
            VehicleSelectorDialog(
                currentVehicle = activeVehicle,
                onVehicleSelected = { updatedVehicle ->
                    onVehicleUpdated(updatedVehicle)
                },
                onDismiss = { showVehicleSelectorDialog = false }
            )
        }
    }
}
