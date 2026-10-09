package com.example.automekaniko.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.automekaniko.R
import com.example.automekaniko.ui.theme.ThemeRed

@Composable
fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepTitle: String,
    modifier: Modifier = Modifier,
    onStepSelected: ((Int) -> Unit)? = null
) {
    if (totalSteps <= 0) return

    val haptic = LocalHapticFeedback.current
    val progress = ((currentStep + 1).toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350),
        label = "StepProgressAnim"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row: Red Brand Badge + Step Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Badge ("Step 1 of 6")
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ThemeRed)
                ) {
                    Text(
                        text = "Step ${currentStep + 1} of $totalSteps",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Active Step Title
                Text(
                    text = stepTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Linear Progress Bar (Brand Red)
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ThemeRed,
                trackColor = ThemeRed.copy(alpha = 0.25f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step Dots Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalSteps) {
                    val isCompleted = i < currentStep
                    val isCurrent = i == currentStep

                    val dotBgColor by animateColorAsState(
                        targetValue = when {
                            isCurrent -> ThemeRed
                            isCompleted -> Color.Black
                            else -> MaterialTheme.colorScheme.surface
                        },
                        animationSpec = tween(durationMillis = 250),
                        label = "DotBgColor"
                    )

                    val dotBorderColor by animateColorAsState(
                        targetValue = when {
                            isCurrent -> ThemeRed
                            isCompleted -> ThemeRed
                            else -> MaterialTheme.colorScheme.outline
                        },
                        animationSpec = tween(durationMillis = 250),
                        label = "DotBorderColor"
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(dotBgColor)
                            .border(1.5.dp, dotBorderColor, CircleShape)
                            .clickable(enabled = onStepSelected != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onStepSelected?.invoke(i)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_white),
                                contentDescription = "Step ${i + 1} completed",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "${i + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
