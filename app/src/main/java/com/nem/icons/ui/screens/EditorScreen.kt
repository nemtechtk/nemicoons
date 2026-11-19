package com.nem.icons.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nem.icons.domain.model.IconStyle
import com.nem.icons.domain.model.StyleMode
import com.nem.icons.ui.MainViewModel
import com.nem.icons.ui.components.*
import com.nem.icons.ui.theme.*

@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val app by viewModel.currentApp.collectAsState()
    val style by viewModel.currentStyle.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val isProcessing by viewModel.isProcessingPreview.collectAsState()
    val palettes by viewModel.palettes.collectAsState()

    var showCompare by remember { mutableStateOf(false) }
    var color1PickerOpen by remember { mutableStateOf(false) }
    var color2PickerOpen by remember { mutableStateOf(false) }
    var savedSnack by remember { mutableStateOf(false) }

    if (app == null) { onBack(); return }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            NemTopBar(
                title = app!!.appName,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.resetStyle() }) {
                        Icon(Icons.Default.RestartAlt, "Reset")
                    }
                    IconButton(onClick = {
                        viewModel.saveCurrentToLibrary()
                        savedSnack = true
                    }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preview section
                item {
                    PreviewSection(
                        app = app!!,
                        style = style,
                        previewBitmap = previewBitmap,
                        isProcessing = isProcessing,
                        showCompare = showCompare,
                        onToggleCompare = { showCompare = !showCompare }
                    )
                }

                // Style mode selector
                item {
                    StyleModeSelector(
                        currentMode = style.mode,
                        onModeChanged = { viewModel.updateMode(it) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Colors
                item {
                    ColorSection(
                        style = style,
                        onColor1Click = { color1PickerOpen = true },
                        onColor2Click = { color2PickerOpen = true },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Gradient angle (only for GRADIENT mode)
                if (style.mode == StyleMode.GRADIENT) {
                    item {
                        SurfaceCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            LabeledSlider(
                                label = "Gradient Angle",
                                value = style.gradientAngle,
                                valueRange = 0f..360f,
                                onValueChange = { viewModel.updateGradientAngle(it) },
                                displayValue = "${style.gradientAngle.toInt()}°"
                            )
                        }
                    }
                }

                // Intensity
                item {
                    SurfaceCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        LabeledSlider(
                            label = "Effect Intensity",
                            value = style.intensity,
                            valueRange = 0f..1f,
                            onValueChange = { viewModel.updateIntensity(it) },
                            displayValue = "${(style.intensity * 100).toInt()}%"
                        )
                    }
                }

                // Adjustments
                item {
                    SurfaceCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Adjustments",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LabeledSlider(
                            label = "Brightness",
                            value = style.brightness,
                            valueRange = -1f..1f,
                            onValueChange = { viewModel.updateBrightness(it) }
                        )
                        Spacer(Modifier.height(8.dp))
                        LabeledSlider(
                            label = "Contrast",
                            value = style.contrast,
                            valueRange = -1f..1f,
                            onValueChange = { viewModel.updateContrast(it) }
                        )
                        Spacer(Modifier.height(8.dp))
                        LabeledSlider(
                            label = "Saturation",
                            value = style.saturation,
                            valueRange = -1f..1f,
                            onValueChange = { viewModel.updateSaturation(it) }
                        )
                    }
                }

                // Palette quick picks
                val allPalettes = viewModel.presetPalettes + palettes
                if (allPalettes.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Quick Palettes")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(allPalettes) { palette ->
                                PaletteChip(
                                    palette = palette,
                                    onClick = { viewModel.applyPalette(palette) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Saved snackbar
        AnimatedVisibility(
            visible = savedSnack,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) {
            Snackbar {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = NemTeal)
                    Spacer(Modifier.width(8.dp))
                    Text("Saved to Library")
                }
            }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                savedSnack = false
            }
        }
    }

    // Color Pickers
    if (color1PickerOpen) {
        ColorPickerDialog(
            initialColor = style.color1,
            onColorSelected = { viewModel.updateColor1(it) },
            onDismiss = { color1PickerOpen = false }
        )
    }
    if (color2PickerOpen) {
        ColorPickerDialog(
            initialColor = style.color2,
            onColorSelected = { viewModel.updateColor2(it) },
            onDismiss = { color2PickerOpen = false }
        )
    }
}

@Composable
private fun PreviewSection(
    app: com.nem.icons.domain.model.AppInfo,
    style: IconStyle,
    previewBitmap: android.graphics.Bitmap?,
    isProcessing: Boolean,
    showCompare: Boolean,
    onToggleCompare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showCompare) {
                // Side by side
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppIconImage(bitmap = app.icon, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Original", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        if (previewBitmap != null) {
                            AppIconImage(bitmap = previewBitmap, modifier = Modifier.size(80.dp))
                        } else {
                            AppIconImage(bitmap = app.icon, modifier = Modifier.size(80.dp).alpha(0.3f))
                        }
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = NemPurple,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Styled", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Single large preview
                Box(contentAlignment = Alignment.Center) {
                    if (previewBitmap != null) {
                        AppIconImage(bitmap = previewBitmap, modifier = Modifier.size(120.dp))
                    } else {
                        AppIconImage(bitmap = app.icon, modifier = Modifier.size(120.dp).alpha(0.5f))
                    }
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = NemPurple
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onToggleCompare) {
            Icon(
                if (showCompare) Icons.Default.Visibility else Icons.Default.CompareArrows,
                null, modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(if (showCompare) "Single View" else "Compare")
        }
    }
}

@Composable
private fun StyleModeSelector(
    currentMode: StyleMode,
    onModeChanged: (StyleMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StyleMode.values().forEach { mode ->
            val selected = currentMode == mode
            FilterChip(
                selected = selected,
                onClick = { onModeChanged(mode) },
                label = {
                    Text(
                        when (mode) {
                            StyleMode.GRADIENT  -> "Gradient"
                            StyleMode.TINT      -> "Tint"
                            StyleMode.HUE_SHIFT -> "Hue Shift"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (selected) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                } else null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ColorSection(
    style: IconStyle,
    onColor1Click: () -> Unit,
    onColor2Click: () -> Unit,
    modifier: Modifier = Modifier
) {
    SurfaceCard(modifier = modifier) {
        Text(
            "Colors",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColorButton(
                label = "Primary",
                color = style.color1,
                onClick = onColor1Click,
                modifier = Modifier.weight(1f)
            )
            ColorButton(
                label = "Secondary",
                color = style.color2,
                onClick = onColor2Click,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        // Gradient preview bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(listOf(style.color1, style.color2))
                )
        )
    }
}

@Composable
private fun ColorButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "#%02X%02X%02X".format(
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                ),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PaletteChip(
    palette: com.nem.icons.domain.model.ColorPalette,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            palette.colors.take(4).forEach { c ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(c)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            palette.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    com.nem.icons.ui.components.ColorPickerDialog(
        initialColor = initialColor,
        onColorSelected = onColorSelected,
        onDismiss = onDismiss
    )
}
