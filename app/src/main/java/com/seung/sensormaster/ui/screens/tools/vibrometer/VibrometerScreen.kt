package com.seung.sensormaster.ui.screens.tools.vibrometer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.components.AdvancedDataRow
import com.seung.sensormaster.ui.components.AdvancedPanel
import com.seung.sensormaster.ui.screens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibrometerScreen(
    onBack: () -> Unit,
    viewModel: VibrometerViewModel = hiltViewModel(),
    settingsVM: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAdvancedMode by settingsVM.advancedMode.collectAsStateWithLifecycle()

    val waveColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("진동 측정기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetMax() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "리셋")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "%.2f".format(state.vibration),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text("m/s²", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))

            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
                Text(state.context, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 파형 그래프 ──
            if (state.waveformHistory.isNotEmpty()) {
                Text("실시간 파형", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val data = state.waveformHistory
                    if (data.size < 2) return@Canvas

                    val maxVal = data.maxOrNull()?.coerceAtLeast(1f) ?: 1f
                    val stepX = size.width / (data.size - 1)

                    val path = Path().apply {
                        data.forEachIndexed { idx, value ->
                            val x = idx * stepX
                            val y = size.height - (value / maxVal * size.height).coerceIn(0f, size.height)
                            if (idx == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }

                    // 기준선
                    drawLine(
                        color = waveColor.copy(alpha = 0.15f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )

                    drawPath(
                        path = path,
                        color = waveColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("최소", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (state.minVibration < Float.MAX_VALUE) "%.2f".format(state.minVibration) else "—",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("평균", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("%.2f".format(state.avgVibration), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("최대", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    Text("%.2f".format(state.maxVibration), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                }
            }
            Text("m/s²", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.weight(1f))

            AdvancedPanel(isVisible = isAdvancedMode) {
                Text("진동 상세", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                AdvancedDataRow(label = "진동 (m/s²)", value = "%.4f".format(state.vibration))
                AdvancedDataRow(label = "X축 (m/s²)", value = "%.4f".format(state.x))
                AdvancedDataRow(label = "Y축 (m/s²)", value = "%.4f".format(state.y))
                AdvancedDataRow(label = "Z축 (m/s²)", value = "%.4f".format(state.z))
                AdvancedDataRow(label = "최대 (m/s²)", value = "%.4f".format(state.maxVibration))
                AdvancedDataRow(label = "최소 (m/s²)", value = if (state.minVibration < Float.MAX_VALUE) "%.4f".format(state.minVibration) else "—")
                AdvancedDataRow(label = "평균 (m/s²)", value = "%.4f".format(state.avgVibration))
            }
        }
    }
}
