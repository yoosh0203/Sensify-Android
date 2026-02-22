package com.seung.sensormaster.ui.screens.tools.speedometer

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

enum class SpeedUnit(val label: String) {
    KMH("km/h"),
    MS("m/s")
}

data class SpeedometerState(
    val speed: Float = 0f,        // m/s (원시)
    val speedKmh: Float = 0f,     // km/h
    val maxSpeed: Float = 0f,     // km/h
    val minSpeed: Float = Float.MAX_VALUE, // km/h
    val avgSpeed: Float = 0f,     // km/h
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val context: String = "정지",
    val unit: SpeedUnit = SpeedUnit.KMH
)

@HiltViewModel
class SpeedometerViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager
) : ViewModel() {

    private val _state = MutableStateFlow(SpeedometerState())
    val state: StateFlow<SpeedometerState> = _state.asStateFlow()

    private var lastTimestamp = 0L
    private var velocity = 0f
    private val alpha = 0.8f
    private val gravity = FloatArray(3)

    // 평균 계산용
    private var speedSum = 0.0
    private var speedCount = 0L

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

                val linearX = x - gravity[0]
                val linearY = y - gravity[1]
                val linearZ = z - gravity[2]
                val accel = sqrt(linearX * linearX + linearY * linearY + linearZ * linearZ)

                if (lastTimestamp > 0) {
                    val dt = (data.timestamp - lastTimestamp) / 1_000_000_000f
                    if (dt in 0.001f..0.5f) {
                        if (accel > 0.3f) {
                            velocity += accel * dt
                        } else {
                            velocity *= 0.95f
                        }
                        velocity = velocity.coerceIn(0f, 100f)
                    }
                }
                lastTimestamp = data.timestamp

                val speedKmh = velocity * 3.6f

                // 평균 축적 (속도 > 0.5 km/h 일 때만)
                if (speedKmh > 0.5f) {
                    speedSum += speedKmh
                    speedCount++
                }
                val avgKmh = if (speedCount > 0) (speedSum / speedCount).toFloat() else 0f

                val current = _state.value
                _state.value = current.copy(
                    speed = velocity,
                    speedKmh = speedKmh,
                    maxSpeed = maxOf(current.maxSpeed, speedKmh),
                    minSpeed = if (speedKmh > 0.5f) minOf(current.minSpeed, speedKmh) else current.minSpeed,
                    avgSpeed = avgKmh,
                    accelX = linearX,
                    accelY = linearY,
                    accelZ = linearZ,
                    context = getSpeedContext(speedKmh)
                )
            }
        }
    }

    fun toggleUnit() {
        val current = _state.value
        _state.value = current.copy(
            unit = if (current.unit == SpeedUnit.KMH) SpeedUnit.MS else SpeedUnit.KMH
        )
    }

    fun reset() {
        velocity = 0f
        speedSum = 0.0
        speedCount = 0L
        _state.value = SpeedometerState(unit = _state.value.unit)
    }

    private fun getSpeedContext(kmh: Float): String = when {
        kmh < 1 -> "정지"
        kmh < 5 -> "걷기"
        kmh < 12 -> "달리기"
        kmh < 30 -> "자전거"
        kmh < 60 -> "시내 주행"
        kmh < 120 -> "고속 주행"
        else -> "초고속"
    }
}
