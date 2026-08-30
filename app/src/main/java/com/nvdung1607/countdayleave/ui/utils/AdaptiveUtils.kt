package com.nvdung1607.countdayleave.ui.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthClass {
    COMPACT,   // < 600dp (standard phone portrait)
    MEDIUM,    // 600dp..839dp (foldables, small tablets, phone landscape)
    EXPANDED   // >= 840dp (large tablets, desktop)
}

enum class WindowHeightClass {
    COMPACT,   // < 480dp (phone landscape)
    MEDIUM,    // 480dp..899dp (phone portrait, tablets)
    EXPANDED   // >= 900dp (tall screens)
}

data class AdaptiveLayoutInfo(
    val widthClass: WindowWidthClass,
    val heightClass: WindowHeightClass,
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val isLandscape: Boolean,
    val fontScale: Float,
    val maxContentWidth: Dp
) {
    val isCompactWidth: Boolean get() = widthClass == WindowWidthClass.COMPACT
    val isMediumOrExpandedWidth: Boolean get() = widthClass != WindowWidthClass.COMPACT
    val isCompactHeight: Boolean get() = heightClass == WindowHeightClass.COMPACT
}

@Composable
fun rememberAdaptiveLayoutInfo(): AdaptiveLayoutInfo {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val fontScale = density.fontScale

    val widthClass = when {
        configuration.screenWidthDp < 600 -> WindowWidthClass.COMPACT
        configuration.screenWidthDp < 840 -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.EXPANDED
    }

    val heightClass = when {
        configuration.screenHeightDp < 480 -> WindowHeightClass.COMPACT
        configuration.screenHeightDp < 900 -> WindowHeightClass.MEDIUM
        else -> WindowHeightClass.EXPANDED
    }

    val maxContentWidth = when (widthClass) {
        WindowWidthClass.COMPACT -> Dp.Unspecified
        WindowWidthClass.MEDIUM -> 640.dp
        WindowWidthClass.EXPANDED -> 720.dp
    }

    return remember(screenWidthDp, screenHeightDp, fontScale) {
        AdaptiveLayoutInfo(
            widthClass = widthClass,
            heightClass = heightClass,
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp,
            isLandscape = isLandscape,
            fontScale = fontScale,
            maxContentWidth = maxContentWidth
        )
    }
}
