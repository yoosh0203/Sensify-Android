package com.seung.sensormaster.ui.screens.tools.gps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.theme.LocalExtendedColors
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsScreen(
    onBack: () -> Unit,
    viewModel: GpsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ext = LocalExtendedColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS 레이더") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로") } },
                actions = {
                    if (state.isListening) {
                        IconButton(onClick = { viewModel.stopListening() }) { Icon(Icons.Outlined.Stop, "정지") }
                    } else {
                        IconButton(onClick = { viewModel.startListening() }) { Icon(Icons.Outlined.MyLocation, "시작") }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.error != null) {
                Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
            }

            // 상태 요약
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip("위성 수", "${state.totalCount}")
                StatChip("사용 중", "${state.usedCount}")
            }

            Spacer(Modifier.height(16.dp))

            // 스카이뷰 레이더
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val maxR = (size.width / 2) * 0.9f
                    val surfVariant = Color.Gray.copy(alpha = 0.2f)

                    // 동심원 (0°, 30°, 60°, 90° 고도)
                    for (i in 1..3) {
                        drawCircle(color = surfVariant, radius = maxR * i / 3f, center = Offset(cx, cy), style = Stroke(1f))
                    }
                    drawCircle(color = surfVariant, radius = maxR, center = Offset(cx, cy), style = Stroke(1.5f))

                    // 십자선
                    drawLine(surfVariant, Offset(cx, cy - maxR), Offset(cx, cy + maxR), strokeWidth = 0.5f)
                    drawLine(surfVariant, Offset(cx - maxR, cy), Offset(cx + maxR, cy), strokeWidth = 0.5f)

                    // 위성 도트
                    state.satellites.forEach { sat ->
                        val r = maxR * (90f - sat.elevation) / 90f
                        val azRad = Math.toRadians(sat.azimuth.toDouble() - 90)
                        val sx = cx + r * cos(azRad).toFloat()
                        val sy = cy + r * sin(azRad).toFloat()
                        val color = if (sat.usedInFix) Color(0xFF69F0AE) else Color(0xFFFFB74D)
                        val dotR = (sat.cn0 / 60f).coerceIn(0.15f, 1f) * 8f

                        drawCircle(color = color.copy(alpha = 0.3f), radius = dotR * 2.5f, center = Offset(sx, sy))
                        drawCircle(color = color, radius = dotR, center = Offset(sx, sy))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 범례
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(Color(0xFF69F0AE), "사용 중")
                LegendDot(Color(0xFFFFB74D), "수신만")
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color, radius = size.width / 2)
        }
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
