package com.symeonchen.wakeupscreen.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The roles M3's [androidx.compose.material3.ColorScheme] has no slot for:
 * success and notice, plus the home hero's gradient.
 *
 * They are built from the same tonal palettes as the scheme itself and read
 * at the same tones M3 uses for its own container roles, so they behave like
 * first-class roles rather than one-off accents. Reading them through
 * [WakeUpTheme] is what keeps light and dark in step; a top-level `val`
 * referenced straight from a composable cannot switch with the theme.
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val notice: Color,
    val onNotice: Color,
    val noticeContainer: Color,
    val onNoticeContainer: Color,
    /** The home hero, the one surface that stays a deep brand colour in both modes. */
    val heroGradient: List<Color>,
    val errorGradient: List<Color>,
    /** Content on top of either gradient. */
    val onHero: Color,
    /**
     * The hero's own accent, for the main switch. Identical in both modes on
     * purpose: the hero stays a deep gradient either way, so a track that
     * followed the theme would turn near-white in dark and stop reading as
     * "on" against a white thumb.
     */
    val heroAccent: Color,
)

val LightExtendedColors = ExtendedColors(
    success = LightSuccess,
    onSuccess = LightOnSuccess,
    successContainer = LightSuccessContainer,
    onSuccessContainer = LightOnSuccessContainer,
    notice = LightNotice,
    onNotice = LightOnNotice,
    noticeContainer = LightNoticeContainer,
    onNoticeContainer = LightOnNoticeContainer,
    heroGradient = listOf(LightHeroA, LightHeroB, LightHeroC),
    errorGradient = listOf(LightErrHeroA, LightErrHeroB, LightErrHeroC),
    onHero = Color.White,
    heroAccent = DarkTertiary,
)

val DarkExtendedColors = ExtendedColors(
    success = DarkSuccess,
    onSuccess = DarkOnSuccess,
    successContainer = DarkSuccessContainer,
    onSuccessContainer = DarkOnSuccessContainer,
    notice = DarkNotice,
    onNotice = DarkOnNotice,
    noticeContainer = DarkNoticeContainer,
    onNoticeContainer = DarkOnNoticeContainer,
    heroGradient = listOf(DarkHeroA, DarkHeroB, DarkHeroC),
    errorGradient = listOf(DarkErrHeroA, DarkErrHeroB, DarkErrHeroC),
    onHero = Color.White,
    heroAccent = DarkTertiary,
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

object WakeUpTheme {
    val colors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
