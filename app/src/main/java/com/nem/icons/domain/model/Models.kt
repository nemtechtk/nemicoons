package com.nem.icons.domain.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Bitmap,
    var isSelected: Boolean = false
)

data class IconStyle(
    val color1: Color = Color(0xFF6C63FF),
    val color2: Color = Color(0xFFFF6584),
    val brightness: Float = 0f,       // -1f .. 1f
    val contrast: Float = 0f,         // -1f .. 1f
    val saturation: Float = 0f,       // -1f .. 1f
    val intensity: Float = 0.7f,      // 0f .. 1f
    val gradientAngle: Float = 135f,  // degrees
    val mode: StyleMode = StyleMode.GRADIENT
)

enum class StyleMode {
    GRADIENT, TINT, HUE_SHIFT
}

data class StyledIcon(
    val id: String,
    val packageName: String,
    val appName: String,
    val originalBitmap: Bitmap,
    val styledBitmap: Bitmap,
    val style: IconStyle,
    val createdAt: Long = System.currentTimeMillis()
)

data class ColorPalette(
    val id: String,
    val name: String,
    val colors: List<Color>,
    val isFromWallpaper: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class ExportResult(
    val success: Boolean,
    val path: String? = null,
    val error: String? = null
)

sealed class ProcessingState {
    object Idle : ProcessingState()
    data class Progress(val current: Int, val total: Int, val appName: String) : ProcessingState()
    data class Done(val count: Int) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}
