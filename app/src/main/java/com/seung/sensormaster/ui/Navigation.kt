package com.seung.sensormaster.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seung.sensormaster.data.model.SensorTools
import com.seung.sensormaster.data.model.ToolCategory
import com.seung.sensormaster.ui.screens.category.CategoryScreen
import com.seung.sensormaster.ui.screens.home.HomeScreen
import com.seung.sensormaster.ui.screens.placeholder.ToolPlaceholderScreen
import com.seung.sensormaster.ui.screens.settings.SettingsScreen
import com.seung.sensormaster.ui.screens.tools.altimeter.AltimeterScreen
import com.seung.sensormaster.ui.screens.tools.battery.BatteryScreen
import com.seung.sensormaster.ui.screens.tools.bluetooth.BluetoothScreen
import com.seung.sensormaster.ui.screens.tools.color.ColorDetectorScreen
import com.seung.sensormaster.ui.screens.tools.compass.CompassScreen
import com.seung.sensormaster.ui.screens.tools.deviceinfo.DeviceInfoScreen
import com.seung.sensormaster.ui.screens.tools.doppler.DopplerScreen
import com.seung.sensormaster.ui.screens.tools.gforce.GForceScreen
import com.seung.sensormaster.ui.screens.tools.gps.GpsScreen
import com.seung.sensormaster.ui.screens.tools.level.LevelScreen
import com.seung.sensormaster.ui.screens.tools.light.LightScreen
import com.seung.sensormaster.ui.screens.tools.metal.MetalDetectorScreen
import com.seung.sensormaster.ui.screens.tools.nfc.NfcScreen
import com.seung.sensormaster.ui.screens.tools.pedometer.PedometerScreen
import com.seung.sensormaster.ui.screens.tools.rpm.RpmScreen
import com.seung.sensormaster.ui.screens.tools.sound.SoundMeterScreen
import com.seung.sensormaster.ui.screens.tools.speedometer.SpeedometerScreen
import com.seung.sensormaster.ui.screens.tools.tone.ToneGeneratorScreen
import com.seung.sensormaster.ui.screens.tools.verticalspeed.VerticalSpeedScreen
import com.seung.sensormaster.ui.screens.tools.heartrate.HeartRateScreen
import com.seung.sensormaster.ui.screens.tools.protractor.ProtractorScreen
import com.seung.sensormaster.ui.screens.tools.vibrometer.VibrometerScreen
import com.seung.sensormaster.ui.screens.tools.wifi.WifiScreen

@Composable
fun SensorMasterNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // ── 홈 화면 ──
        composable("home") {
            HomeScreen(
                onCategoryClick = { category ->
                    navController.navigate(category.route)
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        // ── 설정 화면 ──
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        // ── 카테고리 상세 화면 ──
        composable(
            route = "category/{categoryId}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
            val category = ToolCategory.entries.find { it.route == "category/$categoryId" }
                ?: return@composable

            CategoryScreen(
                category = category,
                onToolClick = { tool ->
                    navController.navigate("tool/${tool.id}")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── 도구 화면 ──
        composable(
            route = "tool/{toolId}",
            arguments = listOf(
                navArgument("toolId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val toolId = backStackEntry.arguments?.getString("toolId") ?: return@composable
            val onBack: () -> Unit = { navController.popBackStack() }

            when (toolId) {
                // Phase 1
                "compass" -> CompassScreen(onBack = onBack)
                "spirit_level" -> LevelScreen(onBack = onBack)
                "protractor" -> ProtractorScreen(onBack = onBack)
                "light_meter" -> LightScreen(onBack = onBack)
                "metal_detector" -> MetalDetectorScreen(onBack = onBack)
                "device_info" -> DeviceInfoScreen(onBack = onBack)

                // Phase 2
                "sound_meter" -> SoundMeterScreen(onBack = onBack)
                "tone_generator" -> ToneGeneratorScreen(onBack = onBack)
                "altimeter" -> AltimeterScreen(onBack = onBack)
                "speedometer" -> SpeedometerScreen(onBack = onBack)
                "vertical_speed" -> VerticalSpeedScreen(onBack = onBack)
                "g_force" -> GForceScreen(onBack = onBack)
                "vibrometer" -> VibrometerScreen(onBack = onBack)

                // Phase 3 — 무선&고급
                "gps_radar" -> GpsScreen(onBack = onBack)
                "color_detector" -> ColorDetectorScreen(onBack = onBack)
                "doppler" -> DopplerScreen(onBack = onBack)
                "rpm_meter" -> RpmScreen(onBack = onBack)
                "wifi_analyzer" -> WifiScreen(onBack = onBack)
                "bt_scanner" -> BluetoothScreen(onBack = onBack)
                "nfc_reader" -> NfcScreen(onBack = onBack)

                // Phase 4 선행
                "battery" -> BatteryScreen(onBack = onBack)
                "pedometer" -> PedometerScreen(onBack = onBack)
                "heart_rate" -> HeartRateScreen(onBack = onBack)

                // 미구현: 플레이스홀더
                else -> {
                    val tool = SensorTools.all.find { it.id == toolId }
                    ToolPlaceholderScreen(
                        toolName = tool?.name ?: "알 수 없는 도구",
                        onBack = onBack
                    )
                }
            }
        }
    }
}
