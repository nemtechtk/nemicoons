package com.nem.icons.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.nem.icons.domain.model.ExportResult
import com.nem.icons.domain.model.StyledIcon
import com.nem.icons.ui.MainViewModel
import com.nem.icons.ui.components.*
import com.nem.icons.ui.theme.*
import com.nem.icons.utils.ExportUtils

@Composable
fun LibraryScreen(viewModel: MainViewModel) {
    val icons by viewModel.libraryIcons.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    val context = LocalContext.current

    var showExportMenu by remember { mutableStateOf(false) }
    var selectedIcon by remember { mutableStateOf<StyledIcon?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        NemTopBar(
            title = "Library",
            actions = {
                if (icons.isNotEmpty()) {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.FileUpload, "Export all")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export ZIP") },
                                leadingIcon = { Icon(Icons.Default.FolderZip, null) },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportLibraryZip()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export MTZ (Xiaomi)") },
                                leadingIcon = { Icon(Icons.Default.PhoneAndroid, null) },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportLibraryMtz()
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Clear All", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, null,
                                    tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showExportMenu = false
                                    // confirm before clear
                                }
                            )
                        }
                    }
                }
            }
        )

        if (icons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.Collections,
                    title = "Library is empty",
                    subtitle = "Style icons in the editor and save them here"
                )
            }
        } else {
            Text(
                "${icons.size} icons",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(80.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(icons, key = { it.id }) { icon ->
                    LibraryIconItem(
                        icon = icon,
                        onClick = { selectedIcon = icon }
                    )
                }
            }
        }
    }

    // Icon detail bottom sheet
    selectedIcon?.let { icon ->
        IconDetailSheet(
            icon = icon,
            onDismiss = { selectedIcon = null },
            onExportPng = {
                viewModel.exportSingle(icon)
                selectedIcon = null
            },
            onDelete = {
                viewModel.deleteFromLibrary(icon.id)
                selectedIcon = null
            },
            onShare = {
                val path = icon.styledBitmap.let { bmp ->
                    val file = java.io.File(context.cacheDir, "${icon.appName}_nem.png")
                    java.io.FileOutputStream(file).use { bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                    file.absolutePath
                }
                val intent = ExportUtils.shareFile(context, path)
                context.startActivity(Intent.createChooser(intent, "Share Icon"))
                selectedIcon = null
            }
        )
    }

    // Export result snackbar
    exportResult?.let { result ->
        LaunchedEffect(result) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearExportResult()
        }
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                action = {
                    if (result.success && result.path != null) {
                        TextButton(onClick = {
                            context.startActivity(ExportUtils.shareFile(context, result.path))
                            viewModel.clearExportResult()
                        }) { Text("Share") }
                    }
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                        null,
                        tint = if (result.success) NemTeal else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (result.success) "Exported to: ${result.path?.substringAfterLast('/')}"
                        else "Export failed: ${result.error}"
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryIconItem(icon: StyledIcon, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIconImage(bitmap = icon.styledBitmap, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = icon.appName,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconDetailSheet(
    icon: StyledIcon,
    onDismiss: () -> Unit,
    onExportPng: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Before/After comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppIconImage(bitmap = icon.originalBitmap, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Original", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    Icons.Default.ArrowForward, null,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppIconImage(bitmap = icon.styledBitmap, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Styled", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                icon.appName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                icon.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Color preview
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape)
                        .background(icon.style.color1)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), CircleShape)
                )
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape)
                        .background(icon.style.color2)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), CircleShape)
                )
                Text(
                    icon.style.mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share")
                }
                Button(
                    onClick = onExportPng,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PNG")
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Delete")
            }
        }
    }
}
