package com.seung.sensormaster.ui.screens.tools.tone

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.ln
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToneGeneratorScreen(
    onBack: () -> Unit,
    viewModel: ToneGeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 로그 스케일 슬라이더 (20Hz ~ 20kHz)
    val logMin = ln(20f)
    val logMax = ln(20000f)
    var sliderPosition by remember { mutableFloatStateOf((ln(state.frequency) - logMin) / (logMax - logMin)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("주파수 발생기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── 주파수 표시 ──
            Text(
                text = if (state.frequency >= 1000)
                    "%.2f".format(state.frequency / 1000) + " kHz"
                else "${state.frequency.roundToInt()} Hz",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── 음계 가이드 ──
            val noteInfo = getNoteInfo(state.frequency)
            Text(
                text = noteInfo,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── 주파수 슬라이더 (로그 스케일) ──
            Text("주파수", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = sliderPosition,
                onValueChange = { pos ->
                    sliderPosition = pos
                    val freq = kotlin.math.exp(logMin + pos * (logMax - logMin))
                    viewModel.setFrequency(freq)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("20 Hz", style = MaterialTheme.typography.labelSmall)
                Text("20 kHz", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 파형 선택 ──
            Text("파형", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("Sine", "Square", "Sawtooth").forEachIndexed { idx, wf ->
                    SegmentedButton(
                        selected = state.waveform == wf,
                        onClick = { viewModel.setWaveform(wf) },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = 3)
                    ) {
                        Text(wf)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── 재생 버튼 ──
            FloatingActionButton(
                onClick = { viewModel.togglePlay() },
                containerColor = if (state.isPlaying)
                    MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                    contentDescription = if (state.isPlaying) "중지" else "재생",
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 프리셋 버튼 ──
            Text("프리셋", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    "A4" to 440f,
                    "C5" to 523.25f,
                    "1kHz" to 1000f,
                    "15kHz" to 15000f
                ).forEach { (label, freq) ->
                    AssistChip(
                        onClick = {
                            viewModel.setFrequency(freq)
                            sliderPosition = (ln(freq) - logMin) / (logMax - logMin)
                        },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}

private fun getNoteInfo(freq: Float): String {
    val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val a4 = 440.0
    val semitones = 12 * (ln(freq / a4.toFloat()) / ln(2f))
    val noteIndex = ((semitones + 69).roundToInt() % 12 + 12) % 12
    val octave = ((semitones + 69).roundToInt() / 12) - 1
    return "${notes[noteIndex]}$octave"
}
