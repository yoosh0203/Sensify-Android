package com.seung.sensormaster.ui.screens.tools.verticalspeed

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

enum class VerticalSpeedUnit(val label: String) {
    KMH("km/h"),
    M_MIN("m/min")
}

data class VerticalSpeedState(
    val altitude: Float = 0f,
    val verticalSpeed: Float = 0f,    // m/s (원시)
    val maxUp: Float = 0f,            // m/s
    val maxDown: Float = 0f,          // m/s
    val context: String = "정지",
    val unit: VerticalSpeedUnit = VerticalSpeedUnit.KMH,
    // 엘리베이터 자동 종료
    val isMoving: Boolean = false,
    val tripComplete: Boolean = false,
    val tripMaxSpeed: Float = 0f,     // m/s — 이번 이동의 최대 속도
    val tripStartAltitude: Float = 0f,
    val tripEndAltitude: Float = 0f,
    val tripFloors: Int = 0
)

@HiltViewModel
class VerticalSpeedViewModel @Inject constructor(
    private val sensorDataManager: SensorDataManager
) : ViewModel() {

    private val _state = MutableStateFlow(VerticalSpeedState())
    val state: StateFlow<VerticalSpeedState> = _state.asStateFlow()

    private var lastAltitude: Float? = null
    private var lastTimestamp = 0L
    private val smoothingFactor = 0.3f
    private var smoothedSpeed = 0f

    // 엘리베이터 감지
    private var movingStartAltitude = 0f
    private var wasMoving = false
    private var stoppedCount = 0    // 연속으로 정지 판정된 횟수

    init {
        viewModelScope.launch {
            sensorDataManager.observeSensor(
                Sensor.TYPE_PRESSURE,
                SensorManager.SENSOR_DELAY_GAME
            ).collect { data ->
                val pressure = data.values[0]
                val altitude = SensorManager.getAltitude(
                    SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure
                )

                if (lastAltitude != null && lastTimestamp > 0) {
                    val dt = (data.timestamp - lastTimestamp) / 1_000_000_000f
                    if (dt in 0.01f..2f) {
                        val rawSpeed = (altitude - lastAltitude!!) / dt
                        smoothedSpeed = smoothingFactor * rawSpeed + (1 - smoothingFactor) * smoothedSpeed
                        val current = _state.value
                        val absSpeed = abs(smoothedSpeed)
                        val isCurrentlyMoving = absSpeed > 0.15f

                        // 엘리베이터 자동 종료 로직
                        var tripComplete = current.tripComplete
                        var tripMaxSpeed = current.tripMaxSpeed
                        var tripStartAlt = current.tripStartAltitude
                        var tripEndAlt = current.tripEndAltitude
                        var tripFloors = current.tripFloors
                        var isMovingState = current.isMoving

                        if (isCurrentlyMoving) {
                            stoppedCount = 0
                            if (!wasMoving) {
                                // 움직이기 시작
                                movingStartAltitude = altitude
                                tripComplete = false
                                tripMaxSpeed = 0f
                                tripStartAlt = altitude
                            }
                            wasMoving = true
                            isMovingState = true
                            tripMaxSpeed = maxOf(tripMaxSpeed, absSpeed)
                        } else if (wasMoving) {
                            stoppedCount++
                            // 연속 15회 정지(~0.5초) 확인 후 종료 판정
                            if (stoppedCount >= 15) {
                                wasMoving = false
                                isMovingState = false
                                tripComplete = true
                                tripEndAlt = altitude
                                val altDiff = abs(altitude - movingStartAltitude)
                                tripFloors = (altDiff / 3f).toInt()  // 1층 ≈ 3m
                            }
                        }

                        _state.value = current.copy(
                            altitude = altitude,
                            verticalSpeed = smoothedSpeed,
                            maxUp = maxOf(current.maxUp, smoothedSpeed),
                            maxDown = minOf(current.maxDown, smoothedSpeed),
                            context = getContext(smoothedSpeed, tripComplete),
                            isMoving = isMovingState,
                            tripComplete = tripComplete,
                            tripMaxSpeed = tripMaxSpeed,
                            tripStartAltitude = tripStartAlt,
                            tripEndAltitude = tripEndAlt,
                            tripFloors = tripFloors
                        )
                    }
                }
                lastAltitude = altitude
                lastTimestamp = data.timestamp
            }
        }
    }

    fun toggleUnit() {
        val current = _state.value
        _state.value = current.copy(
            unit = if (current.unit == VerticalSpeedUnit.KMH) VerticalSpeedUnit.M_MIN else VerticalSpeedUnit.KMH
        )
    }

    fun resetTrip() {
        wasMoving = false
        stoppedCount = 0
        _state.value = _state.value.copy(
            tripComplete = false,
            tripMaxSpeed = 0f,
            tripFloors = 0,
            isMoving = false
        )
    }

    private fun getContext(speed: Float, tripDone: Boolean): String = when {
        tripDone -> "이동 완료"
        speed > 2f -> "빠른 상승 (엘리베이터↑)"
        speed > 0.3f -> "느린 상승"
        speed < -2f -> "빠른 하강 (엘리베이터↓)"
        speed < -0.3f -> "느린 하강"
        else -> "정지"
    }
}
