package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class PixelAccentColor(val label: String, val hex: String, val isDynamic: Boolean = false) {
    DYNAMIC("Material You", "#38BDF8", true),
    BLUE("Pixel Blue", "#4C8DF6"),
    CORAL("Coral", "#FF7D54"),
    VIOLET("Iris Violet", "#9A82DB"),
    MINT("Jade Mint", "#34D399"),
    AMBER("Solar Amber", "#FBBF24"),
    ROSE("Blossom Rose", "#F472B6")
}

val VaultFolioShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

fun getPixelColorScheme(darkTheme: Boolean, accent: PixelAccentColor): ColorScheme {
    val seed = Color(android.graphics.Color.parseColor(accent.hex))
    val hsl = MaterialYouThemeEngine.colorToHsl(seed)
    val h = hsl[0]
    val s = hsl[1].coerceIn(0.45f, 0.90f)
    val secH = (h + 24f) % 360f
    val terH = (h + 60f) % 360f

    return if (darkTheme) {
        val primary = MaterialYouThemeEngine.hslToColor(h, s, 0.76f)
        val onPrimary = MaterialYouThemeEngine.hslToColor(h, 0.45f, 0.14f)
        val primaryContainer = MaterialYouThemeEngine.hslToColor(h, (s * 0.75f).coerceIn(0.30f, 0.75f), 0.26f)
        val onPrimaryContainer = MaterialYouThemeEngine.hslToColor(h, 0.65f, 0.92f)

        val secondary = MaterialYouThemeEngine.hslToColor(secH, 0.38f, 0.74f)
        val onSecondary = MaterialYouThemeEngine.hslToColor(secH, 0.35f, 0.16f)
        val secondaryContainer = MaterialYouThemeEngine.hslToColor(secH, 0.32f, 0.25f)
        val onSecondaryContainer = MaterialYouThemeEngine.hslToColor(secH, 0.45f, 0.90f)

        val tertiary = MaterialYouThemeEngine.hslToColor(terH, 0.50f, 0.76f)
        val onTertiary = MaterialYouThemeEngine.hslToColor(terH, 0.40f, 0.16f)
        val tertiaryContainer = MaterialYouThemeEngine.hslToColor(terH, 0.42f, 0.26f)
        val onTertiaryContainer = MaterialYouThemeEngine.hslToColor(terH, 0.55f, 0.92f)

        val bg = MaterialYouThemeEngine.hslToColor(h, 0.16f, 0.07f)
        val surface = MaterialYouThemeEngine.hslToColor(h, 0.16f, 0.08f)
        val surfaceContainerLowest = MaterialYouThemeEngine.hslToColor(h, 0.16f, 0.05f)
        val surfaceContainerLow = MaterialYouThemeEngine.hslToColor(h, 0.16f, 0.11f)
        val surfaceContainer = MaterialYouThemeEngine.hslToColor(h, 0.18f, 0.13f)
        val surfaceContainerHigh = MaterialYouThemeEngine.hslToColor(h, 0.18f, 0.17f)
        val surfaceContainerHighest = MaterialYouThemeEngine.hslToColor(h, 0.20f, 0.22f)
        val surfaceVariant = MaterialYouThemeEngine.hslToColor(h, 0.20f, 0.20f)
        val onSurface = Color(0xFFEFF3F8)
        val onSurfaceVariant = MaterialYouThemeEngine.hslToColor(h, 0.16f, 0.76f)

        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = bg,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            outline = MaterialYouThemeEngine.hslToColor(h, 0.14f, 0.44f),
            outlineVariant = MaterialYouThemeEngine.hslToColor(h, 0.14f, 0.26f),
            error = Color(0xFFF2B8B5),
            onError = Color(0xFF601410),
            errorContainer = Color(0xFF8C1D18),
            onErrorContainer = Color(0xFFF9DEDC)
        )
    } else {
        val primary = MaterialYouThemeEngine.hslToColor(h, s, 0.40f)
        val onPrimary = Color.White
        val primaryContainer = MaterialYouThemeEngine.hslToColor(h, (s * 0.80f).coerceIn(0.35f, 0.85f), 0.90f)
        val onPrimaryContainer = MaterialYouThemeEngine.hslToColor(h, 0.65f, 0.14f)

        val secondary = MaterialYouThemeEngine.hslToColor(secH, 0.35f, 0.42f)
        val onSecondary = Color.White
        val secondaryContainer = MaterialYouThemeEngine.hslToColor(secH, 0.42f, 0.90f)
        val onSecondaryContainer = MaterialYouThemeEngine.hslToColor(secH, 0.45f, 0.16f)

        val tertiary = MaterialYouThemeEngine.hslToColor(terH, 0.48f, 0.40f)
        val onTertiary = Color.White
        val tertiaryContainer = MaterialYouThemeEngine.hslToColor(terH, 0.55f, 0.90f)
        val onTertiaryContainer = MaterialYouThemeEngine.hslToColor(terH, 0.55f, 0.15f)

        val bg = MaterialYouThemeEngine.hslToColor(h, 0.22f, 0.98f)
        val surface = MaterialYouThemeEngine.hslToColor(h, 0.22f, 0.98f)
        val surfaceContainerLowest = Color.White
        val surfaceContainerLow = MaterialYouThemeEngine.hslToColor(h, 0.24f, 0.96f)
        val surfaceContainer = MaterialYouThemeEngine.hslToColor(h, 0.24f, 0.94f)
        val surfaceContainerHigh = MaterialYouThemeEngine.hslToColor(h, 0.24f, 0.91f)
        val surfaceContainerHighest = MaterialYouThemeEngine.hslToColor(h, 0.24f, 0.88f)
        val surfaceVariant = MaterialYouThemeEngine.hslToColor(h, 0.24f, 0.90f)
        val onSurface = Color(0xFF191C20)
        val onSurfaceVariant = MaterialYouThemeEngine.hslToColor(h, 0.15f, 0.32f)

        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = bg,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            outline = MaterialYouThemeEngine.hslToColor(h, 0.12f, 0.50f),
            outlineVariant = MaterialYouThemeEngine.hslToColor(h, 0.16f, 0.82f),
            error = Color(0xFFB3261E),
            onError = Color.White,
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
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
        shapes = VaultFolioShapes,
        content = content
    )
}
