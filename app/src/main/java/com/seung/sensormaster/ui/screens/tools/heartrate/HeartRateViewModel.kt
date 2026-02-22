package com.seung.sensormaster.ui.screens.tools.heartrate

import android.Manifest
import android.content.Context
import android.graphics.ImageFormat
import android.util.Size
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * 심박수 측정 상태
 */
data class HeartRateState(
    val bpm: Int = 0,
    val confidence: Float = 0f,       // 0..1 — 손가락 덮음/신호 품질
    val isFingerDetected: Boolean = false,
    val isMeasuring: Boolean = false,
    val progress: Float = 0f,         // 0..1 — 측정 진행률
    val redIntensity: Float = 0f,     // 현재 빨간 채널 평균
    val history: List<Int> = emptyList(),     // 최근 측정 BPM들
    val status: String = "손가락을 후면 카메라에 올려주세요"
)

@HiltViewModel
class HeartRateViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(HeartRateState())
    val state: StateFlow<HeartRateState> = _state.asStateFlow()

    // PPG 분석 파라미터
    private val sampleWindow = 10_000L        // 10초 측정 윈도우
    private val minRedThreshold = 50f         // 빨간 채널 최소 강도 (손가락 감지 기준)
    private val redSamples = mutableListOf<Pair<Long, Float>>()  // (timestamp, redAvg)
    private var measureStartTime = 0L
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null

    /**
     * CameraX를 사용하여 측정 시작
     * @param lifecycleOwner Activity/Fragment의 LifecycleOwner
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    fun startMeasurement(lifecycleOwner: LifecycleOwner) {
        if (_state.value.isMeasuring) return

        redSamples.clear()
        measureStartTime = System.currentTimeMillis()
        _state.value = HeartRateState(
            isMeasuring = true,
            status = "손가락을 후면 카메라에 올려주세요"
        )

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(320, 240))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()

                imageAnalysis?.setAnalyzer(
                    ContextCompat.getMainExecutor(context)
                ) { imageProxy ->
                    analyzeFrame(imageProxy)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )

                // 플래시 켜기 — 손가락 투과광 밝히기
                try {
                    val camera = cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        imageAnalysis
                    )
                    camera?.cameraControl?.enableTorch(true)
                } catch (_: Exception) { }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isMeasuring = false,
                    status = "카메라 초기화 실패: ${e.message}"
                )
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * 측정 중지
     */
    fun stopMeasurement() {
        try {
            cameraProvider?.unbindAll()
        } catch (_: Exception) { }
        cameraProvider = null
        imageAnalysis = null
        redSamples.clear()
        _state.value = _state.value.copy(
            isMeasuring = false,
            status = if (_state.value.bpm > 0) "측정 완료: ${_state.value.bpm} BPM" else "측정 중지됨"
        )
    }

    /**
     * 각 프레임에서 빨간 채널 평균을 추출하고 PPG 분석
     */
    private fun analyzeFrame(imageProxy: ImageProxy) {
        if (!_state.value.isMeasuring) {
            imageProxy.close()
            return
        }

        try {
            val redAvg = extractRedAverage(imageProxy)
            val now = System.currentTimeMillis()
            val elapsed = now - measureStartTime
            // 주기 내 진행률 (10초 주기 반복)
            val cycleElapsed = elapsed % sampleWindow
            val progress = (cycleElapsed.toFloat() / sampleWindow).coerceIn(0f, 1f)

            val isFingerDetected = redAvg > minRedThreshold

            if (isFingerDetected) {
                redSamples.add(Pair(now, redAvg))
                // 오래된 데이터 정리 (최근 12초만 유지)
                val cutoffCleanup = now - 12_000L
                redSamples.removeAll { it.first < cutoffCleanup }
            }

            // 상태 업데이트
            val status = when {
                !isFingerDetected -> "손가락을 카메라에 꽉 대세요"
                elapsed < 3000 -> "신호 안정화 중..."
                else -> "측정 중..."
            }

            viewModelScope.launch(Dispatchers.Default) {
                var bpm = _state.value.bpm
                var confidence = 0f

                // 3초 이후부터 BPM 계산 시작
                if (isFingerDetected && redSamples.size > 30) {
                    val result = calculateBPM()
                    bpm = result.first
                    confidence = result.second
                }

                val currentHistory = _state.value.history
                // 10초 주기 완료 시 히스토리에 기록
                val newHistory = if (elapsed > sampleWindow && cycleElapsed < 500 && bpm > 0 &&
                    (currentHistory.isEmpty() || currentHistory.last() != bpm)) {
                    (currentHistory + bpm).takeLast(20)
                } else {
                    currentHistory
                }

                _state.value = _state.value.copy(
                    redIntensity = redAvg,
                    isFingerDetected = isFingerDetected,
                    progress = progress,
                    bpm = bpm,
                    confidence = confidence,
                    status = if (bpm > 0) "$bpm BPM" else status,
                    history = newHistory
                )
            }
        } catch (_: Exception) {
        } finally {
            imageProxy.close()
        }
    }

    /**
     * YUV 이미지에서 빨간 채널 평균 추출
     * Y 프레인의 밝기를 사용. 손가락+플래시 조합 시 빨간 투과광이 밝기 변화를 만듬
     */
    private fun extractRedAverage(imageProxy: ImageProxy): Float {
        val yPlane = imageProxy.planes[0]
        val buffer = yPlane.buffer
        val rowStride = yPlane.rowStride
        val width = imageProxy.width
        val height = imageProxy.height

        // 중앙 영역만 샘플링 (성능)
        val regionSize = minOf(width, height) / 4
        val startX = (width - regionSize) / 2
        val startY = (height - regionSize) / 2

        var sum = 0L
        var count = 0
        for (y in startY until startY + regionSize step 2) {
            for (x in startX until startX + regionSize step 2) {
                val idx = y * rowStride + x
                if (idx < buffer.capacity()) {
                    sum += (buffer.get(idx).toInt() and 0xFF)
                    count++
                }
            }
        }

        return if (count > 0) sum.toFloat() / count else 0f
    }

    /**
     * PPG 신호에서 BPM 계산
     * 피크 간격(IBI)을 추출하여 심박수 계산
     */
    private fun calculateBPM(): Pair<Int, Float> {
        if (redSamples.size < 30) return Pair(0, 0f)

        // 최근 데이터만 사용 (6초)
        val cutoff = System.currentTimeMillis() - 6000
        val recent = redSamples.filter { it.first > cutoff }
        if (recent.size < 20) return Pair(0, 0f)

        // 간단한 이동평균 제거 (DC offset 제거)
        val values = recent.map { it.second }
        val mean = values.average().toFloat()
        val detrended = values.map { it - mean }

        // 피크 검출 (zero-crossing + local max)
        val peaks = mutableListOf<Int>()
        for (i in 2 until detrended.size - 2) {
            if (detrended[i] > 0 &&
                detrended[i] > detrended[i - 1] &&
                detrended[i] > detrended[i + 1] &&
                detrended[i] > detrended[i - 2] &&
                detrended[i] > detrended[i + 2]
            ) {
                // 이전 피크와 최소 간격 확인 (0.3초 = ~200BPM 상한)
                if (peaks.isEmpty() || (recent[i].first - recent[peaks.last()].first) > 300) {
                    peaks.add(i)
                }
            }
        }

        if (peaks.size < 2) return Pair(0, 0f)

        // 피크 간 간격(IBI) 계산
        val ibis = mutableListOf<Long>()
        for (i in 1 until peaks.size) {
            val ibi = recent[peaks[i]].first - recent[peaks[i - 1]].first
            if (ibi in 300..1500) { // 40~200 BPM 범위
                ibis.add(ibi)
            }
        }

        if (ibis.isEmpty()) return Pair(0, 0f)

        val avgIBI = ibis.average()
        val bpm = (60_000.0 / avgIBI).roundToInt()

        // 신뢰도: IBI 표준편차 기반
        val stdDev = if (ibis.size > 1) {
            kotlin.math.sqrt(ibis.map { (it - avgIBI) * (it - avgIBI) }.average())
        } else 0.0
        val confidence = (1.0 - (stdDev / avgIBI).coerceIn(0.0, 1.0)).toFloat()

        // 비현실적 BPM 필터
        return if (bpm in 40..200) Pair(bpm, confidence) else Pair(0, 0f)
    }

    override fun onCleared() {
        super.onCleared()
        try { cameraProvider?.unbindAll() } catch (_: Exception) { }
        cameraProvider = null
        imageAnalysis = null
        redSamples.clear()
    }
}
