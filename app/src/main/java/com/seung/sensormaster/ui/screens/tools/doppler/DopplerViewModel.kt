package com.seung.sensormaster.ui.screens.tools.doppler

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class DopplerState(
    val velocity: Float = 0f,     // m/s
    val direction: String = "정지",
    val shiftHz: Float = 0f,
    val isActive: Boolean = false
)

@HiltViewModel
class DopplerViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DopplerState())
    val state: StateFlow<DopplerState> = _state.asStateFlow()

    private var recorder: AudioRecord? = null
    private var toneTrack: AudioTrack? = null
    private var emitJob: Job? = null
    private var recordJob: Job? = null
    private val sampleRate = 44100
    private val emitFreq = 18000f  // 18kHz (near-ultrasonic)
    private val speedOfSound = 343f

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start() {
        if (_state.value.isActive) return
        _state.value = _state.value.copy(isActive = true)

        // Emit tone
        val bufSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        toneTrack = AudioTrack.Builder()
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(bufSize * 2)
            .build()

        emitJob = viewModelScope.launch(Dispatchers.Default) {
            val buffer = ShortArray(bufSize / 2)
            try {
                toneTrack?.play()
                var phase = 0.0
                val phaseInc = 2.0 * Math.PI * emitFreq / sampleRate
                while (isActive) {
                    for (i in buffer.indices) {
                        buffer[i] = (kotlin.math.sin(phase) * Short.MAX_VALUE).toInt().toShort()
                        phase += phaseInc
                    }
                    toneTrack?.write(buffer, 0, buffer.size)
                }
            } catch (_: Exception) {}
        }

        // Record and detect shift
        val recBuf = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC, sampleRate,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recBuf * 2
        )
        recorder?.startRecording()

        recordJob = viewModelScope.launch(Dispatchers.Default) {
            val audio = ShortArray(4096)
            try {
                while (isActive) {
                    val read = recorder?.read(audio, 0, audio.size) ?: 0
                    if (read > 0) {
                        val peakFreq = findPeakFrequency(audio, read)
                        val shift = peakFreq - emitFreq
                        val velocity = (shift / emitFreq) * speedOfSound / 2f
                        val dir = when {
                            velocity > 0.15f -> "접근 중"
                            velocity < -0.15f -> "멀어짐"
                            else -> "정지"
                        }
                        _state.value = DopplerState(
                            velocity = abs(velocity),
                            direction = dir,
                            shiftHz = shift,
                            isActive = true
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        // 1) 먼저 코루틴 취소 → 오디오 접근 중지
        emitJob?.cancel()
        recordJob?.cancel()
        emitJob = null
        recordJob = null

        // 2) 안전하게 리소스 해제
        try { recorder?.stop() } catch (_: Exception) {}
        try { recorder?.release() } catch (_: Exception) {}
        recorder = null

        try { toneTrack?.stop() } catch (_: Exception) {}
        try { toneTrack?.release() } catch (_: Exception) {}
        toneTrack = null

        _state.value = _state.value.copy(isActive = false)
    }

    private fun findPeakFrequency(buffer: ShortArray, count: Int): Float {
        val n = Integer.highestOneBit(count)
        val real = DoubleArray(n)
        val imag = DoubleArray(n)
        for (i in 0 until n) real[i] = buffer[i].toDouble()

        fft(real, imag, false)

        val binLow = (emitFreq - 500f) * n / sampleRate
        val binHigh = (emitFreq + 500f) * n / sampleRate
        var maxMag = 0.0
        var maxBin = binLow.toInt()
        for (i in binLow.toInt()..minOf(binHigh.toInt(), n / 2)) {
            val mag = real[i] * real[i] + imag[i] * imag[i]
            if (mag > maxMag) { maxMag = mag; maxBin = i }
        }
        return maxBin.toFloat() * sampleRate / n
    }

    private fun fft(real: DoubleArray, imag: DoubleArray, inverse: Boolean) {
        val n = real.size
        var j = 0
        for (i in 0 until n) {
            if (j > i) {
                var t = real[j]; real[j] = real[i]; real[i] = t
                t = imag[j]; imag[j] = imag[i]; imag[i] = t
            }
            var m = n / 2
            while (m >= 1 && j >= m) { j -= m; m /= 2 }
            j += m
        }
        var mmax = 1
        while (mmax < n) {
            val step = mmax * 2
            val angle = (if (inverse) Math.PI else -Math.PI) / mmax
            val wReal = kotlin.math.cos(angle)
            val wImag = kotlin.math.sin(angle)
            var wr = 1.0; var wi = 0.0
            for (m2 in 0 until mmax) {
                for (i in m2 until n step step) {
                    val k = i + mmax
                    val tr = wr * real[k] - wi * imag[k]
                    val ti = wr * imag[k] + wi * real[k]
                    real[k] = real[i] - tr; imag[k] = imag[i] - ti
                    real[i] += tr; imag[i] += ti
                }
                val newWr = wr * wReal - wi * wImag
                wi = wr * wImag + wi * wReal
                wr = newWr
            }
            mmax = step
        }
    }

    override fun onCleared() { stop(); super.onCleared() }
}
