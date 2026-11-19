package com.nem.icons.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nem.icons.domain.model.IconStyle
import com.nem.icons.domain.model.StyledIcon
import com.nem.icons.domain.model.StyleMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.Color

class IconLibraryRepository(private val context: Context) {

    private val iconsDir = File(context.filesDir, "icons").also { it.mkdirs() }
    private val metaFile = File(context.filesDir, "icons_meta.json")

    private val _icons = MutableStateFlow<List<StyledIcon>>(emptyList())
    val icons: StateFlow<List<StyledIcon>> = _icons.asStateFlow()

   private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

init {
    repositoryScope.launch {
        _icons.value = loadAll()
    }
}

    suspend fun save(icon: StyledIcon) = withContext(Dispatchers.IO) {
        // Save bitmap files
        val originalFile = File(iconsDir, "${icon.id}_original.png")
        val styledFile = File(iconsDir, "${icon.id}_styled.png")
        FileOutputStream(originalFile).use { icon.originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        FileOutputStream(styledFile).use { icon.styledBitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        // Update metadata
        val current = _icons.value.toMutableList()
        current.removeAll { it.id == icon.id }
        current.add(0, icon)
        _icons.value = current
        saveMeta(current)
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        File(iconsDir, "${id}_original.png").delete()
        File(iconsDir, "${id}_styled.png").delete()
        val current = _icons.value.toMutableList()
        current.removeAll { it.id == id }
        _icons.value = current
        saveMeta(current)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        iconsDir.listFiles()?.forEach { it.delete() }
        _icons.value = emptyList()
        metaFile.delete()
    }

    private suspend fun loadAll(): List<StyledIcon> = withContext(Dispatchers.IO) {
        if (!metaFile.exists()) return@withContext emptyList()
        try {
            val json = JSONArray(metaFile.readText())
            val result = mutableListOf<StyledIcon>()
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                val id = obj.getString("id")
                val originalFile = File(iconsDir, "${id}_original.png")
                val styledFile = File(iconsDir, "${id}_styled.png")
                if (!originalFile.exists() || !styledFile.exists()) continue
                val originalBitmap = BitmapFactory.decodeFile(originalFile.absolutePath) ?: continue
                val styledBitmap = BitmapFactory.decodeFile(styledFile.absolutePath) ?: continue
                result.add(StyledIcon(
                    id = id,
                    packageName = obj.getString("packageName"),
                    appName = obj.getString("appName"),
                    originalBitmap = originalBitmap,
                    styledBitmap = styledBitmap,
                    style = styleFromJson(obj.getJSONObject("style")),
                    createdAt = obj.getLong("createdAt")
                ))
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveMeta(icons: List<StyledIcon>) {
        val array = JSONArray()
        icons.forEach { icon ->
            val obj = JSONObject().apply {
                put("id", icon.id)
                put("packageName", icon.packageName)
                put("appName", icon.appName)
                put("createdAt", icon.createdAt)
                put("style", styleToJson(icon.style))
            }
            array.put(obj)
        }
        metaFile.writeText(array.toString())
    }

    private fun styleToJson(style: IconStyle): JSONObject = JSONObject().apply {
        put("color1", colorToHex(style.color1))
        put("color2", colorToHex(style.color2))
        put("brightness", style.brightness)
        put("contrast", style.contrast)
        put("saturation", style.saturation)
        put("intensity", style.intensity)
        put("gradientAngle", style.gradientAngle)
        put("mode", style.mode.name)
    }

    private fun styleFromJson(obj: JSONObject): IconStyle = IconStyle(
        color1 = hexToColor(obj.getString("color1")),
        color2 = hexToColor(obj.getString("color2")),
        brightness = obj.getDouble("brightness").toFloat(),
        contrast = obj.getDouble("contrast").toFloat(),
        saturation = obj.getDouble("saturation").toFloat(),
        intensity = obj.getDouble("intensity").toFloat(),
        gradientAngle = obj.getDouble("gradientAngle").toFloat(),
        mode = StyleMode.valueOf(obj.getString("mode"))
    )

    private fun colorToHex(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return "#%02X%02X%02X".format(r, g, b)
    }

    private fun hexToColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF6C63FF)
        }
    }
}
