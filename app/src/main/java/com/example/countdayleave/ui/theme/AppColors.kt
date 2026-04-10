package com.example.countdayleave.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tập hợp màu sắc thay đổi theo theme (dark/light).
 * Truy cập qua: LocalAppColors.current
 */
@Immutable
data class AppColors(
    val backgroundDeep: Color,
    val backgroundDark: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    // Shared accents (không đổi theo theme)
    val accentPurple: Color       = AccentPurple,
    val accentBlue: Color         = AccentBlue,
    val accentPurpleLight: Color  = AccentPurpleLight,
    val gradientStart: Color      = GradientStart,
    val gradientEnd: Color        = GradientEnd,
    val success: Color            = Success,
    val warning: Color            = Warning,
    val error: Color              = Error
)

val DarkAppColors = AppColors(
    backgroundDeep  = Dark_BackgroundDeep,
    backgroundDark  = Dark_BackgroundDark,
    surfaceCard     = Dark_SurfaceCard,
    surfaceElevated = Dark_SurfaceElevated,
    textPrimary     = Dark_TextPrimary,
    textSecondary   = Dark_TextSecondary,
    textMuted       = Dark_TextMuted
)

val LightAppColors = AppColors(
    backgroundDeep  = Light_BackgroundDeep,
    backgroundDark  = Light_BackgroundDark,
    surfaceCard     = Light_SurfaceCard,
    surfaceElevated = Light_SurfaceElevated,
    textPrimary     = Light_TextPrimary,
    textSecondary   = Light_TextSecondary,
    textMuted       = Light_TextMuted
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
