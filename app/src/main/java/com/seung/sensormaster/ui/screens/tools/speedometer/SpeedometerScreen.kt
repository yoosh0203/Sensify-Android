package com.seung.sensormaster.ui.screens.tools.speedometer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.components.AdvancedDataRow
import com.seung.sensormaster.ui.components.AdvancedPanel
import com.seung.sensormaster.ui.screens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedometerScreen(
    onBack: () -> Unit,
    viewModel: SpeedometerViewModel = hiltViewModel(),
    settingsVM: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAdvancedMode by settingsVM.advancedMode.collectAsStateWithLifecycle()

    val isKmh = state.unit == SpeedUnit.KMH
    val displaySpeed = if (isKmh) state.speedKmh else state.speed
    val displayMax = if (isKmh) state.maxSpeed else state.maxSpeed / 3.6f
    val displayMin = if (isKmh) state.minSpeed else state.minSpeed / 3.6f
    val displayAvg = if (isKmh) state.avgSpeed else state.avgSpeed / 3.6f
    val unitLabel = state.unit.label

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("속도계") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleUnit() }) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = "단위 변환")
                    }
                    IconButton(onClick = { viewModel.reset() }) {
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
            Spacer(modifier = Modifier.height(32.dp))

            // 현재 속도
            Text(
                text = if (isKmh) "%.1f".format(displaySpeed) else "%.2f".format(displaySpeed),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            // 단위 (탭하면 전환)
            TextButton(onClick = { viewModel.toggleUnit() }) {
                Text(unitLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 상태 컨텍스트
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
                Text(
                    state.context,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 최고 / 평균 카드
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("최소", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (displayMin < Float.MAX_VALUE) {
                                if (isKmh) "%.1f".format(displayMin) else "%.2f".format(displayMin)
                            } else "—",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(unitLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("평균", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isKmh) "%.1f".format(displayAvg) else "%.2f".format(displayAvg),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(unitLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("최고", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isKmh) "%.1f".format(displayMax) else "%.2f".format(displayMax),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(unitLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AdvancedPanel(isVisible = isAdvancedMode) {
                Text("가속도 상세", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                AdvancedDataRow(label = "선형 X (m/s²)", value = "%.2f".format(state.accelX))
                AdvancedDataRow(label = "선형 Y (m/s²)", value = "%.2f".format(state.accelY))
                AdvancedDataRow(label = "선형 Z (m/s²)", value = "%.2f".format(state.accelZ))
                AdvancedDataRow(label = "속도 (m/s)", value = "%.3f".format(state.speed))
                AdvancedDataRow(label = "최고 (km/h)", value = "%.1f".format(state.maxSpeed))
                AdvancedDataRow(label = "평균 (km/h)", value = "%.1f".format(state.avgSpeed))
            }
        }
    }
}
