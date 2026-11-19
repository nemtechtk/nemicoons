
package com.nem.icons.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import android.graphics.Color as AndroidColor
import com.nem.icons.domain.model.IconStyle
import com.nem.icons.domain.model.StyleMode
import kotlin.math.*

/**
 * Core icon processing engine.
 * Applies ColorOS-style two-tone gradient recoloring while preserving
 * original logos, outlines, shadows, highlights, and fine details.
 */
object IconProcessor {

    private const val ICON_SIZE = 192

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, ICON_SIZE, ICON_SIZE)
        drawable.draw(canvas)
        return bitmap
    }

    fun applyStyle(original: Bitmap, style: IconStyle): Bitmap {
        val src = ensureSize(original, ICON_SIZE)
        return when (style.mode) {
            StyleMode.GRADIENT -> applyGradientStyle(src, style)
            StyleMode.TINT -> applyTintStyle(src, style)
            StyleMode.HUE_SHIFT -> applyHueShiftStyle(src, style)
        }.let { applyColorAdjustments(it, style) }
    }

    private fun applyGradientStyle(src: Bitmap, style: IconStyle): Bitmap {
        val w = src.width
        val h = src.height

        val alphaLayer = extractAlpha(src)
        val luminanceMap = extractLuminance(src)
        val edgeMap = detectEdges(src)
        val highlightMap = extractHighlights(src)
        val shadowMap = extractShadows(src)

        val gradientBitmap = buildGradient(w, h, style.color1, style.color2, style.gradientAngle)

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(w * h)
        val gradPixels = IntArray(w * h)
        val alphaPixels = IntArray(w * h)
        val resultPixels = IntArray(w * h)

        src.getPixels(srcPixels, 0, w, 0, 0, w, h)
        gradientBitmap.getPixels(gradPixels, 0, w, 0, 0, w, h)
        alphaLayer.getPixels(alphaPixels, 0, w, 0, 0, w, h)

        for (i in srcPixels.indices) {
            val srcAlpha = AndroidColor.alpha(srcPixels[i])
            if (srcAlpha < 10) {
                resultPixels[i] = 0
                continue
            }

            val srcR = AndroidColor.red(srcPixels[i])
            val srcG = AndroidColor.green(srcPixels[i])
            val srcB = AndroidColor.blue(srcPixels[i])

            val lum = luminanceMap[i]
            val edge = edgeMap[i]
            val highlight = highlightMap[i]
            val shadow = shadowMap[i]

            val gradR = AndroidColor.red(gradPixels[i])
            val gradG = AndroidColor.green(gradPixels[i])
            val gradB = AndroidColor.blue(gradPixels[i])

            val intensity = style.intensity

            val edgeWeight = edge * 0.6f
            val highlightWeight = highlight * 0.8f
            val shadowWeight = shadow * 0.5f
            val preserveWeight = (edgeWeight + highlightWeight + shadowWeight).coerceAtMost(1f)
            val gradWeight = (1f - preserveWeight) * intensity

            val finalR = (
                srcR * preserveWeight +
                gradR * gradWeight +
                srcR * (1f - preserveWeight - gradWeight)
            ).toInt()

            val finalG = (
                srcG * preserveWeight +
                gradG * gradWeight +
                srcG * (1f - preserveWeight - gradWeight)
            ).toInt()

            val finalB = (
                srcB * preserveWeight +
                gradB * gradWeight +
                srcB * (1f - preserveWeight - gradWeight)
            ).toInt()

            val shimmer = (highlight * 40 * intensity).toInt()

            resultPixels[i] = AndroidColor.argb(
                srcAlpha,
                (finalR + shimmer).coerceIn(0, 255),
                (finalG + shimmer).coerceIn(0, 255),
                (finalB + shimmer).coerceIn(0, 255)
            )
        }

        result.setPixels(resultPixels, 0, w, 0, 0, w, h)
        gradientBitmap.recycle()
        alphaLayer.recycle()
        return result
    }

    private fun applyTintStyle(src: Bitmap, style: IconStyle): Bitmap {
        val w = src.width
        val h = src.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, null)

        val c1 = style.color1
        val paint = Paint().apply {
            colorFilter = PorterDuffColorFilter(
                AndroidColor.argb(
                    (style.intensity * 180).toInt(),
                    (c1.red * 255).toInt(),
                    (c1.green * 255).toInt(),
                    (c1.blue * 255).toInt()
                ),
                PorterDuff.Mode.SRC_ATOP
            )
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    private fun applyHueShiftStyle(src: Bitmap, style: IconStyle): Bitmap {
        val w = src.width
        val h = src.height
        val hsv1 = FloatArray(3)
        android.graphics.Color.colorToHSV(style.color1.toArgb(), hsv1)
        val targetHue = hsv1[0]

        val srcPixels = IntArray(w * h)
        src.getPixels(srcPixels, 0, w, 0, 0, w, h)
        val result = IntArray(w * h)
        val hsv = FloatArray(3)

        for (i in srcPixels.indices) {
            val a = AndroidColor.alpha(srcPixels[i])
            if (a < 10) {
                result[i] = 0
                continue
            }

            android.graphics.Color.colorToHSV(srcPixels[i], hsv)
            if (hsv[1] > 0.1f) {
                hsv[0] = targetHue + (hsv[0] - targetHue) * (1f - style.intensity)
                hsv[1] = (hsv[1] * (0.5f + style.intensity * 0.5f)).coerceIn(0f, 1f)
            }
            result[i] = android.graphics.Color.HSVToColor(a, hsv)
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(result, 0, w, 0, 0, w, h)
        return out
    }

    private fun applyColorAdjustments(src: Bitmap, style: IconStyle): Bitmap {
        if (style.brightness == 0f && style.contrast == 0f && style.saturation == 0f) return src

        val cm = ColorMatrix()

        if (style.saturation != 0f) {
            val sat = ColorMatrix()
            sat.setSaturation(1f + style.saturation)
            cm.postConcat(sat)
        }

        if (style.brightness != 0f || style.contrast != 0f) {
            val b = style.brightness * 255f
            val c = style.contrast + 1f
            val bc = ColorMatrix(floatArrayOf(
                c, 0f, 0f, 0f, b,
                0f, c, 0f, 0f, b,
                0f, 0f, c, 0f, b,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(bc)
        }

        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    private fun buildGradient(
        w: Int,
        h: Int,
        c1: Color,
        c2: Color,
        angleDeg: Float
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rad = Math.toRadians(angleDeg.toDouble())
        val dx = cos(rad).toFloat()
        val dy = sin(rad).toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val half = sqrt((w * dx).pow(2) + (h * dy).pow(2)) / 2f
        val x0 = cx - dx * half
        val y0 = cy - dy * half
        val x1 = cx + dx * half
        val y1 = cy + dy * half

        val paint = Paint().apply {
            shader = LinearGradient(
                x0, y0, x1, y1,
                c1.toArgb(), c2.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        return bitmap
    }

    private fun extractLuminance(src: Bitmap): FloatArray {
        val pixels = IntArray(src.width * src.height)
        src.getPixels(pixels, 0, src.width, 0, 0, src.width, src.height)
        return FloatArray(pixels.size) { i ->
            val r = AndroidColor.red(pixels[i]) / 255f
            val g = AndroidColor.green(pixels[i]) / 255f
            val b = AndroidColor.blue(pixels[i]) / 255f
            0.2126f * r + 0.7152f * g + 0.0722f * b
        }
    }

    private fun detectEdges(src: Bitmap): FloatArray {
        val w = src.width
        val h = src.height
        val lum = extractLuminance(src)
        val edges = FloatArray(w * h)

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val gx = -lum[(y - 1) * w + (x - 1)] + lum[(y - 1) * w + (x + 1)] +
                    -2 * lum[y * w + (x - 1)] + 2 * lum[y * w + (x + 1)] +
                    -lum[(y + 1) * w + (x - 1)] + lum[(y + 1) * w + (x + 1)]

                val gy = -lum[(y - 1) * w + (x - 1)] - 2 * lum[(y - 1) * w + x] -
                    lum[(y - 1) * w + (x + 1)] +
                    lum[(y + 1) * w + (x - 1)] + 2 * lum[(y + 1) * w + x] +
                    lum[(y + 1) * w + (x + 1)]

                edges[y * w + x] = sqrt(gx * gx + gy * gy).coerceIn(0f, 1f)
            }
        }
        return edges
    }

    private fun extractHighlights(src: Bitmap): FloatArray {
        val lum = extractLuminance(src)
        return FloatArray(lum.size) { i ->
            ((lum[i] - 0.75f) / 0.25f).coerceIn(0f, 1f)
        }
    }

    private fun extractShadows(src: Bitmap): FloatArray {
        val lum = extractLuminance(src)
        return FloatArray(lum.size) { i ->
            ((0.25f - lum[i]) / 0.25f).coerceIn(0f, 1f)
        }
    }

    private fun extractAlpha(src: Bitmap): Bitmap {
        val alpha = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(alpha)
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return alpha
    }

    private fun ensureSize(src: Bitmap, size: Int): Bitmap =
        if (src.width == size && src.height == size) src
        else Bitmap.createScaledBitmap(src, size, size, true)

    fun createRoundedBitmap(src: Bitmap, cornerRadius: Float = 42f): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, src.width.toFloat(), src.height.toFloat())

        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return output
    }
}
