package com.telugustockpro.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TradingViewColors.Blue,
    onPrimary = TradingViewColors.TextPrimary,
    primaryContainer = TradingViewColors.BlueDark,
    onPrimaryContainer = TradingViewColors.TextPrimary,

    secondary = TradingViewColors.Green,
    onSecondary = TradingViewColors.TextPrimary,
    secondaryContainer = TradingViewColors.GreenBackground,
    onSecondaryContainer = TradingViewColors.Green,

    tertiary = TradingViewColors.Purple,
    onTertiary = TradingViewColors.TextPrimary,

    error = TradingViewColors.Red,
    onError = TradingViewColors.TextPrimary,
    errorContainer = TradingViewColors.RedBackground,
    onErrorContainer = TradingViewColors.Red,

    background = TradingViewColors.Background,
    onBackground = TradingViewColors.TextPrimary,

    surface = TradingViewColors.Surface,
    onSurface = TradingViewColors.TextPrimary,
    surfaceVariant = TradingViewColors.SurfaceVariant,
    onSurfaceVariant = TradingViewColors.TextSecondary,

    outline = TradingViewColors.Border,
    outlineVariant = TradingViewColors.BorderLight
)

@Composable
fun TeluguStockProTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TradingViewColors.Background.toArgb()
            window.navigationBarColor = TradingViewColors.Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TeluguStockTypography,
        content = content
    )
}
