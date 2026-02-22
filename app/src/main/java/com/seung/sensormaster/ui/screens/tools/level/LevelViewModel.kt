package com.seung.sensormaster.ui.screens.tools.level

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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

data class LevelState(
    val pitch: Float = 0f,      // 전후 기울기 (도)
    val roll: Float = 0f,       // 좌우 기울기 (도)
    val isLevel: Boolean = false, // 수평 여부 (±1° 이내)
    val bubbleX: Float = 0f,    // -1..1 정규화
    val bubbleY: Float = 0f     // -1..1 정규화
)

@HiltViewModel
class LevelViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(LevelState())
    val state: StateFlow<LevelState> = _state.asStateFlow()

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) { null }

    // 경험적 smoothing factor
    private val alpha = 0.15f
    private var smoothX = 0f
    private var smoothY = 0f
    private var smoothZ = 0f
    private var wasLevel = false

    init {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_UI
            ).collect { data ->
                // 로우패스 필터 (떨림 제거)
                smoothX = alpha * data.values[0] + (1 - alpha) * smoothX
                smoothY = alpha * data.values[1] + (1 - alpha) * smoothY
                smoothZ = alpha * data.values[2] + (1 - alpha) * smoothZ

                val pitch = Math.toDegrees(
                    atan2(smoothY.toDouble(), sqrt((smoothX * smoothX + smoothZ * smoothZ).toDouble()))
                ).toFloat()
                val roll = Math.toDegrees(
                    atan2(-smoothX.toDouble(), sqrt((smoothY * smoothY + smoothZ * smoothZ).toDouble()))
                ).toFloat()

                // 수평 여부: ±1도
                val isLevel = abs(pitch) < 1f && abs(roll) < 1f

                // 수평 달성 시 햅틱 피드백 (이전에 수평이 아니었을 때만)
                if (isLevel && !wasLevel) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(50)
                        }
                    } catch (_: Exception) {}
                }
                wasLevel = isLevel

                // 버블 위치: ±45도를 -1..1로 clamp
                val bx = (roll / 45f).coerceIn(-1f, 1f)
                val by = (pitch / 45f).coerceIn(-1f, 1f)

                _state.value = LevelState(
                    pitch = pitch,
                    roll = roll,
                    isLevel = isLevel,
                    bubbleX = bx,
                    bubbleY = by
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try { vibrator?.cancel() } catch (_: Exception) {}
    }
}
