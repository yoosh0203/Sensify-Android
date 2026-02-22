package com.seung.sensormaster.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 센서 데이터 래퍼
 */
data class SensorData(
    val values: FloatArray,
    val accuracy: Int,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SensorData) return false
        return values.contentEquals(other.values) && accuracy == other.accuracy && timestamp == other.timestamp
    }
    override fun hashCode(): Int = values.contentHashCode() * 31 + accuracy * 31 + timestamp.hashCode()
}

/**
 * lifecycle-aware 센서 매니저.
 * Flow로 센서 데이터를 스트림하며, collect가 취소되면 자동으로 리스너 해제.
 */
@Singleton
class SensorDataManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /**
     * 특정 센서 타입의 데이터를 Flow로 제공합니다.
     * @param sensorType e.g., Sensor.TYPE_ACCELEROMETER
     * @param samplingPeriod e.g., SensorManager.SENSOR_DELAY_UI
     */
    fun observeSensor(
        sensorType: Int,
        samplingPeriod: Int = SensorManager.SENSOR_DELAY_UI
    ): Flow<SensorData> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor == null) {
            close(IllegalStateException("Sensor type $sensorType not available"))
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(
                    SensorData(
                        values = event.values.copyOf(),
                        accuracy = event.accuracy,
                        timestamp = event.timestamp
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, samplingPeriod)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    /**
     * 기기에서 사용 가능한 센서 목록
     */
    fun getAvailableSensors(): List<Sensor> =
        sensorManager.getSensorList(Sensor.TYPE_ALL)

    /**
     * 특정 센서 사용 가능 여부
     */
    fun isSensorAvailable(sensorType: Int): Boolean =
        sensorManager.getDefaultSensor(sensorType) != null
}
