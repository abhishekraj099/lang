package com.example.lang.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Japanese Indigo (藍色) & Temple Gold (金色) Colors
private val JapaneseIndigoDark = Color(0xFF0F2942)
private val JapaneseIndigo = Color(0xFF1B3A52)
private val JapaneseIndigoMedium = Color(0xFF264653)
private val JapaneseIndigoLight = Color(0xFF3A5A7C)

private val TempleGoldBright = Color(0xFFFFD700)
private val TempleGold = Color(0xFFFFC107)
private val TempleGoldAccent = Color(0xFFFFE082)

// Supporting colors
private val ErrorRed = Color(0xFFFF6B6B)

private val DarkColorScheme = darkColorScheme(
    primary = TempleGoldBright,           // Main accent - Temple Gold
    onPrimary = JapaneseIndigoDark,       // Text on Temple Gold
    secondary = TempleGold,                // Secondary accent
    onSecondary = JapaneseIndigoDark,     // Text on secondary
    tertiary = TempleGoldAccent,           // Tertiary accent (lighter gold)
    onTertiary = JapaneseIndigoDark,      // Text on tertiary
    background = JapaneseIndigoDark,      // App background - Deep Indigo
    onBackground = Color.White,            // Text on background
    surface = JapaneseIndigo,              // Card/surface background - Medium Indigo
    onSurface = Color.White,               // Text on surface
    surfaceContainer = JapaneseIndigoMedium, // Slightly lighter surface
    onSurfaceVariant = Color(0xFFB0C4D4), // Muted text - lighter indigo-blue
    error = ErrorRed,                      // Error state
    onError = Color.White,                 // Text on error
    outline = JapaneseIndigoLight,        // Borders and outlines
    outlineVariant = Color(0xFF5A7A8D)    // Muted borders
)

private val LightColorScheme = lightColorScheme(
    primary = TempleGoldBright,
    onPrimary = JapaneseIndigoDark,
    secondary = TempleGold,
    onSecondary = JapaneseIndigoDark,
    tertiary = TempleGoldAccent,
    onTertiary = JapaneseIndigoDark,
    background = JapaneseIndigoDark,
    onBackground = Color.White,
    surface = JapaneseIndigo,
    onSurface = Color.White,
    surfaceVariant = JapaneseIndigoMedium,
    onSurfaceVariant = Color(0xFFB0C4D4),
    surfaceContainer = JapaneseIndigoMedium,
    surfaceContainerLow = Color(0xFF183145),
    surfaceContainerHigh = JapaneseIndigo,
    outline = JapaneseIndigoLight,
    outlineVariant = Color(0xFF5A7A8D),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun LangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to preserve the Japanese Indigo & Temple Gold design
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}