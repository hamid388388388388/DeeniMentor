package com.deenimentor.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Islamic Green palette — Light
val GreenPrimary = Color(0xFF1B6B3A)
val GreenSecondary = Color(0xFF2E8B57)
val GreenLight = Color(0xFFE8F5E9)
val GoldAccent = Color(0xFFC9A84C)
val BackgroundLight = Color(0xFFF5F5F0)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextDark = Color(0xFF1A1A1A)
val TextMedium = Color(0xFF555555)
val ErrorRed = Color(0xFFB00020)

// Dark palette
val DarkBackground = Color(0xFF0F1F14)
val DarkSurface = Color(0xFF1A2E20)
val DarkCard = Color(0xFF1E3526)
val DarkTextPrimary = Color(0xFFE8F5E9)
val DarkTextSecondary = Color(0xFF9EC9A7)
val DarkGreenPrimary = Color(0xFF2ECC71)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    secondary = GreenSecondary,
    onSecondary = Color.White,
    tertiary = GoldAccent,
    background = BackgroundLight,
    surface = SurfaceWhite,
    onBackground = TextDark,
    onSurface = TextDark,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = Color(0xFF003314),
    secondary = Color(0xFF27AE60),
    onSecondary = Color.White,
    tertiary = GoldAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    error = Color(0xFFCF6679)
)

val LocalDarkMode = compositionLocalOf { false }

@Composable
fun DeeniMentorTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
