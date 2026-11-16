package com.crypticsamsara.sabikudi.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = LightSurface,
    primaryContainer = MintTint,
    onPrimaryContainer = MintPrimary,
    secondary = SplitPurple,
    onSecondary = LightSurface,
    secondaryContainer = SplitPurpleTint,
    onSecondaryContainer = SplitPurple,
    tertiary = StockGreen,
    onTertiary = LightSurface,
    tertiaryContainer = StockGreenTint,
    onTertiaryContainer = StockGreen,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightMuted,
    outline = LightLine
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkMintPrimary,
    onPrimary = DarkBg,
    primaryContainer = DarkMintTint,
    onPrimaryContainer = DarkMintPrimary,
    secondary = DarkSplitPurple,
    onSecondary = DarkBg,
    secondaryContainer = DarkSplitPurpleBg,
    onSecondaryContainer = DarkSplitPurple,
    tertiary = DarkMintPrimary,
    onTertiary = DarkBg,
    tertiaryContainer = DarkMintTint,
    onTertiaryContainer = DarkMintPrimary,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkMuted,
    outline = DarkLine
)

private val LightExtended = ExtendedColors(
    navy = MintPrimary,
    terracotta = SplitPurple,
    terracottaBg = SplitPurpleTint,
    teal = StockGreen,
    tealBg = StockGreenTint,
    amber = AmberAccent,
    gold = WarningGold,
    goldBg = WarningGoldBg,
    alert = AlertPink,
    muted = LightMuted,
    line = LightLine,
    surface2 = LightSurface2,
    warning = WarningGoldBg
)

private val DarkExtended = ExtendedColors(
    navy = DarkMintPrimary,
    terracotta = DarkSplitPurple,
    terracottaBg = DarkSplitPurpleBg,
    teal = DarkMintPrimary,
    tealBg = DarkMintTint,
    amber = AmberAccent,
    gold = DarkWarningGold,
    goldBg = DarkWarningGoldBg,
    alert = DarkAlertPink,
    muted = DarkMuted,
    line = DarkLine,
    surface2 = DarkSurface2,
    warning = DarkWarningGoldBg
)

@Composable
fun SabiKudiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtended else LightExtended

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object SabiKudiTheme {
    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
