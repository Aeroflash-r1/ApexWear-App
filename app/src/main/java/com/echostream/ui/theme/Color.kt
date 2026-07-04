package com.echostream.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

// ═══════════════════════════════════════════════
//  Brand accent — Coral / Amber (#FF6B4A family)
//  Brand-neutral naming for easy re-theming
// ═══════════════════════════════════════════════

val AccentPrimary = Color(0xFFFF6B4A)
val AccentLight = Color(0xFFFF8A6F)
val AccentDark = Color(0xFFE55A3D)
val AccentGlow = Color(0xFFFF8F7A)
val AccentAlpha10 = Color(0x1AFF6B4A)
val AccentAlpha20 = Color(0x33FF6B4A)
val AccentAlpha40 = Color(0x66FF6B4A)

// Secondary amber accent
val AmberPrimary = Color(0xFFFFB347)
val AmberLight = Color(0xFFFFC573)
val AmberDark = Color(0xFFE59E30)

// Background surfaces
val BgDark = Color(0xFF0A0A0C)
val BgSurface = Color(0xFF121212)
val SurfaceLight = Color(0xFF1A1A1A)
val SurfaceElevated = Color(0xFF242424)
val CardBg = Color(0xFF1E1E1E)
val CardBgLight = Color(0xFF2C2C2C)

// Text hierarchy
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB3B3B3)
val TextTertiary = Color(0xFF6A6A6A)

// Status colors
val ErrorRed = Color(0xFFE74C3C)
val SuccessGreen = Color(0xFF2ECC71)

// Overlays & scrims
val ScrimBlack20 = Color(0x33000000)
val ScrimBlack40 = Color(0x66000000)
val ScrimBlack60 = Color(0x99000000)
val WhiteOverlay5 = Color(0x0DFFFFFF)
val WhiteOverlay10 = Color(0x1AFFFFFF)
val WhiteOverlay20 = Color(0x33FFFFFF)

// Player screen animation colors
val PlayerBgStart = Color(0xFF0A0A0C)
val PlayerBgEnd = Color(0xFF1C1412) // Slight warm tint

// ═══════════════════════════════════════════════
//  M3 ColorScheme builder — derives all 28
//  tokens from the coral accent
// ═══════════════════════════════════════════════

fun coralColorScheme(): ColorScheme = ColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    primaryContainer = AccentPrimary.copy(alpha = 0.20f),
    onPrimaryContainer = AccentLight,
    secondary = AmberPrimary,
    onSecondary = TextPrimary,
    secondaryContainer = AmberPrimary.copy(alpha = 0.20f),
    onSecondaryContainer = AmberLight,
    tertiary = AccentDark,
    onTertiary = TextPrimary,
    tertiaryContainer = AccentDark.copy(alpha = 0.20f),
    onTertiaryContainer = AccentLight,
    background = BgDark,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = ErrorRed.copy(alpha = 0.20f),
    onErrorContainer = ErrorRed,
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF333333),
    inverseSurface = TextPrimary,
    inverseOnSurface = BgDark,
    inversePrimary = AccentPrimary,
    scrim = ScrimBlack60
)
