package com.seung.sensormaster.ui.screens.tools.sound

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundMeterScreen(
    onBack: () -> Unit,
    viewModel: SoundMeterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            @Suppress("MissingPermission")
            viewModel.startRecording()
        }
    }

    // dB 게이지 애니메이션
    val animatedDb by animateFloatAsState(
        targetValue = (state.db / 130f).coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "db_gauge"
    )

    val dbColor = when {
        state.db < 55 -> Color(0xFF4CAF50)
        state.db < 75 -> Color(0xFFFF9800)
        state.db < 85 -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("소음 측정기") },
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
            // ── dB 표시 ──
            Text(
                text = "${state.db.roundToInt()}",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "dB",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── 최소/평균/최대 ──
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "최소 ${if (state.minDb < Float.MAX_VALUE) "%.0f".format(state.minDb) else "—"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "평균 ${"%.0f".format(state.avgDb)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "최대 ${"%.0f".format(state.maxDb)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 시작/중지 버튼 (인라인) ──
            Button(
                onClick = {
                    if (state.isRecording) {
                        viewModel.stopRecording()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isRecording)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = if (state.isRecording) Icons.Outlined.MicOff else Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = if (state.isRecording)
                        MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isRecording) "측정 중지" else "측정 시작",
                    color = if (state.isRecording)
                        MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 컨텍스트 인사이트 ──
            Surface(
                color = if (state.db >= 85) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = state.context,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.db >= 85) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 수평 dB 바 ──
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                val cornerR = 12.dp.toPx()
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerR, cornerR)
                )
                drawRoundRect(
                    color = dbColor,
                    topLeft = Offset.Zero,
                    size = Size(size.width * animatedDb, size.height),
                    cornerRadius = CornerRadius(cornerR, cornerR)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 실시간 dB 파형 ──
            if (state.dbHistory.isNotEmpty()) {
                Text(
                    text = "실시간 파형",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val lineColor = MaterialTheme.colorScheme.primary
                val warnColor = Color(0xFFF44336)
                val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // 그리드 라인 (0, 40, 80, 120 dB)
                    for (i in 1..3) {
                        val y = h - h * (i * 40f / 130f)
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.5f)
                    }

                    // 85dB 위험 기준선
                    val dangerY = h - h * (85f / 130f)
                    drawLine(
                        warnColor.copy(alpha = 0.4f),
                        Offset(0f, dangerY),
                        Offset(w, dangerY),
                        strokeWidth = 1.dp.toPx()
                    )

                    val data = state.dbHistory
                    if (data.size >= 2) {
                        val path = Path()
                        val stepX = w / (data.size - 1).toFloat()

                        path.moveTo(0f, h - h * (data[0] / 130f).coerceIn(0f, 1f))
                        for (i in 1 until data.size) {
                            val x = i * stepX
                            val y = h - h * (data[i] / 130f).coerceIn(0f, 1f)
                            val prevX = (i - 1) * stepX
                            val prevY = h - h * (data[i - 1] / 130f).coerceIn(0f, 1f)
                            val cpx = (prevX + x) / 2
                            path.cubicTo(cpx, prevY, cpx, y, x, y)
                        }

                        // 글로우
                        drawPath(
                            path = path,
                            color = lineColor.copy(alpha = 0.15f),
                            style = Stroke(width = 6.dp.toPx())
                        )
                        // 메인 라인
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── 상세 정보 ──
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("소음 상세", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("현재 (dB)", "%.1f".format(state.db))
                    DetailRow("최대 (dB)", "%.1f".format(state.maxDb))
                    DetailRow(
                        "최소 (dB)",
                        if (state.minDb < Float.MAX_VALUE) "%.1f".format(state.minDb) else "—"
                    )
                    DetailRow("평균 (dB)", "%.1f".format(state.avgDb))
                    DetailRow("샘플레이트", "44.1 kHz")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
