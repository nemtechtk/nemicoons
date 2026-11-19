package com.nem.icons.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.nem.icons.domain.model.AppInfo
import com.nem.icons.domain.model.ProcessingState
import com.nem.icons.ui.MainViewModel
import com.nem.icons.ui.components.*
import com.nem.icons.ui.theme.*

@Composable
fun AppsScreen(
    viewModel: MainViewModel,
    onOpenEditor: (AppInfo) -> Unit
) {
    val apps by viewModel.filteredApps.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val loading by viewModel.appsLoading.collectAsState()
    val batchState by viewModel.batchState.collectAsState()
    val selected = viewModel.selectedApps

    var batchMode by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Top bar
        NemTopBar(
            title = if (batchMode) "${selected.size} selected" else "Apps",
            actions = {
                if (batchMode) {
                    IconButton(onClick = { viewModel.selectAllApps() }) {
                        Icon(Icons.Default.SelectAll, "Select all")
                    }
                    IconButton(onClick = {
                        viewModel.deselectAllApps()
                        batchMode = false
                    }) {
                        Icon(Icons.Default.Close, "Cancel")
                    }
                } else {
                    IconButton(onClick = { batchMode = true }) {
                        Icon(Icons.Default.CheckBox, "Batch mode")
                    }
                    IconButton(onClick = { viewModel.loadApps() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            }
        )

        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search apps…") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        )

        // Batch action bar
        AnimatedVisibility(visible = batchMode && selected.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.batchProcess() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Process ${selected.size}")
                }
            }
        }

        // Loading
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NemPurple)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading apps…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Column
        }

        // Grid
        if (apps.isEmpty()) {
            EmptyState(Icons.Default.Apps, "No apps found", "Try a different search query")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(80.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppGridItem(
                        app = app,
                        batchMode = batchMode,
                        onTap = {
                            if (batchMode) viewModel.toggleAppSelection(app)
                            else { viewModel.selectApp(app); onOpenEditor(app) }
                        },
                        onLongPress = {
                            if (!batchMode) { batchMode = true }
                            viewModel.toggleAppSelection(app)
                        }
                    )
                }
            }
        }

        // Batch progress overlay
        if (batchState is ProcessingState.Progress || batchState is ProcessingState.Done) {
            BatchProgressBar(
                state = batchState,
                onDismiss = { viewModel.dismissBatchState(); batchMode = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppGridItem(
    app: AppInfo,
    batchMode: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AppIconImage(
                bitmap = app.icon,
                modifier = Modifier.size(56.dp)
            )
            if (batchMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (app.isSelected) NemPurple
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(1.5.dp,
                            if (app.isSelected) NemPurple else MaterialTheme.colorScheme.outline,
                            CircleShape)
                ) {
                    if (app.isSelected) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp).align(Alignment.Center)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = app.appName,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun BatchProgressBar(
    state: ProcessingState,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp
    ) {
        when (state) {
            is ProcessingState.Progress -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Processing: ${state.appName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${state.current}/${state.total}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (state.total > 0) state.current.toFloat() / state.total else 0f },
                        modifier = Modifier.fillMaxWidth(),
                        color = NemPurple
                    )
                }
            }
            is ProcessingState.Done -> {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = NemTeal)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Done! Processed ${state.count} icons",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onDismiss) { Text("OK") }
                }
            }
            else -> {}
        }
    }
}
