package com.nem.icons.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var color by remember { mutableStateOf(initialColor) }
    val hsv = remember { FloatArray(3) }

    fun colorToHsv(c: Color) {
        android.graphics.Color.colorToHSV(
            android.graphics.Color.argb(
                (c.alpha * 255).toInt(), (c.red * 255).toInt(),
                (c.green * 255).toInt(), (c.blue * 255).toInt()
            ), hsv
        )
    }
    colorToHsv(color)

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var sat by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    var hexInput by remember { mutableStateOf(colorToHex(color)) }
    var rInput by remember { mutableStateOf("${(color.red * 255).toInt()}") }
    var gInput by remember { mutableStateOf("${(color.green * 255).toInt()}") }
    var bInput by remember { mutableStateOf("${(color.blue * 255).toInt()}") }

    fun updateFromHsv() {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value))
        color = Color(argb)
        hexInput = colorToHex(color)
        rInput = "${(color.red * 255).toInt()}"
        gInput = "${(color.green * 255).toInt()}"
        bInput = "${(color.blue * 255).toInt()}"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Color Picker", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SV Square picker
                SaturationValuePicker(
                    hue = hue, saturation = sat, value = value,
                    onChanged = { s, v -> sat = s; value = v; updateFromHsv() },
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )

                // Hue bar
                HueBar(
                    hue = hue,
                    onHueChanged = { h -> hue = h; updateFromHsv() },
                    modifier = Modifier.fillMaxWidth().height(28.dp)
                )

                // Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    )
                    // HEX input
                    val focusMgr = LocalFocusManager.current
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { v ->
                            hexInput = v.take(7)
                            if (v.length == 7) {
                                try {
                                    val argb = android.graphics.Color.parseColor(v)
                                    color = Color(argb)
                                    colorToHsv(color)
                                    hue = hsv[0]; sat = hsv[1]; value = hsv[2]
                                    rInput = "${(color.red * 255).toInt()}"
                                    gInput = "${(color.green * 255).toInt()}"
                                    bInput = "${(color.blue * 255).toInt()}"
                                } catch (_: Exception) {}
                            }
                        },
                        label = { Text("HEX", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // RGB inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    @Composable
fun rgbField(label: String, v: String, onV: (String) -> Unit) {
                        OutlinedTextField(
                            value = v,
                            onValueChange = { s ->
                                onV(s.filter { it.isDigit() }.take(3))
                                val r2 = rInput.toIntOrNull()?.coerceIn(0,255) ?: return@OutlinedTextField
                                val g2 = gInput.toIntOrNull()?.coerceIn(0,255) ?: return@OutlinedTextField
                                val b2 = bInput.toIntOrNull()?.coerceIn(0,255) ?: return@OutlinedTextField
                                color = Color(r2, g2, b2)
                                colorToHsv(color)
                                hue = hsv[0]; sat = hsv[1]; value = hsv[2]
                                hexInput = colorToHex(color)
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    rgbField("R", rInput) { rInput = it }
                    rgbField("G", gInput) { gInput = it }
                    rgbField("B", bInput) { bInput = it }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onColorSelected(color); onDismiss() }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SaturationValuePicker(
    hue: Float, saturation: Float, value: Float,
    onChanged: (s: Float, v: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White, Color.hsv(hue, 1f, 1f)),
                    start = Offset.Zero, end = Offset(Float.MAX_VALUE, 0f)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color.Transparent, Color.Black),
                        start = Offset.Zero, end = Offset(0f, Float.MAX_VALUE)
                    )
                )
                .pointerInput(hue) {
                    detectTapGestures { offset ->
                        val s = (offset.x / size.width).coerceIn(0f, 1f)
                        val v = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                        onChanged(s, v)
                    }
                }
                .onGloballyPositioned { coords ->
                    size = androidx.compose.ui.geometry.Size(
                        coords.size.width.toFloat(), coords.size.height.toFloat()
                    )
                }
        )
        // Thumb
        Box(
            modifier = Modifier
                .offset(
                    x = (saturation * (size.width - 20)).dp / LocalDensity.current.density,
                    y = ((1f - value) * (size.height - 20)).dp / LocalDensity.current.density
                )
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .background(Color.hsv(hue, saturation, value))
        )
    }
}

@Composable
private fun HueBar(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var width by remember { mutableFloatStateOf(0f) }
    val hueColors = remember {
        List(361) { h -> Color.hsv(h.toFloat(), 1f, 1f) }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(hueColors))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onHueChanged((offset.x / width * 360f).coerceIn(0f, 360f))
                }
            }
            .onGloballyPositioned { width = it.size.width.toFloat() }
    ) {
        // Thumb
        Box(
            modifier = Modifier
                .offset(x = ((hue / 360f) * (width - 24)).dp / LocalDensity.current.density)
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .background(Color.hsv(hue, 1f, 1f))
                .align(Alignment.CenterStart)
        )
    }
}



private fun colorToHex(c: Color): String {
    val r = (c.red * 255).toInt()
    val g = (c.green * 255).toInt()
    val b = (c.blue * 255).toInt()
    return "#%02X%02X%02X".format(r, g, b)
}
