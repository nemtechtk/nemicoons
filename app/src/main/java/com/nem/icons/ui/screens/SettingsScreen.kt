package com.nem.icons.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nem.icons.ui.MainViewModel
import com.nem.icons.ui.components.NemTopBar
import com.nem.icons.ui.theme.NemPurple

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val libraryCount by viewModel.libraryIcons.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        NemTopBar(title = "Settings")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // About card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NemPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("N", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Nem Icons", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text("Version 1.0.0 · ColorOS Icon Studio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            SectionLabel("Library")
            SettingsItem(
                icon = Icons.Default.Collections,
                title = "Saved Icons",
                subtitle = "${libraryCount.size} icons in library"
            )
            SettingsItem(
                icon = Icons.Default.DeleteSweep,
                title = "Clear Library",
                subtitle = "Remove all saved icons",
                tint = MaterialTheme.colorScheme.error,
                onClick = { /* TODO: confirm dialog then viewModel.clearAll() */ }
            )

            SectionLabel("Processing")
            SettingsItem(
                icon = Icons.Default.HighQuality,
                title = "Icon Quality",
                subtitle = "192×192 px (high quality)"
            )
            SettingsItem(
                icon = Icons.Default.Layers,
                title = "Processing Engine",
                subtitle = "Local · Sobel edge detection + gradient blend"
            )

            SectionLabel("Export")
            SettingsItem(
                icon = Icons.Default.FolderOpen,
                title = "Export Location",
                subtitle = "Android/data/com.nem.icons/files/exports"
            )
            SettingsItem(
                icon = Icons.Default.FilePresent,
                title = "PNG Export",
                subtitle = "Full alpha transparency, 100% quality"
            )
            SettingsItem(
                icon = Icons.Default.FolderZip,
                title = "ZIP Export",
                subtitle = "All library icons in one archive"
            )
            SettingsItem(
                icon = Icons.Default.PhoneAndroid,
                title = "MTZ Export",
                subtitle = "Xiaomi/HyperOS theme package"
            )

            SectionLabel("About")
            SettingsItem(
                icon = Icons.Default.Code,
                title = "Technology",
                subtitle = "Kotlin · Jetpack Compose · Material 3"
            )
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Privacy",
                subtitle = "100% local processing · No internet required"
            )
            SettingsItem(
                icon = Icons.Default.Android,
                title = "Minimum Android",
                subtitle = "Android 8.0 (API 26)"
            )
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, color = tint)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onClick != null) {
                Icon(Icons.Default.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f))
            }
        }
    }
}
