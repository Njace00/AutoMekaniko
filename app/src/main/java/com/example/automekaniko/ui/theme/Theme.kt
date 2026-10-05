package com.example.automekaniko.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ThemeRed,
    onPrimary = Color.White,
    primaryContainer = ThemeRedLight,
    onPrimaryContainer = ThemeRed,
    secondary = TextSecondary,
    onSecondary = Color.White,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = ThemeRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF330808),
    onPrimaryContainer = Color(0xFFFFB4AB),
    secondary = TextSecondary,
    onSecondary = Color.White,
    background = Color(0xFF121316),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1E2024),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF2B2D33),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF4E2020)
)

@Composable
fun AutoMekanikoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
