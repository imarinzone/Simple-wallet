package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Gold400,
    onPrimary = Color(0xFF2C1904),
    primaryContainer = Gold600,
    onPrimaryContainer = Color(0xFFFFE0A3),
    secondary = AmberBronze,
    onSecondary = Color.White,
    secondaryContainer = DeepLeather,
    onSecondaryContainer = Gold400,
    tertiary = SecureGreen,
    onTertiary = Color.Black,
    background = ObsidianDark,
    onBackground = WarmOffWhite,
    surface = ObsidianSurface,
    onSurface = WarmOffWhite,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = SubtitleMuted,
    outline = ObsidianBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Gold600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFDE4B0),
    onPrimaryContainer = Color(0xFF331F04),
    secondary = SaddleBrown,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF6E8DF),
    onSecondaryContainer = Color(0xFF3C2010),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = Color(0xFFFBF8F4),
    onBackground = Color(0xFF201A17),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF201A17),
    surfaceVariant = Color(0xFFF0EAE2),
    onSurfaceVariant = Color(0xFF5D524A),
    outline = Color(0xFFD8CEC4)
)

@Composable
fun VaultFolioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default to curated luxury palette
    content: @Composable () -> Unit
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
