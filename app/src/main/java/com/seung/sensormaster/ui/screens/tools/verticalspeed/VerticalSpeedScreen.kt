package com.seung.sensormaster.ui.screens.tools.verticalspeed

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.components.AdvancedDataRow
import com.seung.sensormaster.ui.components.AdvancedPanel
import com.seung.sensormaster.ui.screens.settings.SettingsViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalSpeedScreen(
    onBack: () -> Unit,
    viewModel: VerticalSpeedViewModel = hiltViewModel(),
    settingsVM: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAdvancedMode by settingsVM.advancedMode.collectAsStateWithLifecycle()

    // 단위 변환
    val isKmh = state.unit == VerticalSpeedUnit.KMH
    val displaySpeed = if (isKmh) state.verticalSpeed * 3.6f else state.verticalSpeed * 60f
    val unitLabel = state.unit.label

    val speedColor = when {
        state.verticalSpeed > 0.3f -> Color(0xFF4CAF50)
        state.verticalSpeed < -0.3f -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("수직 속도계") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleUnit() }) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = "단위 변환")
                    }
                    IconButton(onClick = { viewModel.resetTrip() }) {
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

            // 수직 속도
            Text(
                text = "%+.1f".format(displaySpeed),
                style = MaterialTheme.typography.displayLarge,
                color = speedColor
            )
            TextButton(onClick = { viewModel.toggleUnit() }) {
                Text(unitLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 상태
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
                Text(
                    state.context,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 고도
            Text("%.1f m".format(state.altitude), style = MaterialTheme.typography.headlineSmall)
            Text("현재 고도", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            // 최대 상승/하강
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
                    val maxUpDisplay = if (isKmh) state.maxUp * 3.6f else state.maxUp * 60f
                    val maxDownDisplay = if (isKmh) state.maxDown * 3.6f else state.maxDown * 60f
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("최대 상승", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "%+.1f".format(maxUpDisplay),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(unitLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("최대 하강", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "%+.1f".format(maxDownDisplay),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                        Text(unitLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 엘리베이터 이동 결과 카드
            if (state.tripComplete) {
                val altDiff = state.tripEndAltitude - state.tripStartAltitude
                val direction = if (altDiff > 0) "↑ 상승" else "↓ 하강"
                val tripMaxDisplay = if (isKmh) state.tripMaxSpeed * 3.6f else state.tripMaxSpeed * 60f

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "🛗 이동 완료",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("방향", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(direction, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("이동 거리", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("%.1f m".format(abs(altDiff)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("약 층수", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${state.tripFloors}층", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("최대 속도", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("%.1f %s".format(tripMaxDisplay, unitLabel), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AdvancedPanel(isVisible = isAdvancedMode) {
                Text("수직 속도 상세", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                AdvancedDataRow(label = "수직 속도 (m/s)", value = "%+.3f".format(state.verticalSpeed))
                AdvancedDataRow(label = "최대 상승 (m/s)", value = "%.2f".format(state.maxUp))
                AdvancedDataRow(label = "최대 하강 (m/s)", value = "%.2f".format(state.maxDown))
                AdvancedDataRow(label = "현재 고도 (m)", value = "%.2f".format(state.altitude))
                AdvancedDataRow(label = "이동 중", value = if (state.isMoving) "예" else "아니오")
            }
        }
    }
}
