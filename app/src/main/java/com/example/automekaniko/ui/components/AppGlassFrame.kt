package com.example.automekaniko.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.automekaniko.ui.theme.AutoMekanikoTheme
import com.example.automekaniko.ui.theme.LocalDarkTheme

/**
 * Reusable App Frame with solid background surface fill (glass refraction effect removed).
 */
@Composable
fun AppGlassFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = LocalDarkTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = if (isDark) Color(0xFF121316) else Color(0xFFF4F6F9)),
        content = content,
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun AppGlassFrameLightPreview() {
    AutoMekanikoTheme(darkTheme = false) {
        AppGlassFrame {
            Text("Light Mode App Frame", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppGlassFrameDarkPreview() {
    AutoMekanikoTheme(darkTheme = true) {
        AppGlassFrame {
            Text("Dark Mode App Frame", modifier = Modifier.align(Alignment.Center))
        }
    }
}
