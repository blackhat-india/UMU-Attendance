package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = UmuAccentSky,
    onPrimary = UmuNavy,
    primaryContainer = UmuPrimary,
    onPrimaryContainer = Color.White,
    secondary = UmuAccentCyan,
    onSecondary = Color.Black,
    tertiary = UmuAmber,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkCard,
    onSurface = DarkText,
    surfaceVariant = DarkCardHover,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = UmuRed
)

private val LightColorScheme = lightColorScheme(
    primary = UmuPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = UmuPrimary,
    onPrimaryContainer = Color.White,
    secondary = UmuAccentCyan,
    onSecondary = Color.White,
    tertiary = UmuAmber,
    background = LightBg,
    onBackground = LightText,
    surface = LightCard,
    onSurface = LightText,
    surfaceVariant = LightCardHover,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    error = UmuRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

