package com.seung.sensormaster.ui.screens.tools.metal

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seung.sensormaster.data.sensor.SensorDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

data class MetalDetectorState(
    val magnitude: Float = 0f,       // μT (마이크로테슬라)
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val baseline: Float = 0f,        // 보정된 기준선
    val deviation: Float = 0f,       // baseline과의 차이
    val intensity: Float = 0f,       // 0..1 정규화 강도
    val isCalibrated: Boolean = false
)

@HiltViewModel
class MetalDetectorViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    private var lastHapticTime = 0L

    private val _state = MutableStateFlow(MetalDetectorState())
    val state: StateFlow<MetalDetectorState> = _state.asStateFlow()

    // 캘리브레이션 데이터
    private val calibrationSamples = mutableListOf<Float>()
    private var isCalibrating = true
    private val calibrationCount = 30

    init {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_MAGNETIC_FIELD,
                SensorManager.SENSOR_DELAY_GAME
            ).collect { data ->
                val x = data.values[0]
                val y = data.values[1]
                val z = data.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)

                if (isCalibrating) {
                    calibrationSamples.add(magnitude)
                    if (calibrationSamples.size >= calibrationCount) {
                        val baseline = calibrationSamples.average().toFloat()
                        isCalibrating = false
                        _state.value = _state.value.copy(
                            baseline = baseline,
                            isCalibrated = true
                        )
                    }
                    return@collect
                }

                val current = _state.value
                val deviation = magnitude - current.baseline
                // 강도 정규화: deviation 200μT까지를 0..1
                val intensity = (kotlin.math.abs(deviation) / 200f).coerceIn(0f, 1f)

                _state.value = current.copy(
                    magnitude = magnitude,
                    x = x, y = y, z = z,
                    deviation = deviation,
                    intensity = intensity
                )

                // 햅틱 피드백 (intensity에 비례)
                if (intensity > 0.1f) {
                    val currentTime = System.currentTimeMillis()
                    // intensity가 높을수록 더 자주 진동 (0.1 -> 300ms, 1.0 -> 50ms)
                    val interval = (300 - (intensity * 250)).toLong()
                    if (currentTime - lastHapticTime > interval) {
                        lastHapticTime = currentTime
                        try {
                            val amplitude = (intensity * 255).toInt().coerceIn(1, 255)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(30, amplitude))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(30)
                            }
                        } catch (_: Exception) {
                            // 진동 실패 시 무시 — 기능은 계속 동작
                        }
                    }
                }
            }
        }
    }

    fun recalibrate() {
        calibrationSamples.clear()
        isCalibrating = true
        _state.value = _state.value.copy(isCalibrated = false)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            vibrator?.cancel()
        } catch (_: Exception) { }
    }
}
