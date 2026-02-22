package com.seung.sensormaster.ui.screens.tools.color

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorDetectorScreen(
    onBack: () -> Unit,
    viewModel: ColorDetectorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val density = LocalDensity.current

    // 최근 캡처를 위한 프레임 저장
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }
    // 피커 위치
    var pickerOffset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("색상 탐지기") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로") } },
                actions = {
                    if (state.isCaptured) {
                        // 다시 실시간으로 돌아가기
                        IconButton(onClick = { viewModel.releaseCaptured() }) {
                            Icon(Icons.Outlined.Refresh, "다시 촬영")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (state.isCaptured && state.capturedBitmap != null) {
                    // 캡처된 이미지 + 드래그 피커
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { imageSize = it }
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    pickerOffset = offset
                                    viewModel.pickColorAt(offset.x, offset.y, imageSize.width, imageSize.height)
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val newOffset = change.position
                                    pickerOffset = Offset(
                                        newOffset.x.coerceIn(0f, imageSize.width.toFloat()),
                                        newOffset.y.coerceIn(0f, imageSize.height.toFloat())
                                    )
                                    viewModel.pickColorAt(pickerOffset.x, pickerOffset.y, imageSize.width, imageSize.height)
                                }
                            }
                    ) {
                        Image(
                            bitmap = state.capturedBitmap!!.asImageBitmap(),
                            contentDescription = "캡처 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // 피커 서클
                        if (pickerOffset != Offset.Zero) {
                            val pickerX = with(density) { (pickerOffset.x - 20.dp.toPx()).toDp() }
                            val pickerY = with(density) { (pickerOffset.y - 20.dp.toPx()).toDp() }
                            Box(
                                modifier = Modifier
                                    .offset(x = pickerX, y = pickerY)
                                    .size(40.dp)
                                    .border(3.dp, Color.White, CircleShape)
                                    .border(1.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(state.r, state.g, state.b))
                                )
                            }
                        }
                    }
                } else {
                    // 실시간 카메라 프리뷰
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
                                val provider = cameraFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                                val analysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                    .build()
                                    .also {
                                        it.setAnalyzer(executor) { imageProxy ->
                                            analyzeAndCapture(imageProxy, viewModel) { bmp ->
                                                latestBitmap = bmp
                                            }
                                        }
                                    }
                                try {
                                    provider.unbindAll()
                                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // 중앙 조준점
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    )
                }
            }

            // 캡처 버튼
            if (!state.isCaptured) {
                FilledTonalButton(
                    onClick = {
                        latestBitmap?.let { viewModel.captureBitmap(it.copy(Bitmap.Config.ARGB_8888, false)) }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(Icons.Outlined.CameraAlt, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("캡처")
                }
            } else {
                Text(
                    "터치하거나 드래그하여 색상을 선택하세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // 결과 패널
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(state.r, state.g, state.b))
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(state.colorName, style = MaterialTheme.typography.titleLarge)
                        Text(state.colorHex, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("R:${state.r}  G:${state.g}  B:${state.b}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun analyzeAndCapture(imageProxy: ImageProxy, viewModel: ColorDetectorViewModel, onFrame: (Bitmap) -> Unit) {
    try {
        val w = imageProxy.width
        val h = imageProxy.height
        val plane = imageProxy.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rotation = imageProxy.imageInfo.rotationDegrees

        // 중앙 픽셀 위치 계산
        val cx = w / 2
        val cy = h / 2
        val offset = (cy * rowStride) + (cx * pixelStride)

        if (offset + 3 < buffer.remaining()) {
            val r = buffer.get(offset).toInt() and 0xFF
            val g = buffer.get(offset + 1).toInt() and 0xFF
            val b = buffer.get(offset + 2).toInt() and 0xFF

            viewModel.onColorDetected(r, g, b)
        }

        if (System.currentTimeMillis() % 100 < 20) {
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            buffer.rewind()
            bitmap.copyPixelsFromBuffer(buffer)

            // 회전 보정
            val rotatedBitmap = if (rotation != 0) {
                val matrix = android.graphics.Matrix()
                matrix.postRotate(rotation.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true).also {
                    if (it != bitmap) bitmap.recycle()
                }
            } else {
                bitmap
            }
            onFrame(rotatedBitmap)
        }

    } catch (_: Exception) {} finally {
        imageProxy.close()
    }
}
