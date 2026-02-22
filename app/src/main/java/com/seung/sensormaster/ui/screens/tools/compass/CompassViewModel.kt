package com.seung.sensormaster.ui.screens.tools.compass

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
import kotlin.math.roundToInt

data class CompassState(
    val azimuth: Float = 0f,           // 0~360
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val direction: String = "N",
    val directionKr: String = "북",
    val accuracy: Int = 0
)

@HiltViewModel
class CompassViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager
) : ViewModel() {

    private val _state = MutableStateFlow(CompassState())
    val state: StateFlow<CompassState> = _state.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // 가속도 + 지자기 센서 사용 시의 데이터
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var hasAccel = false
    private var hasMag = false

    init {
        observeAccelerometer()
        observeMagnetometer()
    }

    private fun observeAccelerometer() {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_UI
            ).collect { data ->
                lastAccelerometer = data.values
                hasAccel = true
                updateOrientation()
            }
        }
    }

    private fun observeMagnetometer() {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_MAGNETIC_FIELD,
                SensorManager.SENSOR_DELAY_UI
            ).collect { data ->
                lastMagnetometer = data.values
                hasMag = true
                _state.value = _state.value.copy(accuracy = data.accuracy)
                updateOrientation()
            }
        }
    }

    private fun updateOrientation() {
        if (!hasAccel || !hasMag) return

        val success = SensorManager.getRotationMatrix(
            rotationMatrix, null, lastAccelerometer, lastMagnetometer
        )
        if (!success) return

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val azimuthDeg = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360) % 360).toFloat()
        val pitchDeg = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        val rollDeg = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

        _state.value = _state.value.copy(
            azimuth = azimuthDeg,
            pitch = pitchDeg,
            roll = rollDeg,
            direction = getCardinalDirection(azimuthDeg),
            directionKr = getCardinalDirectionKr(azimuthDeg)
        )
    }

    private fun getCardinalDirection(azimuth: Float): String = when {
        azimuth < 22.5f || azimuth >= 337.5f -> "N"
        azimuth < 67.5f -> "NE"
        azimuth < 112.5f -> "E"
        azimuth < 157.5f -> "SE"
        azimuth < 202.5f -> "S"
        azimuth < 247.5f -> "SW"
        azimuth < 292.5f -> "W"
        else -> "NW"
    }

    private fun getCardinalDirectionKr(azimuth: Float): String = when {
        azimuth < 22.5f || azimuth >= 337.5f -> "북"
        azimuth < 67.5f -> "북동"
        azimuth < 112.5f -> "동"
        azimuth < 157.5f -> "남동"
        azimuth < 202.5f -> "남"
        azimuth < 247.5f -> "남서"
        azimuth < 292.5f -> "서"
        else -> "북서"
    }
}
