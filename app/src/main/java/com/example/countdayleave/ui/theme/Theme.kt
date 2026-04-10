package com.example.countdayleave.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val M3DarkColorScheme = darkColorScheme(
    primary          = AccentPurple,
    onPrimary        = Dark_TextPrimary,
    secondary        = AccentBlue,
    onSecondary      = Dark_BackgroundDeep,
    background       = Dark_BackgroundDark,
    onBackground     = Dark_TextPrimary,
    surface          = Dark_SurfaceCard,
    onSurface        = Dark_TextPrimary,
    surfaceVariant   = Dark_SurfaceElevated,
    onSurfaceVariant = Dark_TextSecondary,
    outline          = Dark_TextMuted,
    error            = Error
)

private val M3LightColorScheme = lightColorScheme(
    primary          = AccentPurple,
    onPrimary        = Light_BackgroundDark,
    secondary        = AccentBlue,
    onSecondary      = Light_BackgroundDark,
    background       = Light_BackgroundDark,
    onBackground     = Light_TextPrimary,
    surface          = Light_SurfaceCard,
    onSurface        = Light_TextPrimary,
    surfaceVariant   = Light_SurfaceElevated,
    onSurfaceVariant = Light_TextSecondary,
    outline          = Light_TextMuted,
    error            = Error
)

@Composable
fun CountDayLeaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) M3DarkColorScheme else M3LightColorScheme
    val appColors   = if (darkTheme) DarkAppColors     else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}

/** Shortcut để lấy AppColors trong bất kỳ Composable nào */
object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}