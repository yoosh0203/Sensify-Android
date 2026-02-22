package com.seung.sensormaster.ui.screens.tools.tone

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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
import kotlin.math.PI
import kotlin.math.sin

data class ToneGeneratorState(
    val frequency: Float = 440f,     // Hz
    val isPlaying: Boolean = false,
    val waveform: String = "Sine"    // Sine, Square, Sawtooth
)

@HiltViewModel
class ToneGeneratorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ToneGeneratorState())
    val state: StateFlow<ToneGeneratorState> = _state.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100

    fun setFrequency(freq: Float) {
        _state.value = _state.value.copy(frequency = freq.coerceIn(20f, 20000f))
    }

    fun setWaveform(wf: String) {
        _state.value = _state.value.copy(waveform = wf)
    }

    fun togglePlay() {
        if (_state.value.isPlaying) stop() else play()
    }

    private fun play() {
        _state.value = _state.value.copy(isPlaying = true)

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        viewModelScope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize / 2)
            var phase = 0.0

            while (isActive && _state.value.isPlaying) {
                val freq = _state.value.frequency.toDouble()
                val waveform = _state.value.waveform
                val phaseIncrement = 2.0 * PI * freq / sampleRate

                for (i in buffer.indices) {
                    val sample = when (waveform) {
                        "Square" -> if (sin(phase) >= 0) 1.0 else -1.0
                        "Sawtooth" -> 2.0 * (phase / (2.0 * PI) - kotlin.math.floor(phase / (2.0 * PI))) - 1.0
                        else -> sin(phase)  // Sine
                    }
                    buffer[i] = (sample * Short.MAX_VALUE * 0.7).toInt().toShort()
                    phase += phaseIncrement
                    if (phase >= 2.0 * PI) phase -= 2.0 * PI
                }

                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    private fun stop() {
        _state.value = _state.value.copy(isPlaying = false)
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}
