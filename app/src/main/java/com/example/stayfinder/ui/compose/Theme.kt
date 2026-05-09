package com.example.stayfinder.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF8B2332)
private val LightScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF5D4037),
    onSecondary = Color.White,
    background = Color(0xFFFAF8F5),
    onBackground = Color(0xFF1C1B1C),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1C),
    surfaceVariant = Color(0xFFF3EEEA),
    onSurfaceVariant = Color(0xFF49454E),
    outline = Color(0xFFCAC4BF),
    outlineVariant = Color(0xFFE6E1DB)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFFFB3A8),
    onPrimary = Color(0xFF5F1414),
    primaryContainer = Color(0xFF8B2332),
    onPrimaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121218),
    onBackground = Color(0xFFE6E1E6),
    surface = Color(0xFF1C1C22),
    onSurface = Color(0xFFE6E1E6),
    surfaceVariant = Color(0xFF2A2A32),
    onSurfaceVariant = Color(0xFFCAC4CF),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

@Composable
fun StayFinderTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        content = content
    )
}
