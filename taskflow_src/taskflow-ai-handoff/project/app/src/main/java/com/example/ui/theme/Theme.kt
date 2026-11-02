package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandIndigoDeep,
    secondary = AccentCyan,
    tertiary = AccentEmerald,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = true, // Default to true for the luxury dark enterprise UI
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
