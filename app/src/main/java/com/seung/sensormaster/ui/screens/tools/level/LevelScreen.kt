package com.seung.sensormaster.ui.screens.tools.level

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.StrokeCap
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
fun LevelScreen(
    onBack: () -> Unit,
    viewModel: LevelViewModel = hiltViewModel(),
    settingsVM: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAdvancedMode by settingsVM.advancedMode.collectAsStateWithLifecycle()

    // Springy bubble animation
    val animBubbleX by animateFloatAsState(
        targetValue = state.bubbleX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bubble_x"
    )
    val animBubbleY by animateFloatAsState(
        targetValue = state.bubbleY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bubble_y"
    )

    // 수평이면 초록, 아니면 테마 primary
    val bubbleColor by animateColorAsState(
        targetValue = if (state.isLevel) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
        label = "bubble_color"
    )
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val ringColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("수평계") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // ── 각도 표시 ──
            Text(
                text = if (state.isLevel) "수평" else "%.1f°".format(
                    maxOf(abs(state.pitch), abs(state.roll))
                ),
                style = MaterialTheme.typography.displayLarge,
                color = if (state.isLevel) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (state.isLevel) "완벽한 수평입니다" else "기울어져 있습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── 2D 버블 뷰 ──
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
                    val maxRadius = size.minDimension / 2 * 0.9f

                    // 동심원 (가이드)
                    for (i in 1..4) {
                        drawCircle(
                            color = ringColor,
                            radius = maxRadius * (i / 4f),
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = if (i == 4) 2.dp.toPx() else 1.dp.toPx()
                            )
                        )
                    }

                    // 십자선
                    drawLine(gridColor, Offset(cx - maxRadius, cy), Offset(cx + maxRadius, cy), 1.dp.toPx())
                    drawLine(gridColor, Offset(cx, cy - maxRadius), Offset(cx, cy + maxRadius), 1.dp.toPx())

                    // 버블
                    val bubbleRadius = 24.dp.toPx()
                    val bx = cx + animBubbleX * (maxRadius - bubbleRadius)
                    val by = cy + animBubbleY * (maxRadius - bubbleRadius)

                    // 버블 그림자
                    drawCircle(
                        color = bubbleColor.copy(alpha = 0.2f),
                        radius = bubbleRadius + 4.dp.toPx(),
                        center = Offset(bx, by)
                    )
                    // 버블 본체
                    drawCircle(
                        color = bubbleColor,
                        radius = bubbleRadius,
                        center = Offset(bx, by)
                    )
                    // 버블 하이라이트
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = bubbleRadius * 0.4f,
                        center = Offset(bx - 6.dp.toPx(), by - 6.dp.toPx())
                    )
                }
            }

            // ── 고급 모드 ──
            AdvancedPanel(isVisible = isAdvancedMode) {
                Text("자세 데이터", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                AdvancedDataRow(label = "피치 (전후)", value = "%.2f°".format(state.pitch))
                AdvancedDataRow(label = "롤 (좌우)", value = "%.2f°".format(state.roll))
                AdvancedDataRow(label = "버블 X", value = "%.3f".format(state.bubbleX))
                AdvancedDataRow(label = "버블 Y", value = "%.3f".format(state.bubbleY))
                AdvancedDataRow(label = "수평 판정", value = if (state.isLevel) "✓ ±1° 이내" else "✗ 기울어짐")
            }
        }
    }
}
