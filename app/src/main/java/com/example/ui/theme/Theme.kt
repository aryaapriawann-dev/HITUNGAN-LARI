package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AthleticDarkColorScheme = darkColorScheme(
    primary = SportVolt,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E2D1A),
    onPrimaryContainer = SportVolt,

    secondary = SportCyan,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF0E3A52),
    onSecondaryContainer = Color(0xFFBAE6FD),

    tertiary = SportOrange,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF5A270B),
    onTertiaryContainer = Color(0xFFFFEDD5),

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = DarkOutline,
    outlineVariant = Color(0xFF1E293B),

    error = SportRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We default to sleek athletic dark theme for running apps
    dynamicColor: Boolean = false, // Keep athletic branding consistent
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AthleticDarkColorScheme,
        typography = Typography,
        content = content
    )
}
