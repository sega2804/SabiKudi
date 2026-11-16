package com.crypticsamsara.sabikudi.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Brand Colors - SabiKudi v10 "Balanced" Palette
val MintPrimary = Color(0xFF0F6B4A)
val MintLight = Color(0xFF2E8F68)
val MintTint = Color(0xFFE3F2EA)

val SplitPurple = Color(0xFF3B2A86)
val SplitPurpleTint = Color(0xFFECE9F7)

val StockGreen = Color(0xFF0F6B4A)
val StockGreenTint = Color(0xFFE3F2EA)

val AmberAccent = Color(0xFFF2B632)
val OnAmberDark = Color(0xFF1A1405)

val WarningGold = Color(0xFF7A5A10)
val WarningGoldBg = Color(0xFFFFF4D6)

val AlertPink = Color(0xFFC42B63)
val OnAlertWhite = Color(0xFFFFFFFF)

// Light Theme Surface & Text Tokens
val LightBg = Color(0xFFF7F8F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFEEF1EC)
val LightText = Color(0xFF151A18)
val LightMuted = Color(0xFF55605A)
val LightLine = Color(0xFFDCE3DD)
val LightOuter = Color(0xFFE9ECEA)

// Dark Theme Surface & Text Tokens
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF121614)
val DarkSurface2 = Color(0xFF1B201E)
val DarkText = Color(0xFFF2F5F3)
val DarkMuted = Color(0xFFA7B1AB)
val DarkLine = Color(0xFF2A312D)
val DarkOuter = Color(0xFF000000)

val DarkMintPrimary = Color(0xFF3DD598)
val DarkMintTint = Color(0xFF0F2A20)
val DarkSplitPurple = Color(0xFF9A8CFF)
val DarkSplitPurpleBg = Color(0xFF0E0A2E)
val DarkSplitPurpleTint = Color(0xFF1A1535)
val DarkWarningGold = Color(0xFFF2B632)
val DarkWarningGoldBg = Color(0xFF2E2406)
val DarkAlertPink = Color(0xFFFF6B9A)
val DarkOnAlertPink = Color(0xFF2A0714)

// Legacy aliases for theme compatibility
val NavyPrimary = MintPrimary
val TerracottaOrange = SplitPurple
val TerracottaTint = SplitPurpleTint
val TealGreen = StockGreen
val TealTint = StockGreenTint
val GoldAccent = WarningGold
val GoldTint = WarningGoldBg
val WarningYellow = WarningGoldBg

@Immutable
data class ExtendedColors(
    val navy: Color,         // Primary Mint Green (#0F6B4A / #3DD598)
    val terracotta: Color,   // Split Purple (#3B2A86 / #9A8CFF)
    val terracottaBg: Color, // Split Tint (#ECE9F7 / #1A1535)
    val teal: Color,         // Stock Green (#0F6B4A / #3DD598)
    val tealBg: Color,       // Stock Tint (#E3F2EA / #0F2A20)
    val amber: Color,        // Amber Accent (#F2B632)
    val gold: Color,         // Warning Gold (#7A5A10 / #F2B632)
    val goldBg: Color,       // Warning Gold Bg (#FFF4D6 / #2E2406)
    val alert: Color,        // Alert Pink (#C42B63 / #FF6B9A)
    val muted: Color,        // Muted Text (#55605A / #A7B1AB)
    val line: Color,         // Divider Line (#DCE3DD / #2A312D)
    val surface2: Color,     // Surface 2 (#EEF1EC / #1B201E)
    val warning: Color       // Warning Bg (#FFF4D6 / #2E2406)
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
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
}
