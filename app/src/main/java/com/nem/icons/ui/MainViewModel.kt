package com.nem.icons.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nem.icons.data.repository.AppRepository
import com.nem.icons.data.repository.IconLibraryRepository
import com.nem.icons.data.repository.PaletteRepository
import com.nem.icons.domain.model.*
import com.nem.icons.utils.ExportUtils
import com.nem.icons.utils.IconProcessor
import com.nem.icons.utils.PaletteExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainViewModel(private val context: Context) : ViewModel() {

    private val appRepository = AppRepository(context)
    val libraryRepository = IconLibraryRepository(context)
    val paletteRepository = PaletteRepository(context)

    // ─── Apps ─────────────────────────────────────────────────────────────────
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredApps: StateFlow<List<AppInfo>> = combine(_apps, _searchQuery) { apps, query ->
        if (query.isBlank()) apps
        else apps.filter { it.appName.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _appsLoading = MutableStateFlow(false)
    val appsLoading: StateFlow<Boolean> = _appsLoading.asStateFlow()

    // ─── Current style ────────────────────────────────────────────────────────
    private val _currentStyle = MutableStateFlow(IconStyle())
    val currentStyle: StateFlow<IconStyle> = _currentStyle.asStateFlow()

    // ─── Current icon being edited ────────────────────────────────────────────
    private val _currentApp = MutableStateFlow<AppInfo?>(null)
    val currentApp: StateFlow<AppInfo?> = _currentApp.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _isProcessingPreview = MutableStateFlow(false)
    val isProcessingPreview: StateFlow<Boolean> = _isProcessingPreview.asStateFlow()

    // ─── Batch ────────────────────────────────────────────────────────────────
    private val _batchState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val batchState: StateFlow<ProcessingState> = _batchState.asStateFlow()

    // ─── Wallpaper ────────────────────────────────────────────────────────────
    private val _wallpaperBitmap = MutableStateFlow<Bitmap?>(null)
    val wallpaperBitmap: StateFlow<Bitmap?> = _wallpaperBitmap.asStateFlow()

    private val _wallpaperColors = MutableStateFlow<List<Color>>(emptyList())
    val wallpaperColors: StateFlow<List<Color>> = _wallpaperColors.asStateFlow()

    // ─── Export ───────────────────────────────────────────────────────────────
    private val _exportResult = MutableStateFlow<ExportResult?>(null)
    val exportResult: StateFlow<ExportResult?> = _exportResult.asStateFlow()

    // ─── Library ──────────────────────────────────────────────────────────────
    val libraryIcons: StateFlow<List<StyledIcon>> = libraryRepository.icons

    // ─── Palettes ─────────────────────────────────────────────────────────────
    val palettes: StateFlow<List<ColorPalette>> = paletteRepository.palettes
    val presetPalettes: List<ColorPalette> = paletteRepository.presets

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _appsLoading.value = true
            _apps.value = appRepository.getInstalledApps()
            _appsLoading.value = false
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun selectApp(app: AppInfo) {
        _currentApp.value = app
        updatePreview()
    }

    fun toggleAppSelection(app: AppInfo) {
        _apps.value = _apps.value.map {
            if (it.packageName == app.packageName) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun selectAllApps() {
        _apps.value = _apps.value.map { it.copy(isSelected = true) }
    }

    fun deselectAllApps() {
        _apps.value = _apps.value.map { it.copy(isSelected = false) }
    }

    val selectedApps: List<AppInfo> get() = _apps.value.filter { it.isSelected }

    // ─── Style Updates ────────────────────────────────────────────────────────

    fun updateColor1(color: Color) {
        _currentStyle.value = _currentStyle.value.copy(color1 = color)
        updatePreview()
    }

    fun updateColor2(color: Color) {
        _currentStyle.value = _currentStyle.value.copy(color2 = color)
        updatePreview()
    }

    fun updateBrightness(v: Float) {
        _currentStyle.value = _currentStyle.value.copy(brightness = v)
        updatePreview()
    }

    fun updateContrast(v: Float) {
        _currentStyle.value = _currentStyle.value.copy(contrast = v)
        updatePreview()
    }

    fun updateSaturation(v: Float) {
        _currentStyle.value = _currentStyle.value.copy(saturation = v)
        updatePreview()
    }

    fun updateIntensity(v: Float) {
        _currentStyle.value = _currentStyle.value.copy(intensity = v)
        updatePreview()
    }

    fun updateGradientAngle(v: Float) {
        _currentStyle.value = _currentStyle.value.copy(gradientAngle = v)
        updatePreview()
    }

    fun updateMode(mode: StyleMode) {
        _currentStyle.value = _currentStyle.value.copy(mode = mode)
        updatePreview()
    }

    fun applyPalette(palette: ColorPalette) {
        if (palette.colors.size >= 2) {
            _currentStyle.value = _currentStyle.value.copy(
                color1 = palette.colors[0],
                color2 = palette.colors[1]
            )
            updatePreview()
        }
    }

    fun resetStyle() {
        _currentStyle.value = IconStyle()
        updatePreview()
    }

    private fun updatePreview() {
        val app = _currentApp.value ?: return
        viewModelScope.launch {
            _isProcessingPreview.value = true
            val styled = withContext(Dispatchers.Default) {
                IconProcessor.applyStyle(app.icon, _currentStyle.value)
            }
            _previewBitmap.value = styled
            _isProcessingPreview.value = false
        }
    }

    // ─── Save to library ──────────────────────────────────────────────────────

    fun saveCurrentToLibrary() {
        val app = _currentApp.value ?: return
        val preview = _previewBitmap.value ?: return
        viewModelScope.launch {
            val icon = StyledIcon(
                id = UUID.randomUUID().toString(),
                packageName = app.packageName,
                appName = app.appName,
                originalBitmap = app.icon,
                styledBitmap = preview,
                style = _currentStyle.value
            )
            libraryRepository.save(icon)
        }
    }

    // ─── Batch processing ────────────────────────────────────────────────────

    fun batchProcess() {
        val selected = selectedApps
        if (selected.isEmpty()) return
        viewModelScope.launch {
            _batchState.value = ProcessingState.Progress(0, selected.size, "")
            var done = 0
            selected.forEach { app ->
                _batchState.value = ProcessingState.Progress(done, selected.size, app.appName)
                val styled = withContext(Dispatchers.Default) {
                    IconProcessor.applyStyle(app.icon, _currentStyle.value)
                }
                libraryRepository.save(StyledIcon(
                    id = UUID.randomUUID().toString(),
                    packageName = app.packageName,
                    appName = app.appName,
                    originalBitmap = app.icon,
                    styledBitmap = styled,
                    style = _currentStyle.value
                ))
                done++
            }
            _batchState.value = ProcessingState.Done(done)
            deselectAllApps()
        }
    }

    fun dismissBatchState() { _batchState.value = ProcessingState.Idle }

    // ─── Export ───────────────────────────────────────────────────────────────

    fun exportSingle(icon: StyledIcon) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                ExportUtils.exportSinglePng(context, icon)
            }
            _exportResult.value = result
        }
    }

    fun exportLibraryZip() {
        viewModelScope.launch {
            val icons = libraryIcons.value
            if (icons.isEmpty()) return@launch
            val result = withContext(Dispatchers.IO) {
                ExportUtils.exportZip(context, icons)
            }
            _exportResult.value = result
        }
    }

    fun exportLibraryMtz() {
        viewModelScope.launch {
            val icons = libraryIcons.value
            if (icons.isEmpty()) return@launch
            val result = withContext(Dispatchers.IO) {
                ExportUtils.exportMtz(context, icons)
            }
            _exportResult.value = result
        }
    }

    fun clearExportResult() { _exportResult.value = null }

    // ─── Wallpaper ────────────────────────────────────────────────────────────

    fun loadWallpaperFromUri(uri: Uri) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } catch (e: Exception) { null }
            } ?: return@launch

            _wallpaperBitmap.value = bitmap
            val small = PaletteExtractor.scaleBitmapForPalette(bitmap)
            _wallpaperColors.value = PaletteExtractor.extractFromBitmap(small)
        }
    }

    // ─── Palettes ─────────────────────────────────────────────────────────────

    fun saveWallpaperPaletteAs(name: String) {
        val colors = _wallpaperColors.value
        if (colors.isEmpty()) return
        viewModelScope.launch {
            val palette = paletteRepository.createNewPalette(name, colors).copy(isFromWallpaper = true)
            paletteRepository.savePalette(palette)
        }
    }

    fun saveCustomPalette(name: String, colors: List<Color>) {
        viewModelScope.launch {
            val palette = paletteRepository.createNewPalette(name, colors)
            paletteRepository.savePalette(palette)
        }
    }

    fun deletePalette(id: String) {
        viewModelScope.launch { paletteRepository.deletePalette(id) }
    }

    fun deleteFromLibrary(id: String) {
        viewModelScope.launch { libraryRepository.delete(id) }
    }
}
