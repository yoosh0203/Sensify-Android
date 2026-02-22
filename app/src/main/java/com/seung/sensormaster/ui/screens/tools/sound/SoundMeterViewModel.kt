package com.seung.sensormaster.ui.screens.tools.sound

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

data class SoundMeterState(
    val db: Float = 0f,
    val maxDb: Float = 0f,
    val minDb: Float = Float.MAX_VALUE,
    val avgDb: Float = 0f,
    val context: String = "측정 대기",
    val isRecording: Boolean = false,
    val fftMagnitudes: FloatArray = FloatArray(0),
    val dbHistory: List<Float> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SoundMeterState) return false
        return db == other.db && maxDb == other.maxDb && minDb == other.minDb &&
                avgDb == other.avgDb && context == other.context && isRecording == other.isRecording &&
                fftMagnitudes.contentEquals(other.fftMagnitudes) &&
                dbHistory == other.dbHistory
    }
    override fun hashCode(): Int = db.hashCode()
}

@HiltViewModel
class SoundMeterViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SoundMeterState())
    val state: StateFlow<SoundMeterState> = _state.asStateFlow()

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) { null }

    private var audioRecord: AudioRecord? = null
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    // 평균 계산용
    private var dbSum = 0.0
    private var dbCount = 0L
    // 진동 피드백용
    private var lastVibTime = 0L

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        if (_state.value.isRecording) return

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        _state.value = _state.value.copy(isRecording = true)

        viewModelScope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize / 2)
            while (isActive && _state.value.isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: break
                if (read > 0) {
                    val db = calculateDb(buffer, read)
                    val fft = calculateSimpleFFT(buffer, read)
                    val current = _state.value

                    // 평균 축적
                    if (db > 0) {
                        dbSum += db
                        dbCount++
                    }
                    val avgDb = if (dbCount > 0) (dbSum / dbCount).toFloat() else 0f

                    // dB 히스토리 (시간축 파형용)
                    val history = current.dbHistory.toMutableList().apply {
                        add(db)
                        if (size > 150) removeFirst()
                    }

                    _state.value = current.copy(
                        db = db,
                        maxDb = maxOf(current.maxDb, db),
                        minDb = if (db > 0) minOf(current.minDb, db) else current.minDb,
                        avgDb = avgDb,
                        context = getDbContext(db),
                        fftMagnitudes = fft,
                        dbHistory = history
                    )

                    // 85dB 이상 시 진동 경고 (1초 간격)
                    if (db >= 85f) {
                        val now = System.currentTimeMillis()
                        if (now - lastVibTime > 1000) {
                            lastVibTime = now
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(100)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        }
    }

    fun stopRecording() {
        _state.value = _state.value.copy(isRecording = false)
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun calculateDb(buffer: ShortArray, size: Int): Float {
        var sum = 0.0
        for (i in 0 until size) {
            sum += buffer[i].toDouble() * buffer[i].toDouble()
        }
        val rms = sqrt(sum / size)
        return if (rms > 0) (20 * log10(rms / 32768.0) + 90).toFloat().coerceIn(0f, 130f) else 0f
    }

    /**
     * 간이 FFT: 대역별 에너지 (16 밴드)
     */
    private fun calculateSimpleFFT(buffer: ShortArray, size: Int): FloatArray {
        val bands = 16
        val bandSize = size / bands
        val magnitudes = FloatArray(bands)
        for (band in 0 until bands) {
            var sum = 0.0
            val start = band * bandSize
            val end = minOf(start + bandSize, size)
            for (i in start until end) {
                sum += abs(buffer[i].toInt())
            }
            magnitudes[band] = (sum / (end - start)).toFloat()
        }
        // 정규화
        val max = magnitudes.maxOrNull() ?: 1f
        if (max > 0) {
            for (i in magnitudes.indices) {
                magnitudes[i] = magnitudes[i] / max
            }
        }
        return magnitudes
    }

    private fun getDbContext(db: Float): String = when {
        db < 20 -> "거의 무음"
        db < 40 -> "조용한 도서관"
        db < 55 -> "보통 실내"
        db < 65 -> "일반 대화"
        db < 75 -> "번화가 수준"
        db < 85 -> "시끄러운 환경"
        db < 100 -> "⚠️ 청력 주의"
        else -> "🔴 매우 위험!"
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }
}
