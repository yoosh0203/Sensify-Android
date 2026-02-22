package com.seung.sensormaster.ui.screens.tools.rpm

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sqrt

data class RpmState(
    val rpm: Float = 0f,
    val frequency: Float = 0f,
    val isRecording: Boolean = false
)

@HiltViewModel
class RpmViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(RpmState())
    val state: StateFlow<RpmState> = _state.asStateFlow()

    private var recorder: AudioRecord? = null
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        if (_state.value.isRecording) return
        _state.value = _state.value.copy(isRecording = true)

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2
        )
        recorder?.startRecording()

        viewModelScope.launch(Dispatchers.Default) {
            val buffer = ShortArray(4096)
            while (isActive && _state.value.isRecording) {
                val read = recorder?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val freq = autocorrelationFrequency(buffer, read)
                    if (freq > 10f) {
                        _state.value = RpmState(
                            rpm = freq * 60f,
                            frequency = freq,
                            isRecording = true
                        )
                    }
                }
            }
        }
    }

    fun stopRecording() {
        _state.value = _state.value.copy(isRecording = false)
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    private fun autocorrelationFrequency(buffer: ShortArray, count: Int): Float {
        // Autocorrelation method for periodic signal detection
        val minLag = sampleRate / 2000   // max 2000 Hz
        val maxLag = sampleRate / 20     // min 20 Hz
        var bestLag = minLag
        var bestCorr = -1.0

        val doubles = DoubleArray(count) { buffer[it].toDouble() }
        val mean = doubles.average()
        for (i in doubles.indices) doubles[i] -= mean

        for (lag in minLag..minOf(maxLag, count / 2)) {
            var sum = 0.0
            var norm1 = 0.0
            var norm2 = 0.0
            for (i in 0 until count - lag) {
                sum += doubles[i] * doubles[i + lag]
                norm1 += doubles[i] * doubles[i]
                norm2 += doubles[i + lag] * doubles[i + lag]
            }
            val normProduct = sqrt(norm1 * norm2)
            if (normProduct > 0) {
                val corr = sum / normProduct
                if (corr > bestCorr) {
                    bestCorr = corr
                    bestLag = lag
                }
            }
        }

        return if (bestCorr > 0.4) sampleRate.toFloat() / bestLag else 0f
    }

    override fun onCleared() {
        stopRecording()
        super.onCleared()
    }
}
