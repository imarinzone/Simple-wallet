package com.example.ui.theme

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class ExtractedCardPalette(
    val dominantHex: String,
    val vibrantHex: String,
    val secondaryHex: String,
    val tertiaryHex: String,
    val mutedHex: String,
    val suggestedSwatches: List<String>
)

object BitmapPaletteExtractor {

    /**
     * Extracts dominant and vibrant Material You color tones from a card photo or image bitmap.
     * Uses downscaled pixel frequency and saturation/chroma weighting.
     */
    fun extractPalette(bitmap: Bitmap): ExtractedCardPalette {
        val sampleDim = 64
        val scaled = Bitmap.createScaledBitmap(bitmap, sampleDim, sampleDim, true)

        val pixels = IntArray(sampleDim * sampleDim)
        scaled.getPixels(pixels, 0, sampleDim, 0, 0, sampleDim, sampleDim)

        val colorBuckets = mutableMapOf<Int, Int>()

        for (pixel in pixels) {
            val a = AndroidColor.alpha(pixel)
            if (a < 180) continue

            val r = AndroidColor.red(pixel)
            val g = AndroidColor.green(pixel)
            val b = AndroidColor.blue(pixel)

            // Quantize to reduce noise and find dominant clusters (5 bits per channel)
            val qr = (r / 8) * 8
            val qg = (g / 8) * 8
            val qb = (b / 8) * 8

            // Filter out near-pure blacks and pure whites
            val brightness = (r + g + b) / 3
            if (brightness < 20 || brightness > 245) continue

            val quantizedColor = AndroidColor.rgb(qr, qg, qb)
            colorBuckets[quantizedColor] = (colorBuckets[quantizedColor] ?: 0) + 1
        }

        if (colorBuckets.isEmpty()) {
            return fallbackPalette()
        }

        // Score colors by frequency * saturation weighting
        val scoredColors = colorBuckets.map { (colorInt, count) ->
            val r = AndroidColor.red(colorInt) / 255f
            val g = AndroidColor.green(colorInt) / 255f
            val b = AndroidColor.blue(colorInt) / 255f
            val max = max(r, max(g, b))
            val min = min(r, min(g, b))
            val delta = max - min
            val saturation = if (delta == 0f) 0f else delta / max

            val score = count * (0.6f + saturation * 1.5f)
            colorInt to score
        }.sortedByDescending { it.second }

        val dominantInt = scoredColors.firstOrNull()?.first ?: AndroidColor.parseColor("#1E293B")

        // Find vibrant (high saturation, medium luminance)
        val vibrantInt = scoredColors.maxByOrNull { (colorInt, _) ->
            val r = AndroidColor.red(colorInt) / 255f
            val g = AndroidColor.green(colorInt) / 255f
            val b = AndroidColor.blue(colorInt) / 255f
            val max = max(r, max(g, b))
            val min = min(r, min(g, b))
            val delta = max - min
            val sat = if (delta == 0f) 0f else delta / max
            val lum = (max + min) / 2f
            sat * (1f - abs(lum - 0.5f) * 1.5f)
        }?.first ?: dominantInt

        // Find contrasting secondary (different hue)
        val vibrantHsl = FloatArray(3)
        AndroidColor.colorToHSV(vibrantInt, vibrantHsl)
        val targetSecondaryHue = (vibrantHsl[0] + 35f) % 360f

        val secondaryInt = scoredColors.minByOrNull { (colorInt, _) ->
            val hsv = FloatArray(3)
            AndroidColor.colorToHSV(colorInt, hsv)
            val hueDiff = abs(hsv[0] - targetSecondaryHue)
            val dist = if (hueDiff > 180f) 360f - hueDiff else hueDiff
            dist
        }?.first ?: AndroidColor.HSVToColor(floatArrayOf(targetSecondaryHue, 0.7f, 0.4f))

        // Tertiary complementary
        val tertiaryHue = (vibrantHsl[0] + 65f) % 360f
        val tertiaryInt = AndroidColor.HSVToColor(floatArrayOf(tertiaryHue, 0.8f, 0.75f))

        // Muted
        val mutedInt = scoredColors.minByOrNull { (colorInt, _) ->
            val r = AndroidColor.red(colorInt) / 255f
            val g = AndroidColor.green(colorInt) / 255f
            val b = AndroidColor.blue(colorInt) / 255f
            val max = max(r, max(g, b))
            val min = min(r, min(g, b))
            max - min
        }?.first ?: dominantInt

        val dominantHex = String.format("#%06X", 0xFFFFFF and dominantInt)
        val vibrantHex = String.format("#%06X", 0xFFFFFF and vibrantInt)
        val secondaryHex = String.format("#%06X", 0xFFFFFF and secondaryInt)
        val tertiaryHex = String.format("#%06X", 0xFFFFFF and tertiaryInt)
        val mutedHex = String.format("#%06X", 0xFFFFFF and mutedInt)

        val suggested = listOf(vibrantHex, dominantHex, secondaryHex, tertiaryHex, mutedHex).distinct()

        return ExtractedCardPalette(
            dominantHex = dominantHex,
            vibrantHex = vibrantHex,
            secondaryHex = secondaryHex,
            tertiaryHex = tertiaryHex,
            mutedHex = mutedHex,
            suggestedSwatches = suggested
        )
    }

    private fun fallbackPalette() = ExtractedCardPalette(
        dominantHex = "#1E293B",
        vibrantHex = "#3B82F6",
        secondaryHex = "#0EA5E9",
        tertiaryHex = "#F59E0B",
        mutedHex = "#334155",
        suggestedSwatches = listOf("#3B82F6", "#1E293B", "#0EA5E9", "#F59E0B", "#334155")
    )
}
