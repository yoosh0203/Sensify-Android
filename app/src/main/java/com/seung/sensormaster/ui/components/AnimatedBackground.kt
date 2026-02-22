package com.seung.sensormaster.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.seung.sensormaster.ui.theme.LocalExtendedColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * 메시 그라디언트 배경 — 시간에 따라 색상이 부드럽게 변화
 */
@Composable
fun MeshGradientBackground(
    modifier: Modifier = Modifier
) {
    val ext = LocalExtendedColors.current
    val isDark = isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "mesh")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2832f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val alpha = if (isDark) 0.08f else 0.06f
    val neonCyan = ext.neonCyan
    val neonPurple = ext.neonPurple
    val neonAmber = ext.neonAmber

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val radius = maxOf(w, h) * 0.6f

        drawRect(color = surfaceColor, size = size)

        // 1번: Cyan
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(neonCyan.copy(alpha = alpha), Color.Transparent),
                center = Offset(cx + cos(phase) * radius * 0.3f, cy + sin(phase) * radius * 0.2f),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx + cos(phase) * radius * 0.3f, cy + sin(phase) * radius * 0.2f)
        )
        // 2번: Purple
        val p2 = phase + 2.094f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(neonPurple.copy(alpha = alpha), Color.Transparent),
                center = Offset(cx + cos(p2) * radius * 0.25f, cy + sin(p2) * radius * 0.35f),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx + cos(p2) * radius * 0.25f, cy + sin(p2) * radius * 0.35f)
        )
        // 3번: Amber
        val p3 = phase + 4.189f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(neonAmber.copy(alpha = alpha), Color.Transparent),
                center = Offset(cx + cos(p3) * radius * 0.2f, cy + sin(p3) * radius * 0.3f),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx + cos(p3) * radius * 0.2f, cy + sin(p3) * radius * 0.3f)
        )
    }
}

/**
 * 원형 펄스 배경 — 도구 화면 배경에 사용
 */
@Composable
fun PulseRingBackground(
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale1 by transition.animateFloat(
        initialValue = 0.4f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "s1"
    )
    val scale2 by transition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(3000, 1000, LinearEasing), RepeatMode.Restart),
        label = "s2"
    )
    val alpha1 by transition.animateFloat(
        initialValue = 0.15f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "a1"
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.12f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3000, 1000, LinearEasing), RepeatMode.Restart),
        label = "a2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height * 0.35f)
        val maxR = size.width * 0.5f
        drawCircle(color = color.copy(alpha = alpha1), radius = maxR * scale1, center = center)
        drawCircle(color = color.copy(alpha = alpha2), radius = maxR * scale2, center = center)
    }
}
