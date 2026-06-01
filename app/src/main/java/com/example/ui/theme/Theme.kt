package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SafetyGreen,
    onPrimary = SlateDarkest,
    primaryContainer = SafetyGreenDark,
    onPrimaryContainer = LightGreenAccent,
    secondary = LightGreenAccent,
    onSecondary = SlateDarkest,
    background = SlateDarkest,
    onBackground = SlateTextColor,
    surface = SlateDark,
    onSurface = SlateTextColor,
    surfaceVariant = SlateMedium,
    onSurfaceVariant = SlateLight,
    outline = SlateMedium
)

private val LightColorScheme = lightColorScheme(
    primary = SafetyGreenDark,
    onPrimary = SlateTextColor,
    primaryContainer = LightGreenAccent,
    onPrimaryContainer = SlateDarkest,
    secondary = SteelBlue,
    onSecondary = SlateTextColor,
    background = SteelBlueLight,
    onBackground = SlateDarkest,
    surface = SlateTextColor,
    onSurface = SlateDarkest,
    surfaceVariant = SteelBlueLight,
    onSurfaceVariant = SteelBlue,
    outline = SlateLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamicColor false to enforce our distinctive industrial visual identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
