package com.nem.icons.utils

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PaletteExtractor {

    suspend fun extractFromBitmap(bitmap: Bitmap): List<Color> = withContext(Dispatchers.Default) {
        val palette = Palette.from(bitmap)
            .maximumColorCount(16)
            .generate()

        val colors = mutableListOf<Color>()

        // Add swatches in priority order
        palette.vibrantSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.lightVibrantSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.darkVibrantSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.mutedSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.lightMutedSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.darkMutedSwatch?.rgb?.let { colors.add(Color(it)) }

        // Fill remaining from all swatches
        palette.swatches.forEach { swatch ->
            val c = Color(swatch.rgb)
            if (colors.none { it == c } && colors.size < 12) {
                colors.add(c)
            }
        }

        // Deduplicate by hue proximity
        deduplicateColors(colors.take(12))
    }

    private fun deduplicateColors(colors: List<Color>): List<Color> {
        val result = mutableListOf<Color>()
        val hsv = FloatArray(3)
        colors.forEach { color ->
            android.graphics.Color.colorToHSV(
                android.graphics.Color.argb(
                    (color.alpha * 255).toInt(),
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                ), hsv
            )
            val hue = hsv[0]
            val isDuplicate = result.any { existing ->
                val existHsv = FloatArray(3)
                android.graphics.Color.colorToHSV(
                    android.graphics.Color.argb(
                        (existing.alpha * 255).toInt(),
                        (existing.red * 255).toInt(),
                        (existing.green * 255).toInt(),
                        (existing.blue * 255).toInt()
                    ), existHsv
                )
                kotlin.math.abs(existHsv[0] - hue) < 20f
            }
            if (!isDuplicate) result.add(color)
        }
        return result
    }

    /**
     * Scale-down bitmap for faster palette extraction without quality loss
     */
    fun scaleBitmapForPalette(src: Bitmap): Bitmap {
        val maxDim = 200
        if (src.width <= maxDim && src.height <= maxDim) return src
        val scale = maxDim.toFloat() / maxOf(src.width, src.height)
        return Bitmap.createScaledBitmap(
            src,
            (src.width * scale).toInt(),
            (src.height * scale).toInt(),
            true
        )
    }
}
