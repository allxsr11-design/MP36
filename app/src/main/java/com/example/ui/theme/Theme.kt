package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppleBlueDark,
    onPrimary = Color.White,
    primaryContainer = AppleBlueDark.copy(alpha = 0.2f),
    onPrimaryContainer = AppleBlueDark,
    secondary = AppleIndigoDark,
    onSecondary = Color.White,
    tertiary = AppleRedDark,
    onTertiary = Color.White,
    background = IosDarkBackground,
    onBackground = IosDarkTextPrimary,
    surface = IosDarkSurface,
    onSurface = IosDarkTextPrimary,
    surfaceVariant = IosDarkSurfaceSecondary,
    onSurfaceVariant = IosDarkTextSecondary,
    outline = IosDarkSeparator,
    outlineVariant = IosDarkSeparator.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = AppleBlue,
    onPrimary = Color.White,
    primaryContainer = AppleBlue.copy(alpha = 0.12f),
    onPrimaryContainer = AppleBlue,
    secondary = AppleIndigo,
    onSecondary = Color.White,
    tertiary = AppleRed,
    onTertiary = Color.White,
    background = IosLightBackground,
    onBackground = IosLightTextPrimary,
    surface = IosLightSurface,
    onSurface = IosLightTextPrimary,
    surfaceVariant = IosLightSurfaceSecondary,
    onSurfaceVariant = IosLightTextSecondary,
    outline = IosLightSeparator,
    outlineVariant = IosLightSeparator.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
