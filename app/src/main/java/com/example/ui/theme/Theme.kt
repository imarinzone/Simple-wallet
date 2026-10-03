package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class PixelAccentColor(val label: String, val hex: String, val isDynamic: Boolean = false) {
    DYNAMIC("Multi-color", "#38BDF8", true),
    BLUE("Blue", "#1A73E8"),
    CORAL("Coral", "#F97316"),
    VIOLET("Violet", "#7C3AED"),
    MINT("Mint", "#10B981"),
    AMBER("Amber", "#F59E0B"),
    ROSE("Rose", "#EC4899")
}

fun getPixelColorScheme(darkTheme: Boolean, accent: PixelAccentColor): ColorScheme {
    val accentColor = Color(android.graphics.Color.parseColor(accent.hex))
    return if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = Color(0xFF0F172A),
            primaryContainer = accentColor.copy(alpha = 0.22f),
            onPrimaryContainer = Color.White,
            secondary = accentColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF1E293B),
            onSecondaryContainer = Color.White,
            background = Color(0xFF0F172A),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF1E293B),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF334155),
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF475569)
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.14f),
            onPrimaryContainer = accentColor,
            secondary = accentColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF1F5F9),
            onSecondaryContainer = Color(0xFF0F172A),
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color.White,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun VaultFolioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: PixelAccentColor = PixelAccentColor.BLUE,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        accentColor.isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getPixelColorScheme(darkTheme, accentColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
