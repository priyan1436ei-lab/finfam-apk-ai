package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextPrimary,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = PrimaryBlueGlow,
    secondary = SecondaryViolet,
    onSecondary = TextPrimary,
    secondaryContainer = DarkSurfaceGlow,
    onSecondaryContainer = SecondaryVioletGlow,
    tertiary = CyanAccent,
    onTertiary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = TextPrimary,
    outline = BorderGlassLight,
    outlineVariant = BorderGlass
)

@Composable
fun FinGuardTheme(
    darkTheme: Boolean = true, // FinGuard is dark-mode-first cyberpunk aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
