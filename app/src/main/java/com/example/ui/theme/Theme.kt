package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = StealthBlack,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = CyberCyan,
    secondary = NeonOrange,
    onSecondary = StealthBlack,
    secondaryContainer = Color(0xFF2C1908),
    onSecondaryContainer = NeonOrangeLight,
    tertiary = HeadshotRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2E0911),
    onTertiaryContainer = Color(0xFFFF8FA3),
    background = StealthBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF1E2838),
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to gaming stealth dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
