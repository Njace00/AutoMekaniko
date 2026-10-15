package com.example.automekaniko.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.automekaniko.ui.theme.AutoMekanikoTheme
import com.example.automekaniko.ui.theme.LocalDarkTheme
import com.example.automekaniko.ui.theme.ThemeRed

/**
 * Reusable Glass Effect App Frame with diagonal Red-to-White (Light Mode) or Red-to-Black (Dark Mode) refraction.
 * Refraction starts at Top-Left (Red) and extends to Bottom-Right (White or Black).
 */
@Composable
fun AppGlassFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalDarkTheme.current

    // Diagonal Glass Refraction Brush: Top-Left Red to Bottom-Right White (Light) or Black (Dark)
    val diagonalGlassBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                ThemeRed.copy(alpha = 0.50f),
                ThemeRed.copy(alpha = 0.20f),
                Color(0xFF1E2024).copy(alpha = 0.60f),
                Color(0xFF121316),
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite,
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                ThemeRed.copy(alpha = 0.35f),
                ThemeRed.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.70f),
                Color(0xFFF4F6F9),
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite,
        )
    }

    // Outer glass frame border
    val frameBorderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                ThemeRed.copy(alpha = 0.80f),
                ThemeRed.copy(alpha = 0.30f),
                Color(0xFF2B2D33),
                Color.Black,
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite,
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                ThemeRed.copy(alpha = 0.70f),
                ThemeRed.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.80f),
                Color.White,
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = if (isDark) Color(0xFF121316) else Color(0xFFF4F6F9))
            .background(brush = diagonalGlassBrush)
            .border(border = BorderStroke(1.5.dp, frameBorderBrush)),
        content = content,
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun AppGlassFrameLightPreview() {
    AutoMekanikoTheme(darkTheme = false) {
        AppGlassFrame {
            Text("Light Mode App Glass Frame", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppGlassFrameDarkPreview() {
    AutoMekanikoTheme(darkTheme = true) {
        AppGlassFrame {
            Text("Dark Mode App Glass Frame", modifier = Modifier.align(Alignment.Center))
        }
    }
}
