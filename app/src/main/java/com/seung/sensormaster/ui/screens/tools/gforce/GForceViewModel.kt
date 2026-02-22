package com.seung.sensormaster.ui.screens.tools.gforce

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seung.sensormaster.data.sensor.SensorDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

data class GForceState(
    val totalG: Float = 1f,
    val gX: Float = 0f,
    val gY: Float = 0f,
    val gZ: Float = 0f,
    val maxG: Float = 1f,
    val minG: Float = Float.MAX_VALUE,
    val avgG: Float = 1f,
    val context: String = "정상"
)

@HiltViewModel
class GForceViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager
) : ViewModel() {

    private val _state = MutableStateFlow(GForceState())
    val state: StateFlow<GForceState> = _state.asStateFlow()

    companion object {
        private const val GRAVITY = 9.80665f
    }

    // 평균 계산용
    private var gSum = 0.0
    private var gCount = 0L

    init {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME
            ).collect { data ->
                val x = data.values[0]
                val y = data.values[1]
                val z = data.values[2]
                val total = sqrt(x * x + y * y + z * z) / GRAVITY
                val current = _state.value
                gSum += total
                gCount++
                _state.value = current.copy(
                    totalG = total,
                    gX = x / GRAVITY,
                    gY = y / GRAVITY,
                    gZ = z / GRAVITY,
                    maxG = maxOf(current.maxG, total),
                    minG = minOf(current.minG, total),
                    avgG = (gSum / gCount).toFloat(),
                    context = getContext(total)
                )
            }
        }
    }

    fun resetMax() {
        gSum = 0.0
        gCount = 0
        _state.value = _state.value.copy(maxG = _state.value.totalG, minG = _state.value.totalG, avgG = _state.value.totalG)
    }

    private fun getContext(g: Float): String = when {
        g < 0.3f -> "무중력 상태!"
        g < 0.8f -> "자유 낙하"
        g in 0.8f..1.2f -> "정상 (1G)"
        g < 2f -> "약한 가속"
        g < 4f -> "강한 가속 (놀이기구)"
        g < 6f -> "⚠️ 고G (전투기급)"
        else -> "🔴 위험한 G-Force!"
    }
}
