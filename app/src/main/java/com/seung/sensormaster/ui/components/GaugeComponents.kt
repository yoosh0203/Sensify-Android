package com.seung.sensormaster.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 네온 링 게이지 — 원형 프로그래스 + 글로우 효과
 * 성능 최적화: 무한 글로우 펄스 애니메이션 제거 → 정적 glowAlpha
 * @param progress 0f..1f
 * @param glowColor 네온 색상
 */
@Composable
fun NeonRingGauge(
    progress: Float,
    glowColor: Color,
    modifier: Modifier = Modifier.size(220.dp),
    trackAlpha: Float = 0.12f,
    strokeWidth: Float = 14f
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "neon_ring"
    )

    // 정적 글로우 (무한 애니메이션 제거)
    val glowAlpha = 0.6f

    Canvas(modifier = modifier) {
        val padding = strokeWidth * 2
        val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
        val topLeft = Offset(padding, padding)
        val startAngle = 135f
        val sweepTotal = 270f

        // 트랙 (배경)
        drawArc(
            color = glowColor.copy(alpha = trackAlpha),
            startAngle = startAngle,
            sweepAngle = sweepTotal,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 글로우 레이어 (넓은 스트로크 + 낮은 알파)
        drawArc(
            color = glowColor.copy(alpha = glowAlpha * 0.3f),
            startAngle = startAngle,
            sweepAngle = sweepTotal * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth * 3f, cap = StrokeCap.Round)
        )

        // 메인 아크
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.3f),
                    glowColor,
                    glowColor
                )
            ),
            startAngle = startAngle,
            sweepAngle = sweepTotal * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 끝점 도트
        if (animatedProgress > 0.01f) {
            val endAngle = Math.toRadians((startAngle + sweepTotal * animatedProgress).toDouble())
            val cx = size.width / 2 + (arcSize.width / 2) * cos(endAngle).toFloat()
            val cy = size.height / 2 + (arcSize.height / 2) * sin(endAngle).toFloat()
            drawCircle(color = glowColor, radius = strokeWidth * 0.8f, center = Offset(cx, cy))
            drawCircle(color = glowColor.copy(alpha = 0.3f), radius = strokeWidth * 2f, center = Offset(cx, cy))
        }
    }
}

/**
 * 네온 바 게이지 — 수평 그라디언트 바 + 글로우
 * @param progress 0f..1f
 */
@Composable
fun NeonBarGauge(
    progress: Float,
    glowColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "neon_bar"
    )

    Canvas(modifier = modifier) {
        val cornerR = size.height / 2
        // 트랙
        drawRoundRect(
            color = glowColor.copy(alpha = 0.1f),
            cornerRadius = CornerRadius(cornerR),
            size = size
        )
        // 글로우
        drawRoundRect(
            color = glowColor.copy(alpha = 0.15f),
            cornerRadius = CornerRadius(cornerR),
            size = Size(size.width * animatedProgress, size.height * 1.6f),
            topLeft = Offset(0f, -size.height * 0.3f)
        )
        // 메인 바
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(glowColor.copy(alpha = 0.4f), glowColor)
            ),
            cornerRadius = CornerRadius(cornerR),
            size = Size(size.width * animatedProgress, size.height)
        )
    }
}
