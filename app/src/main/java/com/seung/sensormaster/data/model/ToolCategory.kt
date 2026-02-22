package com.seung.sensormaster.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 6대 카테고리 정의
 */
enum class ToolCategory(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val description: String
) {
    NAVIGATION(
        label = "네비게이션",
        icon = Icons.Outlined.Explore,
        route = "category/navigation",
        description = "나침반 · GPS · 속도 · 고도"
    ),
    ENVIRONMENT(
        label = "환경 & 소음",
        icon = Icons.Outlined.Forest,
        route = "category/environment",
        description = "소음 · 금속 · 조도 · 색상"
    ),
    PHYSICS(
        label = "전문가 & 물리",
        icon = Icons.Outlined.Speed,
        route = "category/physics",
        description = "G-Force · 수직속도 · RPM"
    ),
    WIRELESS(
        label = "무선 & 연결",
        icon = Icons.Outlined.CellTower,
        route = "category/wireless",
        description = "WiFi · BT · NFC"
    ),
    WELLNESS(
        label = "웰니스",
        icon = Icons.Outlined.FavoriteBorder,
        route = "category/wellness",
        description = "심박수 · 만보기"
    ),
    SYSTEM(
        label = "시스템",
        icon = Icons.Outlined.PhoneAndroid,
        route = "category/system",
        description = "배터리 · 하드웨어 정보"
    );
}
