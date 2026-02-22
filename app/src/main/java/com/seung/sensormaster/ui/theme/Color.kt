package com.seung.sensormaster.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Primary Palette (Deep Teal / Cyan) ──
val PrimaryDark = Color(0xFF80CBC4)
val OnPrimaryDark = Color(0xFF003733)
val PrimaryContainerDark = Color(0xFF00504A)
val OnPrimaryContainerDark = Color(0xFF9EF2E9)

val PrimaryLight = Color(0xFF006A63)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF9EF2E9)
val OnPrimaryContainerLight = Color(0xFF00201D)

// ── Secondary Palette (Warm Amber) ──
val SecondaryDark = Color(0xFFFFB74D)
val OnSecondaryDark = Color(0xFF422C00)
val SecondaryContainerDark = Color(0xFF5F4100)
val OnSecondaryContainerDark = Color(0xFFFFDDB3)

val SecondaryLight = Color(0xFF7D5800)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFFFDDB3)
val OnSecondaryContainerLight = Color(0xFF271900)

// ── Tertiary (Soft Purple) ──
val TertiaryDark = Color(0xFFCFBCFF)
val OnTertiaryDark = Color(0xFF381E72)
val TertiaryContainerDark = Color(0xFF4F378A)
val OnTertiaryContainerDark = Color(0xFFE9DDFF)

val TertiaryLight = Color(0xFF6750A4)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFE9DDFF)
val OnTertiaryContainerLight = Color(0xFF22005D)

// ── Surface / Background (OLED optimized) ──
val SurfaceDark = Color(0xFF0D1117)
val OnSurfaceDark = Color(0xFFE1E3E4)
val SurfaceContainerDark = Color(0xFF1A1F26)
val SurfaceContainerHighDark = Color(0xFF242A32)

val SurfaceLight = Color(0xFFF8FAFA)
val OnSurfaceLight = Color(0xFF191C1D)
val SurfaceContainerLight = Color(0xFFECEFEF)
val SurfaceContainerHighLight = Color(0xFFE1E3E4)

// ── Error ──
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)

// ── Glassmorphism ──
val GlassWhite = Color(0x1AFFFFFF)
val GlassBorder = Color(0x33FFFFFF)
val GlassDarkOverlay = Color(0x4D000000)

// ── Neon Accents (다크 모드 최적화) ──
val NeonCyan = Color(0xFF00FFDD)
val NeonPurple = Color(0xFFBB86FC)
val NeonAmber = Color(0xFFFFD54F)
val NeonGreen = Color(0xFF69F0AE)
val NeonPink = Color(0xFFFF6090)
val NeonBlue = Color(0xFF448AFF)

// ── Neon Accents (라이트 모드용 — 약간 톤다운) ──
val NeonCyanLight = Color(0xFF00897B)
val NeonPurpleLight = Color(0xFF7C4DFF)
val NeonAmberLight = Color(0xFFFF8F00)
val NeonGreenLight = Color(0xFF00C853)
val NeonPinkLight = Color(0xFFD81B60)
val NeonBlueLight = Color(0xFF2962FF)

// ── 카테고리별 그라디언트 색상 쌍 ──
data class GradientPair(val start: Color, val end: Color)

@Immutable
data class ExtendedColors(
    val neonCyan: Color,
    val neonPurple: Color,
    val neonAmber: Color,
    val neonGreen: Color,
    val neonPink: Color,
    val neonBlue: Color,
    val glowAlpha: Float,
    val categoryGradients: List<GradientPair>
)

val DarkExtendedColors = ExtendedColors(
    neonCyan = NeonCyan,
    neonPurple = NeonPurple,
    neonAmber = NeonAmber,
    neonGreen = NeonGreen,
    neonPink = NeonPink,
    neonBlue = NeonBlue,
    glowAlpha = 0.6f,
    categoryGradients = listOf(
        GradientPair(Color(0xFF00B4D8), Color(0xFF0077B6)),  // 네비게이션
        GradientPair(Color(0xFF4CAF50), Color(0xFF1B5E20)),  // 환경
        GradientPair(Color(0xFFE040FB), Color(0xFF7C4DFF)),  // 물리
        GradientPair(Color(0xFF29B6F6), Color(0xFF0277BD)),  // 무선
        GradientPair(Color(0xFFEF5350), Color(0xFFE91E63)),  // 웰니스
        GradientPair(Color(0xFFFFB74D), Color(0xFFF57C00)),  // 시스템
    )
)

val LightExtendedColors = ExtendedColors(
    neonCyan = NeonCyanLight,
    neonPurple = NeonPurpleLight,
    neonAmber = NeonAmberLight,
    neonGreen = NeonGreenLight,
    neonPink = NeonPinkLight,
    neonBlue = NeonBlueLight,
    glowAlpha = 0.3f,
    categoryGradients = listOf(
        GradientPair(Color(0xFF4FC3F7), Color(0xFF0288D1)),
        GradientPair(Color(0xFF66BB6A), Color(0xFF2E7D32)),
        GradientPair(Color(0xFFCE93D8), Color(0xFF8E24AA)),
        GradientPair(Color(0xFF4FC3F7), Color(0xFF0288D1)),
        GradientPair(Color(0xFFEF9A9A), Color(0xFFE53935)),
        GradientPair(Color(0xFFFFCC80), Color(0xFFF57C00)),
    )
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }
