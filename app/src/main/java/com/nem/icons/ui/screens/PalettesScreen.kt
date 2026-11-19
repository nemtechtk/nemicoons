package com.nem.icons.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nem.icons.domain.model.ColorPalette
import com.nem.icons.ui.MainViewModel
import com.nem.icons.ui.components.*
import com.nem.icons.ui.theme.*

@Composable
fun PalettesScreen(viewModel: MainViewModel) {
    val palettes by viewModel.palettes.collectAsState()
    val wallpaper by viewModel.wallpaperBitmap.collectAsState()
    val wallpaperColors by viewModel.wallpaperColors.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var newPaletteName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var customColors by remember { mutableStateOf(listOf<Color>()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadWallpaperFromUri(it) }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        NemTopBar(
            title = "Palettes",
            actions = {
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, "New palette")
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Wallpaper section
            item {
                SectionHeader(title = "Wallpaper Colors")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Wallpaper thumbnail
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.outline.copy(0.2f))
                                    .clickable {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                        }
                                        imagePicker.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (wallpaper != null) {
                                    Image(
                                        bitmap = wallpaper!!.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                       Icon(
    imageVector = Icons.Default.Wallpaper,
    contentDescription = null,
    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Pick",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Extract from wallpaper",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Tap to pick an image from gallery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                        }
                                        imagePicker.launch("image/*")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Select Image", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // Extracted colors
                        AnimatedVisibility(visible = wallpaperColors.isNotEmpty()) {
                            Column {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Extracted colors",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    wallpaperColors.forEach { color ->
                                        ColorSwatch(color = color, size = 36.dp, onClick = {})
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { showSaveDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Save as Palette")
                                }
                            }
                        }
                    }
                }
            }

            // Presets section
            item { SectionHeader(title = "Presets") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.presetPalettes) { palette ->
                        PaletteCard(
                            palette = palette,
                            onApply = { viewModel.applyPalette(palette) },
                            onDelete = null
                        )
                    }
                }
            }

            // Custom palettes
            if (palettes.isNotEmpty()) {
                item { SectionHeader(title = "My Palettes") }
                items(palettes) { palette ->
                    PaletteCard(
                        palette = palette,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onApply = { viewModel.applyPalette(palette) },
                        onDelete = { viewModel.deletePalette(palette.id) },
                        horizontal = true
                    )
                }
            }
        }
    }

    // Save wallpaper palette dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Palette") },
            text = {
                OutlinedTextField(
                    value = newPaletteName,
                    onValueChange = { newPaletteName = it },
                    label = { Text("Palette name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPaletteName.isNotBlank()) {
                        viewModel.saveWallpaperPaletteAs(newPaletteName)
                        newPaletteName = ""
                        showSaveDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Create custom palette dialog
    if (showCreateDialog) {
        CreatePaletteDialog(
            onSave = { name, colors ->
                viewModel.saveCustomPalette(name, colors)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun PaletteCard(
    palette: ColorPalette,
    modifier: Modifier = Modifier,
    onApply: () -> Unit,
    onDelete: (() -> Unit)?,
    horizontal: Boolean = false
) {
    Surface(
        modifier = modifier.then(
            if (horizontal) Modifier.fillMaxWidth() else Modifier.width(160.dp)
        ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
    ) {
        if (horizontal) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    palette.colors.take(5).forEach { c ->
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(c)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    palette.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = onApply) { Text("Use") }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null,
                            tint = MaterialTheme.colorScheme.error.copy(0.7f),
                            modifier = Modifier.size(16.dp))
                    }
                }
            }
        } else {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    palette.colors.take(4).forEach { c ->
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(c))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    palette.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                TextButton(
                    onClick = onApply,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Apply", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun CreatePaletteDialog(
    onSave: (String, List<Color>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var colors by remember { mutableStateOf(listOf(
        Color(0xFF6C63FF), Color(0xFFFF6584), Color(0xFF43E97B), Color(0xFFFFB347)
    )) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Palette") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Colors", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colors.forEachIndexed { i, color ->
                        ColorSwatch(
                            color = color,
                            size = 40.dp,
                            selected = editingIndex == i,
                            onClick = { editingIndex = i }
                        )
                    }
                    IconButton(
                        onClick = {
                            if (colors.size < 8) colors = colors + Color(0xFF888888)
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, "Add color",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onSave(name, colors)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    editingIndex?.let { idx ->
        ColorPickerDialog(
            initialColor = colors[idx],
            onColorSelected = { c ->
                colors = colors.toMutableList().also { it[idx] = c }
            },
            onDismiss = { editingIndex = null }
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
