package com.example.automekaniko.ui.components

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automekaniko.TutorialManager
import com.example.automekaniko.ui.theme.ThemeRed

@Composable
fun TutorialOverlay(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? AppCompatActivity ?: return

    val isVisible = TutorialManager.isTutorialActiveState.value
    val stepInfo = TutorialManager.getCurrentStepInfo()

    AnimatedVisibility(
        visible = isVisible && stepInfo != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        val info = stepInfo ?: return@AnimatedVisibility
        val targetRect = TutorialManager.boundsMap[info.targetViewId]

        val configuration = LocalConfiguration.current
        val density = LocalDensity.current
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

        // Determine smart dialog placement (Top vs Bottom) and compute exact gap spacing from target bounds
        val isTargetInBottomHalf = targetRect != null && ((targetRect.top + targetRect.bottom) / 2f) > (screenHeightPx / 2f)

        val cardAlignment = if (isTargetInBottomHalf) Alignment.TopCenter else Alignment.BottomCenter

        val cardModifier = if (targetRect != null) {
            if (isTargetInBottomHalf) {
                // Target is near bottom -> Position tutorial card at top with top padding below status bar
                Modifier
                    .statusBarsPadding()
                    .padding(start = 18.dp, end = 18.dp, top = 70.dp)
            } else {
                // Target is in top half -> Position tutorial card below targetRect with 20dp gap padding
                val targetBottomDp = with(density) { targetRect.bottom.toDp() }
                val topGapDp = (targetBottomDp + 20.dp).coerceAtLeast(80.dp)
                Modifier
                    .padding(start = 18.dp, end = 18.dp, top = topGapDp)
                    .navigationBarsPadding()
            }
        } else {
            // Default fallback
            Modifier
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 28.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    TutorialManager.advanceStep(activity)
                }
        ) {
            // Spotlight Scrim Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.99f) // Required for BlendMode.Clear
            ) {
                // 1. Draw dark semi-transparent overlay
                drawRect(color = Color.Black.copy(alpha = 0.75f))

                // 2. If target bounds are measured, carve out spotlight hole and draw glowing outline
                targetRect?.let { rect ->
                    val paddingPx = 8.dp.toPx()
                    val left = rect.left - paddingPx
                    val top = rect.top - paddingPx
                    val width = (rect.right - rect.left) + (paddingPx * 2)
                    val height = (rect.bottom - rect.top) + (paddingPx * 2)
                    val cornerRadiusPx = 16.dp.toPx()

                    // Clear cutout over target view
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                        blendMode = BlendMode.Clear
                    )

                    // Glowing red border stroke around target view
                    drawRoundRect(
                        color = ThemeRed,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }

            // Tutorial Dialogue Card (Calculates dynamic gap spacing from highlighted red box)
            Card(
                modifier = Modifier
                    .align(cardAlignment)
                    .then(cardModifier)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.5.dp, ThemeRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step ${info.stepIndex + 1} of ${info.totalSteps}: ${info.title}",
                            color = ThemeRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = {
                                TutorialManager.finishTutorial(activity)
                            }
                        ) {
                            Text(
                                text = "Skip",
                                color = ThemeRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = info.description,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tap anywhere on the screen to continue ›",
                        color = ThemeRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
