package com.nem.icons.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.nem.icons.domain.model.ColorPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class PaletteRepository(private val context: Context) {

    private val paletteFile = File(context.filesDir, "palettes.json")

    private val _palettes = MutableStateFlow<List<ColorPalette>>(loadPalettes())
    val palettes: StateFlow<List<ColorPalette>> = _palettes.asStateFlow()

    // Built-in ColorOS inspired presets
    val presets: List<ColorPalette> = listOf(
        ColorPalette(
            id = "preset_oppo_coral",
            name = "OPPO Coral",
            colors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53), Color(0xFFFF6B9D), Color(0xFFFFB347))
        ),
        ColorPalette(
            id = "preset_coloros_violet",
            name = "ColorOS Violet",
            colors = listOf(Color(0xFF6C63FF), Color(0xFF9B59B6), Color(0xFF3498DB), Color(0xFFE91E63))
        ),
        ColorPalette(
            id = "preset_hyperos_teal",
            name = "HyperOS Teal",
            colors = listOf(Color(0xFF00BFA5), Color(0xFF26C6DA), Color(0xFF4FC3F7), Color(0xFF80CBC4))
        ),
        ColorPalette(
            id = "preset_midnight",
            name = "Midnight Blue",
            colors = listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB), Color(0xFF5C6BC0))
        ),
        ColorPalette(
            id = "preset_sunset",
            name = "Sunset",
            colors = listOf(Color(0xFFFF6584), Color(0xFFFFB347), Color(0xFFFF4500), Color(0xFFFFD700))
        ),
        ColorPalette(
            id = "preset_forest",
            name = "Forest",
            colors = listOf(Color(0xFF2E7D32), Color(0xFF388E3C), Color(0xFF4CAF50), Color(0xFF8BC34A))
        )
    )

    suspend fun savePalette(palette: ColorPalette) = withContext(Dispatchers.IO) {
        val current = _palettes.value.toMutableList()
        current.removeAll { it.id == palette.id }
        current.add(0, palette)
        _palettes.value = current
        persistPalettes(current)
    }

    suspend fun deletePalette(id: String) = withContext(Dispatchers.IO) {
        val current = _palettes.value.toMutableList()
        current.removeAll { it.id == id }
        _palettes.value = current
        persistPalettes(current)
    }

    fun createNewPalette(name: String, colors: List<Color>): ColorPalette =
        ColorPalette(id = UUID.randomUUID().toString(), name = name, colors = colors)

    private fun loadPalettes(): List<ColorPalette> {
        if (!paletteFile.exists()) return emptyList()
        return try {
            val array = JSONArray(paletteFile.readText())
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                val colorsArr = obj.getJSONArray("colors")
                ColorPalette(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    colors = (0 until colorsArr.length()).map { j ->
                        Color(android.graphics.Color.parseColor(colorsArr.getString(j)))
                    },
                    isFromWallpaper = obj.optBoolean("isFromWallpaper", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistPalettes(palettes: List<ColorPalette>) {
        val array = JSONArray()
        palettes.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("isFromWallpaper", p.isFromWallpaper)
                put("createdAt", p.createdAt)
                val colorsArr = JSONArray()
                p.colors.forEach { c ->
                    colorsArr.put("#%02X%02X%02X".format(
                        (c.red * 255).toInt(),
                        (c.green * 255).toInt(),
                        (c.blue * 255).toInt()
                    ))
                }
                put("colors", colorsArr)
            }
            array.put(obj)
        }
        paletteFile.writeText(array.toString())
    }
}
