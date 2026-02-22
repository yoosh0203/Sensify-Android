package com.seung.sensormaster.ui.screens.tools.metal

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.components.AdvancedDataRow
import com.seung.sensormaster.ui.components.AdvancedPanel
import com.seung.sensormaster.ui.screens.settings.SettingsViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetalDetectorScreen(
    onBack: () -> Unit,
    viewModel: MetalDetectorViewModel = hiltViewModel(),
    settingsVM: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAdvancedMode by settingsVM.advancedMode.collectAsStateWithLifecycle()

    val animatedIntensity by animateFloatAsState(
        targetValue = state.intensity,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "metal_intensity"
    )

    // 강도에 따른 색상 변화 (초록→노랑→빨강)
    val gaugeColor = when {
        state.intensity < 0.3f -> Color(0xFF4CAF50)
        state.intensity < 0.6f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("금속 탐지기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.recalibrate() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "재보정")
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
            // 고급모드는 설정에서 관리

            Spacer(modifier = Modifier.height(16.dp))

            if (!state.isCalibrated) {
                // 캘리브레이션 중
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "보정 중... 금속에서 떨어뜨린 상태로 기다려주세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // ── 자기장 값 ──
                Text(
                    text = "${state.magnitude.roundToInt()}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "μT",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── 편차 인사이트 ──
                val deviationText = when {
                    abs(state.deviation) < 5 -> "금속 미감지"
                    abs(state.deviation) < 30 -> "약한 금속 반응"
                    abs(state.deviation) < 80 -> "금속 감지됨!"
                    else -> "강한 금속 반응!"
                }
                Surface(
                    color = if (abs(state.deviation) > 30)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = deviationText,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (abs(state.deviation) > 30)
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── 세로 바 게이지 ──
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(60.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = size.width
                        val barHeight = size.height
                        val cornerR = 12.dp.toPx()

                        // 트랙
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset.Zero,
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(cornerR, cornerR)
                        )

                        // 진행 바 (아래에서 위로)
                        val fillHeight = barHeight * animatedIntensity
                        drawRoundRect(
                            color = gaugeColor,
                            topLeft = Offset(0f, barHeight - fillHeight),
                            size = Size(barWidth, fillHeight),
                            cornerRadius = CornerRadius(cornerR, cornerR)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 고급 모드 ──
            AdvancedPanel(isVisible = isAdvancedMode) {
                Text("자기장 상세", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                AdvancedDataRow(label = "총 자기장 (μT)", value = "%.1f".format(state.magnitude))
                AdvancedDataRow(label = "기준선 (μT)", value = "%.1f".format(state.baseline))
                AdvancedDataRow(label = "편차 (μT)", value = "%+.1f".format(state.deviation))
                AdvancedDataRow(label = "X축 (μT)", value = "%.1f".format(state.x))
                AdvancedDataRow(label = "Y축 (μT)", value = "%.1f".format(state.y))
                AdvancedDataRow(label = "Z축 (μT)", value = "%.1f".format(state.z))
                AdvancedDataRow(label = "강도", value = "${(state.intensity * 100).roundToInt()}%")
            }
        }
    }
}
