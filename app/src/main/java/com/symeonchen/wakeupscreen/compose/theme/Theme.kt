package com.symeonchen.wakeupscreen.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoLight,
    onPrimaryContainer = IndigoDark,
    secondary = PinkAccent,
    onSecondary = Color.White,
    tertiary = MintGreen,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    outline = LightDivider,
    error = CoralRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkIndigo,
    onPrimary = Color.Black,
    primaryContainer = IndigoDark,
    onPrimaryContainer = DarkIndigo,
    secondary = DarkPinkAccent,
    onSecondary = Color.Black,
    tertiary = DarkMintGreen,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    outline = DarkDivider,
    error = DarkCoralRed,
)

@Composable
fun WakeUpScreenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WakeUpTypography,
        content = content
    )
}
