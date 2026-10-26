package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NexBlueLight,
    onPrimary = Color(0xFF082F49),
    primaryContainer = NexBlueContainerDark,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = NexPurpleLight,
    onSecondary = Color(0xFF2E1065),
    secondaryContainer = NexPurpleContainerDark,
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = NexBluePrimary,
    onPrimary = Color.White,
    primaryContainer = NexBlueContainerLight,
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = NexPurpleSecondary,
    onSecondary = Color.White,
    secondaryContainer = NexPurpleContainerLight,
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = Color(0xFF7F1D1D),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

enum class AppThemeMode {
    LIGHT, DARK, SYSTEM
}

@Composable
fun NexEarnTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    NexEarnTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        content = content
    )
}
