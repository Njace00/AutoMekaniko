package com.example.automekaniko.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automekaniko.R
import com.example.automekaniko.tutorialTarget
import com.example.automekaniko.ui.theme.AutoMekanikoTheme
import com.example.automekaniko.ui.theme.LocalDarkTheme
import com.example.automekaniko.ui.theme.ThemeRed

enum class NavTab {
    HOME, SETTINGS
}

/**
 * Glassmorphic Bottom Navigation Bar featuring dynamic light refraction (Red to White in Light Mode, Red to Black in Dark Mode).
 */
@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalDarkTheme.current
    val isHome = selectedTab == NavTab.HOME

    // Base glass translucent fill (Light = Crystal White, Dark = Obsidian Glass)
    val glassBgBase = if (isDark) {
        Color(0xFF121316).copy(alpha = 0.88f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.82f)
    }

    // Refraction target color for inactive side
    val inactiveSideColor = if (isDark) Color(0xFF121316) else Color.White

    // Animated Refraction Colors based on active tab & theme
    val c0 by animateColorAsState(
        targetValue = if (isHome) ThemeRed.copy(alpha = if (isDark) 0.75f else 0.70f) else inactiveSideColor.copy(alpha = if (isDark) 0.60f else 0.85f),
        animationSpec = tween(durationMillis = 300),
        label = "c0"
    )
    val c1 by animateColorAsState(
        targetValue = if (isHome) ThemeRed.copy(alpha = 0.35f) else inactiveSideColor.copy(alpha = 0.45f),
        animationSpec = tween(durationMillis = 300),
        label = "c1"
    )
    val c2 by animateColorAsState(
        targetValue = if (isHome) inactiveSideColor.copy(alpha = 0.45f) else ThemeRed.copy(alpha = 0.35f),
        animationSpec = tween(durationMillis = 300),
        label = "c2"
    )
    val c3 by animateColorAsState(
        targetValue = if (isHome) inactiveSideColor.copy(alpha = if (isDark) 0.90f else 0.85f) else ThemeRed.copy(alpha = if (isDark) 0.75f else 0.70f),
        animationSpec = tween(durationMillis = 300),
        label = "c3"
    )

    val refractionBrush = Brush.horizontalGradient(
        colors = listOf(c0, c1, c2, c3)
    )

    // Animated Border Colors
    val borderInactive = if (isDark) Color(0xFF2B2D33) else Color.White

    val b0 by animateColorAsState(
        targetValue = if (isHome) ThemeRed else borderInactive,
        animationSpec = tween(durationMillis = 300),
        label = "b0"
    )
    val b1 by animateColorAsState(
        targetValue = if (isHome) ThemeRed.copy(alpha = 0.7f) else borderInactive.copy(alpha = 0.8f),
        animationSpec = tween(durationMillis = 300),
        label = "b1"
    )
    val b2 by animateColorAsState(
        targetValue = if (isHome) borderInactive.copy(alpha = 0.8f) else ThemeRed.copy(alpha = 0.7f),
        animationSpec = tween(durationMillis = 300),
        label = "b2"
    )
    val b3 by animateColorAsState(
        targetValue = if (isHome) borderInactive else ThemeRed,
        animationSpec = tween(durationMillis = 300),
        label = "b3"
    )

    val borderRefractionBrush = Brush.horizontalGradient(
        colors = listOf(b0, b1, b2, b3)
    )

    // Gloss sheen top reflection
    val sheenBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.15f else 0.40f),
            Color.Transparent,
        ),
    )

    val barShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
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
            // Top Refraction Edge Beam
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(brush = borderRefractionBrush),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Navigation items container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavItem(
                        iconRes = R.drawable.ic_home,
                        label = stringResource(R.string.nav_home),
                        isSelected = isHome,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTabSelected(NavTab.HOME)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .tutorialTarget(R.id.navHome)
                    )

                    NavItem(
                        iconRes = R.drawable.ic_settings,
                        label = stringResource(R.string.nav_settings),
                        isSelected = !isHome,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTabSelected(NavTab.SETTINGS)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .tutorialTarget(R.id.navSettings)
                    )
                }

                // System navigation bar spacer so glass background extends flush behind gesture bar
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun NavItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkTheme.current
    // Pure White for active tab over Red refraction background, dark/soft grey for unselected tab
    val activeColor = Color.White
    val inactiveColor = if (isDark) Color(0xFFA0A5B0) else Color(0xFF4A5056)

    val color by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 300),
        label = "navItemColor"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun BottomNavBarHomeLightPreview() {
    AutoMekanikoTheme(darkTheme = false) {
        BottomNavBar(selectedTab = NavTab.HOME, onTabSelected = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomNavBarHomeDarkPreview() {
    AutoMekanikoTheme(darkTheme = true) {
        BottomNavBar(selectedTab = NavTab.HOME, onTabSelected = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomNavBarSettingsDarkPreview() {
    AutoMekanikoTheme(darkTheme = true) {
        BottomNavBar(selectedTab = NavTab.SETTINGS, onTabSelected = {})
    }
}
