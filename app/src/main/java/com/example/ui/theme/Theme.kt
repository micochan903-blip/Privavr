package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VrDarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = ObsidianBlack,
    primaryContainer = SurfaceCardHighlight,
    onPrimaryContainer = CyanNeon,
    secondary = ElectricAzure,
    onSecondary = Color.White,
    tertiary = PrivacyGreen,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = SurfaceOledDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = Color(0xFFFF5252),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VrDarkColorScheme,
        typography = Typography,
        content = content
    )
}
