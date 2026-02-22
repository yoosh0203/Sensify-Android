package com.seung.sensormaster.ui.screens.tools.protractor

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
import kotlin.math.atan2

data class ProtractorState(
    val angle: Float = 0f,          // 현재 각도 (0..360)
    val relativeAngle: Float = 0f,  // 기준점 대비 각도
    val offset: Float = 0f          // 기준점 오프셋
)

@HiltViewModel
class ProtractorViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProtractorState())
    val state: StateFlow<ProtractorState> = _state.asStateFlow()

    // 로우패스 필터용
    private val alpha = 0.15f
    private var smoothX = 0f
    private var smoothY = 0f

    init {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_UI
            ).collect { data ->
                val x = data.values[0]
                val y = data.values[1]

                // 필터링
                smoothX = alpha * x + (1 - alpha) * smoothX
                smoothY = alpha * y + (1 - alpha) * smoothY

                // 각도 계산 (라디안 -> 도)
                // 기기를 세웠을 때 Y축이 중력 방향인 점을 고려
                var angleDeg = Math.toDegrees(atan2(smoothY.toDouble(), smoothX.toDouble())).toFloat()
                
                // 0..360 범위로 정규화 (90도 회전시켜서 위쪽을 0도로 맞춤)
                angleDeg = (angleDeg + 90f + 360f) % 360f

                val current = _state.value
                val relative = (angleDeg - current.offset + 360f) % 360f

                _state.value = current.copy(
                    angle = angleDeg,
                    relativeAngle = relative
                )
            }
        }
    }

    fun setZero() {
        val currentAngle = _state.value.angle
        _state.value = _state.value.copy(
            offset = currentAngle,
            relativeAngle = 0f
        )
    }

    fun resetZero() {
        _state.value = _state.value.copy(
            offset = 0f,
            relativeAngle = _state.value.angle
        )
    }
}
