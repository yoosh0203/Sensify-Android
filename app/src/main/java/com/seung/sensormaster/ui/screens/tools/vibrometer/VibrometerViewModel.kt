package com.seung.sensormaster.ui.screens.tools.vibrometer

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
import kotlin.math.abs
import kotlin.math.sqrt

data class VibrometerState(
    val vibration: Float = 0f,          // 진동 강도 (m/s²)
    val maxVibration: Float = 0f,
    val minVibration: Float = Float.MAX_VALUE,
    val avgVibration: Float = 0f,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val context: String = "안정",
    val waveformHistory: List<Float> = emptyList()
)

@HiltViewModel
class VibrometerViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager
) : ViewModel() {

    private val _state = MutableStateFlow(VibrometerState())
    val state: StateFlow<VibrometerState> = _state.asStateFlow()

    private val gravity = FloatArray(3)
    private val alpha = 0.8f
    private val historyMaxSize = 100

    // 평균 계산용
    private var vibSum = 0.0
    private var vibCount = 0L

    init {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME
            ).collect { data ->
                val x = data.values[0]
                val y = data.values[1]
                val z = data.values[2]

                gravity[0] = alpha * gravity[0] + (1 - alpha) * x
                gravity[1] = alpha * gravity[1] + (1 - alpha) * y
                gravity[2] = alpha * gravity[2] + (1 - alpha) * z

                val lx = x - gravity[0]
                val ly = y - gravity[1]
                val lz = z - gravity[2]
                val vibration = sqrt(lx * lx + ly * ly + lz * lz)

                val current = _state.value
                val history = current.waveformHistory.toMutableList().apply {
                    add(vibration)
                    if (size > historyMaxSize) removeFirst()
                }
                vibSum += vibration
                vibCount++
                _state.value = current.copy(
                    vibration = vibration,
                    maxVibration = maxOf(current.maxVibration, vibration),
                    minVibration = minOf(current.minVibration, vibration),
                    avgVibration = (vibSum / vibCount).toFloat(),
                    x = lx, y = ly, z = lz,
                    context = getContext(vibration),
                    waveformHistory = history
                )
            }
        }
    }

    fun resetMax() {
        vibSum = 0.0
        vibCount = 0
        _state.value = _state.value.copy(maxVibration = 0f, minVibration = Float.MAX_VALUE, avgVibration = 0f)
    }

    private fun getContext(v: Float): String = when {
        v < 0.1f -> "안정"
        v < 0.5f -> "미세 진동"
        v < 2f -> "진동 감지"
        v < 5f -> "강한 진동"
        else -> "매우 강한 진동!"
    }
}
