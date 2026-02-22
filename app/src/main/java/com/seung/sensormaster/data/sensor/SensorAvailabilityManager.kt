package com.seung.sensormaster.data.sensor

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 기기의 센서 가용성을 최초 설치 시 감지하여 SharedPreferences에 저장.
 * 이후 앱 실행 시에는 캐시된 결과를 사용.
 */
@Singleton
class SensorAvailabilityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sensor_availability", Context.MODE_PRIVATE)
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    companion object {
        private const val KEY_INITIALIZED = "sensor_check_initialized"
        private const val KEY_PREFIX = "sensor_available_"
    }

    // 사용하는 센서 타입 목록
    private val sensorTypesToCheck = listOf(
        Sensor.TYPE_ACCELEROMETER,
        Sensor.TYPE_MAGNETIC_FIELD,
        Sensor.TYPE_LIGHT,
        Sensor.TYPE_PRESSURE,
        Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_STEP_COUNTER
    )

    /**
     * 최초 실행 시 센서 가용성 감지 및 저장.
     * 이미 감지 완료된 경우 아무 작업도 하지 않음.
     */
    fun initializeIfNeeded() {
        if (prefs.getBoolean(KEY_INITIALIZED, false)) return

        val editor = prefs.edit()
        for (type in sensorTypesToCheck) {
            val available = sensorManager.getDefaultSensor(type) != null
            editor.putBoolean("$KEY_PREFIX$type", available)
        }
        editor.putBoolean(KEY_INITIALIZED, true)
        editor.apply()
    }

    /**
     * 특정 센서 타입이 사용 가능한지 확인.
     * null 타입 (센서 불필요 도구)은 항상 true 반환.
     */
    fun isSensorAvailable(sensorType: Int?): Boolean {
        if (sensorType == null) return true
        return prefs.getBoolean("$KEY_PREFIX$sensorType", true) // 기본값 true (안전 폴백)
    }

    /**
     * 사용 가능한 센서 목록 (디버깅/기기정보용)
     */
    fun getAvailableSensors(): Map<Int, Boolean> {
        return sensorTypesToCheck.associateWith { isSensorAvailable(it) }
    }
}
