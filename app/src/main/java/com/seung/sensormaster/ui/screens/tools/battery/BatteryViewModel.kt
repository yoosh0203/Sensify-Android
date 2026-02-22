package com.seung.sensormaster.ui.screens.tools.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class BatteryState(
    val level: Int = 0,
    val voltage: Float = 0f,
    val temperature: Float = 0f,
    val health: String = "알 수 없음",
    val status: String = "알 수 없음",
    val plugType: String = "없음",
    val technology: String = "알 수 없음"
)

@HiltViewModel
class BatteryViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(BatteryState())
    val state: StateFlow<BatteryState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        intent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val pct = (level * 100) / scale
            val voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
            val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f

            val healthInt = it.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
            val health = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "양호"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "과열"
                BatteryManager.BATTERY_HEALTH_DEAD -> "사용 불가"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "과전압"
                BatteryManager.BATTERY_HEALTH_COLD -> "저온"
                else -> "알 수 없음"
            }

            val statusInt = it.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
            val status = when (statusInt) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "충전 중"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "방전 중"
                BatteryManager.BATTERY_STATUS_FULL -> "완충"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "충전 안 함"
                else -> "알 수 없음"
            }

            val plugInt = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val plug = when (plugInt) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC 어댑터"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "무선 충전"
                else -> "없음"
            }

            val tech = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "알 수 없음"

            _state.value = BatteryState(pct, voltage, temp, health, status, plug, tech)
        }
    }
}
