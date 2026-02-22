package com.seung.sensormaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import com.seung.sensormaster.ui.theme.LocalExtendedColors

/**
 * Glassmorphism 카드 — 반투명 배경 + 네온 보더 글로우 + springy press 애니메이션
 * 성능 최적화: 무한 글로우 펄스 애니메이션 제거 → 정적 글로우
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glowColor: Color = LocalExtendedColors.current.neonCyan,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDark = isSystemInDarkTheme()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glass_scale"
    )

    // 정적 글로우 alpha (무한 애니메이션 제거)
    val glowAlpha = if (isDark) 0.25f else 0.10f

    val shape = RoundedCornerShape(24.dp)
    // 글래스모피즘 가시성 개선: 반투명도 조정
    val bgAlpha = if (isDark) 0.12f else 0.65f
    val bgAlpha2 = if (isDark) 0.04f else 0.40f
    val borderAlpha = if (isDark) 0.25f else 0.12f

    Column(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .drawBehind {
                // 외곽 글로우 (정적)
                drawRoundRect(
                    color = glowColor.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(28.dp.toPx()),
                    size = size.copy(
                        width = size.width + 4.dp.toPx(),
                        height = size.height + 4.dp.toPx()
                    ),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = bgAlpha),
                        Color.White.copy(alpha = bgAlpha2),
                    )
                )
            )
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = borderAlpha)
                        else Color.Black.copy(alpha = 0.08f),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(20.dp),
        content = content
    )
}

/**
 * 카테고리 그리드 카드 — 홈 화면용 (네온 그라디언트 아이콘 배경)
 */
@Composable
fun CategoryCard(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientStart: Color = LocalExtendedColors.current.neonCyan,
    gradientEnd: Color = LocalExtendedColors.current.neonPurple
) {
    val isDark = isSystemInDarkTheme()
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        glowColor = gradientStart
    ) {
        // 아이콘 배경 — 그라디언트 원형
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            gradientStart.copy(alpha = if (isDark) 0.25f else 0.15f),
                            gradientEnd.copy(alpha = if (isDark) 0.15f else 0.08f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = gradientStart,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 도구 리스트 아이템 — 카테고리 상세 화면용
 */
@Composable
fun ToolListItem(
    icon: ImageVector,
    name: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalExtendedColors.current.neonCyan
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        glowColor = accentColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
