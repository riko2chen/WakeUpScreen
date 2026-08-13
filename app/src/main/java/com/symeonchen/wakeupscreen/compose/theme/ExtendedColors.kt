package com.symeonchen.wakeupscreen.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The colours M3's [androidx.compose.material3.ColorScheme] has no slot for:
 * the header gradients and the success / notice / danger accents.
 *
 * They belong here rather than as constants referenced from each screen,
 * because every one of them needs a different value in dark mode and a
 * top-level `val` read straight from a composable cannot switch with the
 * theme. Reading them through [WakeUpTheme] is what keeps the two modes in
 * step.
 */
@Immutable
data class ExtendedColors(
    /** Toolbars, the home hero, the settings header. */
    val heroGradient: List<Color>,
    /** The home hero while something is wrong. */
    val errorGradient: List<Color>,
    /** Feature cards that sit on a filled indigo panel. */
    val accentGradient: List<Color>,
    val switchTrack: Color,
    val success: Color,
    val successContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val notice: Color,
    val noticeContainer: Color,
    val noticeStroke: Color,
)

val LightExtendedColors = ExtendedColors(
    heroGradient = listOf(GradientStart, GradientMid, GradientEnd),
    errorGradient = listOf(ErrorGradientStart, ErrorGradientMid, ErrorGradientEnd),
    accentGradient = listOf(IndigoDark, Indigo),
    switchTrack = PinkAccent,
    success = MintGreen,
    successContainer = MintGreenBg,
    danger = CoralRed,
    dangerContainer = CoralRedBg,
    notice = Amber,
    noticeContainer = AmberSoft,
    noticeStroke = AmberStroke,
)

/**
 * Darker, less saturated counterparts. The gradients drop to the deep end of
 * the indigo range so a header does not glow against a near-black background,
 * and the accents move to their lighter tints so they stay legible on it.
 */
val DarkExtendedColors = ExtendedColors(
    heroGradient = listOf(DarkGradientStart, DarkGradientMid, DarkGradientEnd),
    errorGradient = listOf(ErrorGradientStart, ErrorGradientMid, ErrorGradientEnd),
    accentGradient = listOf(IndigoDeep, IndigoDark),
    switchTrack = DarkPinkAccent,
    success = DarkMintGreen,
    successContainer = DarkMintGreenBg,
    danger = DarkCoralRed,
    dangerContainer = DarkCoralRedBg,
    notice = Amber,
    noticeContainer = DarkAmberSoft,
    noticeStroke = AmberStroke,
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

object WakeUpTheme {
    val colors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
