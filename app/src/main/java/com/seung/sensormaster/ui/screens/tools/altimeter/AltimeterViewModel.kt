package com.seung.sensormaster.ui.screens.tools.altimeter

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

data class AltimeterState(
    val pressure: Float = 1013.25f,       // hPa
    val altitude: Float = 0f,             // meters
    val referencePressure: Float = 1013.25f, // 해면기압
    val context: String = "측정 대기",
    val maxAlt: Float = Float.MIN_VALUE,
    val minAlt: Float = Float.MAX_VALUE,
    val avgAlt: Float = 0f
)

@HiltViewModel
class AltimeterViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager
) : ViewModel() {

    private val _state = MutableStateFlow(AltimeterState())
    val state: StateFlow<AltimeterState> = _state.asStateFlow()

    // 평균 계산용
    private var altSum = 0.0
    private var altCount = 0L

    init {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_PRESSURE,
                SensorManager.SENSOR_DELAY_UI
            ).collect { data ->
                val pressure = data.values[0]
                val altitude = SensorManager.getAltitude(
                    _state.value.referencePressure, pressure
                )
                val current = _state.value
                altSum += altitude
                altCount++
                _state.value = current.copy(
                    pressure = pressure,
                    altitude = altitude,
                    maxAlt = maxOf(current.maxAlt, altitude),
                    minAlt = minOf(current.minAlt, altitude),
                    avgAlt = (altSum / altCount).toFloat(),
                    context = getAltContext(altitude)
                )
            }
        }
    }

    fun setReferencePressure() {
        _state.value = _state.value.copy(referencePressure = _state.value.pressure)
    }

    private fun getAltContext(alt: Float): String = when {
        alt < 0 -> "해수면 이하"
        alt < 100 -> "저지대"
        alt < 500 -> "구릉지대"
        alt < 1500 -> "산악지대"
        alt < 3000 -> "고산지대"
        else -> "초고산지대"
    }
}
