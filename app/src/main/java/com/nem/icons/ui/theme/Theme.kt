package com.nem.icons.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val NemPurple = Color(0xFF7C4DFF)
val NemPink = Color(0xFFFF4081)
val NemTeal = Color(0xFF00BFA5)
val NemBackground = Color(0xFF0E0E16)
val NemSurface = Color(0xFF1A1A2E)
val NemSurfaceVariant = Color(0xFF232340)
val NemOnSurface = Color(0xFFE8E8F0)

private val DarkColorScheme = darkColorScheme(
    primary = NemPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3D1F8F),
    onPrimaryContainer = Color(0xFFCFBEFF),
    secondary = NemPink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8A1041),
    onSecondaryContainer = Color(0xFFFFD1E0),
    tertiary = NemTeal,
    onTertiary = Color.Black,
    background = NemBackground,
    onBackground = NemOnSurface,
    surface = NemSurface,
    onSurface = NemOnSurface,
    surfaceVariant = NemSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B0CC),
    outline = Color(0xFF4A4A6A),
    outlineVariant = Color(0xFF2A2A45),
)

private val LightColorScheme = lightColorScheme(
    primary = NemPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7FF),
    onPrimaryContainer = Color(0xFF3D1F8F),
    secondary = NemPink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD1E0),
    onSecondaryContainer = Color(0xFF8A1041),
    tertiary = NemTeal,
    background = Color(0xFFF6F6FF),
    onBackground = Color(0xFF1A1A2E),
    surface = Color.White,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFF0EEFF),
    onSurfaceVariant = Color(0xFF4A4A6A),
    outline = Color(0xFFB0B0CC),
)

@Composable
fun NemIconsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
