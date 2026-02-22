package com.seung.sensormaster.ui.screens.tools.color

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ColorState(
    val colorHex: String = "#000000",
    val r: Int = 0, val g: Int = 0, val b: Int = 0,
    val colorName: String = "검정",
    val isCameraReady: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val isCaptured: Boolean = false
)

@HiltViewModel
class ColorDetectorViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ColorState())
    val state: StateFlow<ColorState> = _state.asStateFlow()

    fun onColorDetected(r: Int, g: Int, b: Int) {
        if (_state.value.isCaptured) return
        updateColor(r, g, b)
    }

    fun captureBitmap(bitmap: Bitmap) {
        _state.value = _state.value.copy(
            capturedBitmap = bitmap,
            isCaptured = true
        )
    }

    fun releaseCaptured() {
        _state.value = _state.value.copy(
            capturedBitmap = null,
            isCaptured = false
        )
    }

    fun pickColorAt(x: Float, y: Float, bitmapWidth: Int, bitmapHeight: Int) {
        val bitmap = _state.value.capturedBitmap ?: return
        val px = (x * bitmap.width / bitmapWidth).toInt().coerceIn(0, bitmap.width - 1)
        val py = (y * bitmap.height / bitmapHeight).toInt().coerceIn(0, bitmap.height - 1)
        val pixel = bitmap.getPixel(px, py)
        updateColor(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
    }

    private fun updateColor(r: Int, g: Int, b: Int) {
        val hex = "#%02X%02X%02X".format(r, g, b)
        _state.value = _state.value.copy(
            colorHex = hex, r = r, g = g, b = b,
            colorName = classifyColor(r, g, b),
            isCameraReady = true
        )
    }

    private fun classifyColor(r: Int, g: Int, b: Int): String {
        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)
        val h = hsv[0]; val s = hsv[1]; val v = hsv[2]
        return when {
            v < 0.15f -> "검정"
            s < 0.1f && v > 0.85f -> "흰색"
            s < 0.15f -> "회색"
            h < 15 || h >= 345 -> "빨강"
            h < 45 -> "주황"
            h < 70 -> "노랑"
            h < 160 -> "초록"
            h < 200 -> "시안"
            h < 260 -> "파랑"
            h < 290 -> "보라"
            else -> "분홍"
        }
    }
}
