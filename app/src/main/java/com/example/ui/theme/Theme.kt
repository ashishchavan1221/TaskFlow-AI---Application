package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1), // Brand Indigo
    secondary = Color(0xFF38BDF8), // Sky Cyan
    tertiary = Color(0xFF10B981), // Emerald
    background = Color(0xFF0B1120), // Deep Dark Slate
    surface = Color(0xFF131D33), // Dark Navy Surface
    surfaceVariant = Color(0xFF1E293B), // Dark Slate Card
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF26334D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5), // Indigo
    secondary = Color(0xFF0284C7), // Sky Blue
    tertiary = Color(0xFF059669), // Emerald
    background = Color(0xFFF1F5F9), // Crisp clean light background
    surface = Color(0xFFFFFFFF), // Pure White cards
    surfaceVariant = Color(0xFFE2E8F0), // Light slate container
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A), // Deep Navy text
    onSurface = Color(0xFF0F172A), // Deep Navy text
    onSurfaceVariant = Color(0xFF475569), // Slate secondary text
    outline = Color(0xFFCBD5E1) // Clean border
)

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
