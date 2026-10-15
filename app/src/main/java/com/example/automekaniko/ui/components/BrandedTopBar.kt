package com.example.automekaniko.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.automekaniko.R
import com.example.automekaniko.tutorialTarget
import com.example.automekaniko.ui.theme.AutoMekanikoTheme
import com.example.automekaniko.ui.theme.LocalDarkTheme
import com.example.automekaniko.ui.theme.ThemeRed

/**
 * Glassmorphic App Bar flush with the top screen edge, featuring theme-aware light refraction (Red to White in Light Mode, Red to Black in Dark Mode).
 */
@Composable
fun BrandedTopBar(
    modifier: Modifier = Modifier,
    onTutorialClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    title: String? = null,
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalDarkTheme.current

    // Base glass translucent fill (Light = Crystal White, Dark = Obsidian Glass)
    val glassBgBase = if (isDark) {
        Color(0xFF121316).copy(alpha = 0.88f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.82f)
    }

    // Refraction Brush: Red to White in Light Mode, Red to Black in Dark Mode
    val refractionBrush = if (isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                ThemeRed.copy(alpha = 0.75f),
                ThemeRed.copy(alpha = 0.35f),
                Color(0xFF121316).copy(alpha = 0.60f),
                Color.Black.copy(alpha = 0.90f),
            ),
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                ThemeRed.copy(alpha = 0.55f),
                ThemeRed.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.85f),
            ),
        )
    }

    // Border Rim Stroke
    val borderRefractionBrush = if (isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                ThemeRed,
                ThemeRed.copy(alpha = 0.7f),
                Color(0xFF2B2D33),
                Color.Black,
            ),
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                ThemeRed,
                ThemeRed.copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.8f),
                Color.White,
            ),
        )
    }

    // Gloss sheen top reflection
    val sheenBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.15f else 0.40f),
            Color.Transparent,
        ),
    )

    val barShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 20.dp,
        bottomEnd = 20.dp,
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        shape = barShape,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = glassBgBase, shape = barShape)
                .background(brush = refractionBrush, shape = barShape)
                .border(border = BorderStroke(1.5.dp, borderRefractionBrush), shape = barShape)
                .background(brush = sheenBrush, shape = barShape),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Status bar spacer so glass background extends flush behind system status bar
                Spacer(modifier = Modifier.statusBarsPadding())

                // App bar content area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Optional Back Button
                    if (onBackClick != null) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBackClick()
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                                .size(40.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back_red),
                                contentDescription = "Back",
                                tint = ThemeRed,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    // Title "AutoMekaniko" or custom title
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    } else {
                        val annotatedTitle = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                ),
                            ) {
                                append("Auto")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = ThemeRed,
                                    fontWeight = FontWeight.Bold,
                                ),
                            ) {
                                append("Mekaniko")
                            }
                        }

                        Text(
                            text = annotatedTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // Top-right Question Mark Tutorial Button (Independent Solid Red & White / Dark Card)
                    if (onTutorialClick != null) {
                        val cardBg = if (isDark) Color(0xFF22252A) else Color.White

                        Card(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTutorialClick()
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .size(38.dp)
                                .tutorialTarget(R.id.btnTutorial),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = cardBg,
                            ),
                            border = BorderStroke(1.5.dp, ThemeRed),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "?",
                                    color = ThemeRed,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Refraction Edge Beam
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(brush = borderRefractionBrush),
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun BrandedTopBarLightPreview() {
    AutoMekanikoTheme(darkTheme = false) {
        BrandedTopBar(onTutorialClick = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BrandedTopBarDarkPreview() {
    AutoMekanikoTheme(darkTheme = true) {
        BrandedTopBar(onTutorialClick = {})
    }
}
