package com.seung.sensormaster.data.model

import android.hardware.Sensor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SatelliteAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 개별 도구(기능) 정의
 */
data class SensorTool(
    val id: String,
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: ToolCategory,
    val route: String,
    val requiresPermission: List<String> = emptyList(),
    val requiredSensorType: Int? = null  // android.hardware.Sensor.TYPE_*
)

/**
 * 전체 도구 목록 — 20개
 * 아이콘은 모두 androidx.compose.material:material-icons-extended에서 검증된 이름만 사용
 */
object SensorTools {
    val all: List<SensorTool> = listOf(
        // ── 네비게이션 ──
        SensorTool("compass", "나침반", "방위각 측정", Icons.Outlined.Navigation, ToolCategory.NAVIGATION, "tool/compass", requiredSensorType = Sensor.TYPE_MAGNETIC_FIELD),
        SensorTool("gps_radar", "위성 레이더", "GPS Skyview", Icons.Outlined.SatelliteAlt, ToolCategory.NAVIGATION, "tool/gps_radar", listOf("ACCESS_FINE_LOCATION")),
        SensorTool("speedometer", "속도계", "가속도 기반", Icons.Outlined.Speed, ToolCategory.NAVIGATION, "tool/speedometer", requiredSensorType = Sensor.TYPE_LINEAR_ACCELERATION),
        SensorTool("altimeter", "고도/기압계", "기압 센서", Icons.Outlined.Terrain, ToolCategory.NAVIGATION, "tool/altimeter", requiredSensorType = Sensor.TYPE_PRESSURE),

        // ── 환경 & 소음 ──
        SensorTool("sound_meter", "소음 측정기", "데시벨 & FFT", Icons.AutoMirrored.Outlined.VolumeUp, ToolCategory.ENVIRONMENT, "tool/sound_meter", listOf("RECORD_AUDIO")),
        SensorTool("metal_detector", "금속 탐지기", "자기장 + 비프음", Icons.Outlined.Search, ToolCategory.ENVIRONMENT, "tool/metal_detector", requiredSensorType = Sensor.TYPE_MAGNETIC_FIELD),
        SensorTool("light_meter", "조도 센서", "Lux 측정", Icons.Outlined.LightMode, ToolCategory.ENVIRONMENT, "tool/light_meter", requiredSensorType = Sensor.TYPE_LIGHT),
        SensorTool("color_detector", "색상 탐지기", "카메라 기반", Icons.Outlined.Palette, ToolCategory.ENVIRONMENT, "tool/color_detector", listOf("CAMERA")),

        // ── 전문가 & 물리 ──
        SensorTool("vertical_speed", "수직 속도계", "엘리베이터 속도", Icons.Outlined.Height, ToolCategory.PHYSICS, "tool/vertical_speed", requiredSensorType = Sensor.TYPE_PRESSURE),
        SensorTool("doppler", "도플러 속도계", "음향 분석", Icons.Outlined.Equalizer, ToolCategory.PHYSICS, "tool/doppler", listOf("RECORD_AUDIO")),
        SensorTool("rpm_meter", "RPM 측정기", "회전수 분석", Icons.Outlined.Refresh, ToolCategory.PHYSICS, "tool/rpm_meter", listOf("RECORD_AUDIO")),
        SensorTool("g_force", "G-Force", "중력가속도", Icons.Outlined.FitnessCenter, ToolCategory.PHYSICS, "tool/g_force", requiredSensorType = Sensor.TYPE_ACCELEROMETER),
        SensorTool("spirit_level", "수평계", "기울기 측정", Icons.Outlined.Straighten, ToolCategory.PHYSICS, "tool/spirit_level", requiredSensorType = Sensor.TYPE_ACCELEROMETER),
        SensorTool("protractor", "각도기", "카메라 기반 측정", Icons.Outlined.Straighten, ToolCategory.PHYSICS, "tool/protractor", requiredSensorType = Sensor.TYPE_ACCELEROMETER),
        SensorTool("vibrometer", "진동 측정기", "지진계 스타일", Icons.Outlined.Vibration, ToolCategory.PHYSICS, "tool/vibrometer", requiredSensorType = Sensor.TYPE_ACCELEROMETER),
        SensorTool("tone_generator", "주파수 발생기", "Hz 제어", Icons.Outlined.MusicNote, ToolCategory.PHYSICS, "tool/tone_generator"),

        // ── 무선 & 연결 ──
        SensorTool("wifi_analyzer", "WiFi 분석기", "채널 & 신호", Icons.Outlined.Wifi, ToolCategory.WIRELESS, "tool/wifi_analyzer", listOf("ACCESS_FINE_LOCATION", "NEARBY_WIFI_DEVICES")),
        SensorTool("bt_scanner", "BT 스캐너", "BLE 탐색", Icons.Outlined.Bluetooth, ToolCategory.WIRELESS, "tool/bt_scanner", listOf("BLUETOOTH_SCAN", "BLUETOOTH_CONNECT")),
        SensorTool("nfc_reader", "NFC 리더", "태그 읽기/쓰기", Icons.Outlined.MyLocation, ToolCategory.WIRELESS, "tool/nfc_reader", listOf("NFC")),

        // ── 웰니스 ──
        SensorTool("heart_rate", "심박수", "PPG 측정", Icons.Outlined.MonitorHeart, ToolCategory.WELLNESS, "tool/heart_rate", listOf("CAMERA")),
        SensorTool("pedometer", "만보기", "걸음 수 측정", Icons.AutoMirrored.Outlined.DirectionsWalk, ToolCategory.WELLNESS, "tool/pedometer", listOf("ACTIVITY_RECOGNITION"), Sensor.TYPE_STEP_COUNTER),

        // ── 시스템 ──
        SensorTool("battery", "배터리 & 파워", "전류/전압/건강도", Icons.Outlined.BatteryChargingFull, ToolCategory.SYSTEM, "tool/battery"),
        SensorTool("device_info", "기기 정보", "CPU/센서 스펙", Icons.Outlined.Memory, ToolCategory.SYSTEM, "tool/device_info"),
    )

    fun byCategory(category: ToolCategory): List<SensorTool> =
        all.filter { it.category == category }
}
