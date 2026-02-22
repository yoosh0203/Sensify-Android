package com.seung.sensormaster.ui.screens.tools.pedometer

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

data class PedometerState(
    val steps: Int = 0,
    val initialSteps: Int? = null,
    val distance: Float = 0f,    // km
    val calories: Float = 0f     // kcal
)

@HiltViewModel
class PedometerViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager
) : ViewModel() {

    private val _state = MutableStateFlow(PedometerState())
    val state: StateFlow<PedometerState> = _state.asStateFlow()

    private val stepLength = 0.0007f  // ~0.7m → km

    init {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_STEP_COUNTER,
                SensorManager.SENSOR_DELAY_NORMAL
            ).collect { data ->
                val totalSteps = data.values[0].toInt()
                val current = _state.value
                val initial = current.initialSteps ?: totalSteps
                val sessionSteps = totalSteps - initial
                _state.value = current.copy(
                    steps = sessionSteps,
                    initialSteps = initial,
                    distance = sessionSteps * stepLength,
                    calories = sessionSteps * 0.04f
                )
            }
        }
    }
}
