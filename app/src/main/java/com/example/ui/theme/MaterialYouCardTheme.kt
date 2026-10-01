package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.CardEntity
import kotlin.math.abs

/**
 * Material You (Material 3) Dynamic Tonal Palette for a single card.
 * Generated dynamically from the card's dominant color palette or image.
 */
data class CardMaterialYouTheme(
    val seedColor: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val ambientGlow: Color,
    val cardGradient: Brush,
    val borderBrush: Brush,
    val chipBaseColor: Color,
    val chipDetailColor: Color
)

object MaterialYouThemeEngine {

    /**
     * Converts a Color to HSL representation (Hue: 0..360, Saturation: 0..1, Lightness: 0..1)
     */
    fun colorToHsl(color: Color): FloatArray {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min

        val l = (max + min) / 2f
        val s = if (delta == 0f) 0f else delta / (1f - abs(2f * l - 1f))

        var h = when {
            delta == 0f -> 0f
            max == r -> ((g - b) / delta) % 6f
            max == g -> ((b - r) / delta) + 2f
            else -> ((r - g) / delta) + 4f
        } * 60f
        if (h < 0f) h += 360f

        return floatArrayOf(h, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
    }

    /**
     * Converts HSL to an Android Compose Color
     */
    fun hslToColor(h: Float, s: Float, l: Float, alpha: Float = 1f): Color {
        val normalizedHue = ((h % 360f) + 360f) % 360f
        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs((normalizedHue / 60f) % 2f - 1f))
        val m = l - c / 2f

        val (r1, g1, b1) = when ((normalizedHue / 60f).toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(
            red = (r1 + m).coerceIn(0f, 1f),
            green = (g1 + m).coerceIn(0f, 1f),
            blue = (b1 + m).coerceIn(0f, 1f),
            alpha = alpha.coerceIn(0f, 1f)
        )
    }

    fun parseHexColor(hex: String, fallback: Color = Color(0xFF1E293B)): Color {
        return try {
            val clean = hex.removePrefix("#")
            if (clean.length == 6 || clean.length == 8) {
                Color(android.graphics.Color.parseColor("#$clean"))
            } else fallback
        } catch (_: Exception) {
            fallback
        }
    }

    /**
     * Generates a complete Material You dynamic color scheme from a card entity
     */
    fun generateTheme(card: CardEntity, customSeed: Color? = null): CardMaterialYouTheme {
        val seed = customSeed ?: parseHexColor(card.themeColorHex, Color(0xFF2563EB))
        val secondarySeed = parseHexColor(card.gradientEndHex, seed)

        val hsl = colorToHsl(seed)
        val h = hsl[0]
        val s = hsl[1].coerceIn(0.35f, 0.95f)
        val l = hsl[2]

        // Material 3 Dynamic Tonal mapping
        val primary = hslToColor(h, s, 0.74f)
        val onPrimary = hslToColor(h, 0.30f, 0.12f)
        val primaryContainer = hslToColor(h, (s * 0.90f).coerceIn(0.30f, 0.85f), 0.32f)
        val onPrimaryContainer = hslToColor(h, 0.35f, 0.92f)

        // Harmonic Secondary (subtly shifted hue + restrained saturation)
        val secondaryH = (h + 18f) % 360f
        val secondary = hslToColor(secondaryH, (s * 0.70f).coerceIn(0.25f, 0.65f), 0.68f)
        val onSecondary = hslToColor(secondaryH, 0.25f, 0.14f)
        val secondaryContainer = hslToColor(secondaryH, (s * 0.60f).coerceIn(0.20f, 0.60f), 0.26f)
        val onSecondaryContainer = hslToColor(secondaryH, 0.30f, 0.90f)

        // Harmonic Tertiary (complementary accent for metallic chip & badges)
        val tertiaryH = (h + 55f) % 360f
        val tertiary = hslToColor(tertiaryH, (s * 0.85f).coerceIn(0.40f, 0.85f), 0.76f)
        val onTertiary = hslToColor(tertiaryH, 0.30f, 0.15f)
        val tertiaryContainer = hslToColor(tertiaryH, (s * 0.75f).coerceIn(0.30f, 0.75f), 0.34f)
        val onTertiaryContainer = hslToColor(tertiaryH, 0.40f, 0.92f)

        // Dark-tinted Dynamic Surfaces
        val surface = hslToColor(h, 0.18f, 0.08f)
        val surfaceContainer = hslToColor(h, 0.20f, 0.14f)
        val surfaceContainerHigh = hslToColor(h, 0.24f, 0.20f)
        val onSurface = Color(0xFFF1F5F9)
        val onSurfaceVariant = hslToColor(h, 0.20f, 0.75f)

        val outline = hslToColor(h, 0.25f, 0.40f).copy(alpha = 0.45f)
        val outlineVariant = hslToColor(h, 0.20f, 0.25f).copy(alpha = 0.35f)

        // Ambient colored glow for realistic lighting in stack
        val ambientGlow = primary.copy(alpha = 0.42f)

        // Rich 4-stop dynamic luxury card gradient
        val secondaryHsl = colorToHsl(secondarySeed)
        val endH = if (secondarySeed != seed) secondaryHsl[0] else (h + 25f) % 360f

        val cardGradient = Brush.linearGradient(
            colors = listOf(
                primaryContainer,
                hslToColor(endH, s * 0.85f, 0.24f),
                hslToColor(h, s * 0.70f, 0.15f),
                surfaceContainer
            ),
            start = Offset(0f, 0f),
            end = Offset(850f, 540f)
        )

        // Dynamic specular border gradient
        val borderBrush = Brush.linearGradient(
            colors = listOf(
                primary.copy(alpha = 0.60f),
                tertiary.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.10f),
                primary.copy(alpha = 0.35f)
            ),
            start = Offset(0f, 0f),
            end = Offset(700f, 400f)
        )

        // Harmonic metallic chip colors
        val chipBaseColor = hslToColor(tertiaryH, 0.65f, 0.58f)
        val chipDetailColor = hslToColor(tertiaryH, 0.45f, 0.38f)

        return CardMaterialYouTheme(
            seedColor = seed,
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
            surface = surface,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            ambientGlow = ambientGlow,
            cardGradient = cardGradient,
            borderBrush = borderBrush,
            chipBaseColor = chipBaseColor,
            chipDetailColor = chipDetailColor
        )
    }
}
