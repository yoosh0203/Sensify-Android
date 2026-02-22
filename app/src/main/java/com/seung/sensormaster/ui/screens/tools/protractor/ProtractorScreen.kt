package com.seung.sensormaster.ui.screens.tools.protractor

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtractorScreen(
    onBack: () -> Unit,
    viewModel: ProtractorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    // 주요 각도(0, 90, 180, 270) 진동 피드백
    var lastSnappingAngle by remember { mutableStateOf(-1) }
    LaunchedEffect(state.angle) {
        val rounded = state.angle.toInt()
        if (rounded % 90 == 0 || (rounded % 90 == 89) || (rounded % 90 == 1)) {
            val target = ((rounded + 1) / 90) * 90 % 360
            if (lastSnappingAngle != target) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                lastSnappingAngle = target
            }
        } else {
            lastSnappingAngle = -1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("각도기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 1. 카메라 프리뷰 (배경)
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                    val cameraFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraFuture.addListener({
                        val cameraProvider = cameraFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. 가이드 라인 (십자선)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val color = Color.White.copy(alpha = 0.5f)
                drawLine(color, Offset(0f, cy), Offset(size.width, cy), strokeWidth = 1.dp.toPx())
                drawLine(color, Offset(cx, 0f), Offset(cx, size.height), strokeWidth = 1.dp.toPx())
            }

            // 3. 각도기 오버레이
            ProtractorOverlay(
                angle = state.angle,
                relativeAngle = state.relativeAngle,
                isRelative = state.offset != 0f,
                modifier = Modifier.fillMaxSize()
            )

            // 4. 상단 정보 패널
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "%.1f°".format(state.relativeAngle),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (state.offset != 0f) {
                        Text(
                            text = "기준점 대비 (절대: %.1f°)".format(state.angle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "실시간 기울기",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 5. 컨트롤 버튼
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.setZero() },
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.RestartAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("영점 설정")
                }

                if (state.offset != 0f) {
                    OutlinedButton(
                        onClick = { viewModel.resetZero() },
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(Icons.Outlined.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("초기화")
                    }
                }
            }
        }
    }
}

@Composable
fun ProtractorOverlay(
    angle: Float,
    relativeAngle: Float,
    isRelative: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val textColor = Color.White

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width.coerceAtMost(size.height) * 0.4f

        // 바깥 테두리
        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )

        // 눈금 그리기 (10도 단위 숫표기, 2도 단위 눈금)
        for (i in 0 until 360 step 2) {
            val angleRad = Math.toRadians(i.toDouble() - 90.0)
            val isMajor = i % 10 == 0
            val tickLen = if (isMajor) 15.dp.toPx() else 8.dp.toPx()
            val color = if (i % 90 == 0) primaryColor else Color.White.copy(alpha = 0.6f)

            val startX = cx + (radius - tickLen) * cos(angleRad).toFloat()
            val startY = cy + (radius - tickLen) * sin(angleRad).toFloat()
            val endX = cx + radius * cos(angleRad).toFloat()
            val endY = cy + radius * sin(angleRad).toFloat()

            drawLine(color, Offset(startX, startY), Offset(endX, endY), strokeWidth = 1.dp.toPx())

            // 숫자 표기
            if (i % 30 == 0) {
                val textRadius = radius - 30.dp.toPx()
                val tx = cx + textRadius * cos(angleRad).toFloat()
                val ty = cy + textRadius * sin(angleRad).toFloat()
                
                drawContext.canvas.nativeCanvas.drawText(
                    i.toString(),
                    tx,
                    ty + 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        this.textSize = 12.sp.toPx()
                        this.textAlign = android.graphics.Paint.Align.CENTER
                        this.isFakeBoldText = true
                    }
                )
            }
        }

        // 현재 각도 지시선
        val pointerRad = Math.toRadians(angle.toDouble() - 90.0)
        val px = cx + radius * cos(pointerRad).toFloat()
        val py = cy + radius * sin(pointerRad).toFloat()

        drawLine(
            primaryColor,
            Offset(cx, cy),
            Offset(px, py),
            strokeWidth = 3.dp.toPx()
        )
        drawCircle(primaryColor, radius = 6.dp.toPx(), center = Offset(px, py))
        
        // 중앙 코어
        drawCircle(primaryColor, radius = 4.dp.toPx(), center = Offset(cx, cy))
    }
}
