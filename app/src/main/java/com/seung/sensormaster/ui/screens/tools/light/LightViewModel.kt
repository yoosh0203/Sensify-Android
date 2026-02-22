package com.seung.sensormaster.ui.screens.tools.light

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

data class LightState(
    val lux: Float = 0f,
    val maxLux: Float = 0f,
    val minLux: Float = Float.MAX_VALUE,
    val avgLux: Float = 0f,
    val context: String = "측정 중...",
    val contextEmoji: String = "💡"
)

@HiltViewModel
class LightViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager
) : ViewModel() {

    private val _state = MutableStateFlow(LightState())
    val state: StateFlow<LightState> = _state.asStateFlow()

    // 평균 계산용
    private var luxSum = 0.0
    private var luxCount = 0L

    init {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_LIGHT,
                SensorManager.SENSOR_DELAY_UI
            ).collect { data ->
                val lux = data.values[0]
                val current = _state.value
                luxSum += lux
                luxCount++
                _state.value = current.copy(
                    lux = lux,
                    maxLux = maxOf(current.maxLux, lux),
                    minLux = minOf(current.minLux, lux),
                    avgLux = (luxSum / luxCount).toFloat(),
                    context = getLuxContext(lux),
                    contextEmoji = getLuxEmoji(lux)
                )
            }
        }
    }

    private fun getLuxContext(lux: Float): String = when {
        lux < 1 -> "칠흑같은 어둠"
        lux < 10 -> "달빛 수준"
        lux < 50 -> "어두운 실내"
        lux < 200 -> "거실 조명"
        lux < 500 -> "사무실 조명"
        lux < 1000 -> "밝은 실내"
        lux < 5000 -> "흐린 날 야외"
        lux < 20000 -> "맑은 날 그늘"
        lux < 50000 -> "맑은 날 햇빛"
        else -> "직사광선"
    }

    private fun getLuxEmoji(lux: Float): String = when {
        lux < 10 -> "🌙"
        lux < 200 -> "💡"
        lux < 1000 -> "🏠"
        lux < 10000 -> "⛅"
        else -> "☀️"
    }
}
