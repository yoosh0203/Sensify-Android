package com.seung.sensormaster.ui.screens.tools.compass

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.components.AdvancedDataRow
import com.seung.sensormaster.ui.components.AdvancedPanel
import com.seung.sensormaster.ui.screens.settings.SettingsViewModel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    onBack: () -> Unit,
    viewModel: CompassViewModel = hiltViewModel(),
    settingsVM: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAdvancedMode by settingsVM.advancedMode.collectAsStateWithLifecycle()

    // Springy rotation animation
    val animatedAzimuth by animateFloatAsState(
        targetValue = -state.azimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "compass_rotation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    val northColor = Color(0xFFE53935)          // 빨간 바늘
    val southColor = Color(0xFF78909C)           // 회색 바늘

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("나침반") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            Spacer(modifier = Modifier.height(16.dp))

            // ── 방위각 표시 ──
            Text(
                text = "${state.azimuth.roundToInt()}°",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = state.directionKr,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── 나침반 다이얼 ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val radius = size.minDimension / 2 * 0.85f

                    // 회전 적용 (나침반 다이얼이 회전)
                    rotate(animatedAzimuth, pivot = Offset(cx, cy)) {
                        // 외곽 원
                        drawCircle(
                            color = surfaceVariantColor.copy(alpha = 0.2f),
                            radius = radius,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )

                        // 눈금 (360도)
                        for (i in 0 until 360 step 10) {
                            val angle = Math.toRadians(i.toDouble())
                            val isMajor = i % 90 == 0
                            val isMid = i % 30 == 0
                            val tickLength = when {
                                isMajor -> 24.dp.toPx()
                                isMid -> 16.dp.toPx()
                                else -> 8.dp.toPx()
                            }
                            val startR = radius - tickLength
                            val endR = radius

                            drawLine(
                                color = if (isMajor) onSurfaceColor else surfaceVariantColor.copy(alpha = 0.5f),
                                start = Offset(
                                    cx + (startR * sin(angle)).toFloat(),
                                    cy - (startR * cos(angle)).toFloat()
                                ),
                                end = Offset(
                                    cx + (endR * sin(angle)).toFloat(),
                                    cy - (endR * cos(angle)).toFloat()
                                ),
                                strokeWidth = if (isMajor) 3.dp.toPx() else 1.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // 북쪽 표시 (다이얼에 고정된 작은 "N" 마커)
                        val nMarkerY = cy - radius + 8.dp.toPx()
                        val nPath = Path().apply {
                            moveTo(cx, nMarkerY)
                            lineTo(cx - 6.dp.toPx(), nMarkerY + 12.dp.toPx())
                            lineTo(cx + 6.dp.toPx(), nMarkerY + 12.dp.toPx())
                            close()
                        }
                        drawPath(nPath, errorColor)
                    }

                    // ── 고정된 나침반 바늘 (화면 위쪽 = 현재 방향) ──
                    val needleLength = radius * 0.65f
                    val needleWidth = 12.dp.toPx()

                    // 북쪽 바늘 (빨간색) — 화면 위쪽을 가리킴
                    val northPath = Path().apply {
                        moveTo(cx, cy - needleLength)                   // 꼭짓점 (위쪽)
                        lineTo(cx - needleWidth / 2, cy)                // 왼쪽 아래
                        lineTo(cx + needleWidth / 2, cy)                // 오른쪽 아래
                        close()
                    }
                    drawPath(northPath, northColor)

                    // 남쪽 바늘 (회색) — 화면 아래쪽을 가리킴
                    val southPath = Path().apply {
                        moveTo(cx, cy + needleLength)                   // 꼭짓점 (아래쪽)
                        lineTo(cx - needleWidth / 2, cy)                // 왼쪽 위
                        lineTo(cx + needleWidth / 2, cy)                // 오른쪽 위
                        close()
                    }
                    drawPath(southPath, southColor)

                    // 중앙 원 (바늘 피벗)
                    drawCircle(
                        color = onSurfaceColor,
                        radius = 5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 3.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            // ── 고급 모드 패널 ──
            AdvancedPanel(isVisible = isAdvancedMode) {
                Text(
                    text = "센서 상세",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AdvancedDataRow(label = "방위각 (Azimuth)", value = "%.1f°".format(state.azimuth))
                AdvancedDataRow(label = "피치 (Pitch)", value = "%.1f°".format(state.pitch))
                AdvancedDataRow(label = "롤 (Roll)", value = "%.1f°".format(state.roll))
                AdvancedDataRow(label = "방향", value = "${state.direction} / ${state.directionKr}")
                AdvancedDataRow(
                    label = "센서 정확도",
                    value = when (state.accuracy) {
                        3 -> "높음"
                        2 -> "보통"
                        1 -> "낮음"
                        else -> "보정 필요"
                    }
                )
            }
        }
    }
}
