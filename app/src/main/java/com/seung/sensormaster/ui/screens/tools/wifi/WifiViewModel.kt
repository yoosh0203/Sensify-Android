package com.seung.sensormaster.ui.screens.tools.wifi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val level: Int,      // dBm
    val frequency: Int,  // MHz
    val channel: Int,
    val security: String = "Open",
    val signalHistory: List<Int> = emptyList() // 최근 신호 강도 추이
)

data class WifiState(
    val networks: List<WifiNetwork> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WifiViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(WifiState())
    val state: StateFlow<WifiState> = _state.asStateFlow()

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun scan() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            _state.value = _state.value.copy(error = "위치 권한이 필요합니다")
            return
        }
        _state.value = _state.value.copy(isScanning = true, error = null)
        try {
            @Suppress("DEPRECATION")
            val results = wifiManager.scanResults
            val networks = results.map { sr ->
                WifiNetwork(
                    ssid = sr.SSID.ifEmpty { "(숨겨진 네트워크)" },
                    bssid = sr.BSSID,
                    level = sr.level,
                    frequency = sr.frequency,
                    channel = frequencyToChannel(sr.frequency),
                    security = parseCapabilities(sr.capabilities)
                )
            }.sortedByDescending { it.level }
            _state.value = WifiState(networks = networks)
        } catch (e: Exception) {
            _state.value = WifiState(error = e.message)
        }
    }

    private fun parseCapabilities(cap: String): String {
        return when {
            cap.contains("WPA3") -> "WPA3"
            cap.contains("WPA2") -> "WPA2"
            cap.contains("WPA") -> "WPA"
            cap.contains("WEP") -> "WEP"
            else -> if (cap.contains("ESS")) "Open" else "Unknown"
        }
    }

    private fun frequencyToChannel(freq: Int): Int = when {
        freq in 2412..2484 -> (freq - 2412) / 5 + 1
        freq in 5170..5825 -> (freq - 5170) / 5 + 34
        else -> 0
    }
}
