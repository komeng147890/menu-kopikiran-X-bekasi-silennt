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
    primary = EspressoPrimaryDark,
    onPrimary = CaramelDarkText,
    primaryContainer = CoffeeSurfaceVariantDark,
    onPrimaryContainer = CaramelLight,
    secondary = TerracottaSecondaryDark,
    onSecondary = CaramelDarkText,
    secondaryContainer = CoffeeSurfaceVariantDark,
    onSecondaryContainer = CinnamonContainer,
    tertiary = MatchaAccentDark,
    background = CoffeeBrownDark,
    surface = CoffeeSurfaceDark,
    surfaceVariant = CoffeeSurfaceVariantDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextSecondaryDark,
    outlineVariant = CoffeeSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = EspressoPrimary,
    onPrimary = CreamSurface,
    primaryContainer = CaramelLight,
    onPrimaryContainer = CaramelDarkText,
    secondary = TerracottaSecondary,
    onSecondary = CreamSurface,
    secondaryContainer = CinnamonContainer,
    onSecondaryContainer = CaramelDarkText,
    tertiary = MatchaAccent,
    background = CreamBackground,
    surface = CreamSurface,
    surfaceVariant = CreamSurfaceVariant,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = WarmBorder,
    outlineVariant = CreamSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our branded coffee palette by default
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
