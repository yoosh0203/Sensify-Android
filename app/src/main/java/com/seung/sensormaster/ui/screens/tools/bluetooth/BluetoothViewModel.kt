package com.seung.sensormaster.ui.screens.tools.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val deviceType: String = "Generic" // Phone, Audio, Computer, etc.
)

data class BluetoothState(
    val devices: List<BleDevice> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothState())
    val state: StateFlow<BluetoothState> = _state.asStateFlow()

    private val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val deviceMap = mutableMapOf<String, BleDevice>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            @SuppressLint("MissingPermission")
            val name = result.device.name ?: "(알 수 없음)"
            val addr = result.device.address
            val type = parseDeviceType(result.device.bluetoothClass?.majorDeviceClass ?: 0)
            deviceMap[addr] = BleDevice(name, addr, result.rssi, type)
            _state.value = _state.value.copy(devices = deviceMap.values.sortedByDescending { it.rssi })
        }
        override fun onScanFailed(errorCode: Int) {
            _state.value = _state.value.copy(isScanning = false, error = "스캔 실패 (코드: $errorCode)")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        try {
            deviceMap.clear()
            _state.value = BluetoothState(isScanning = true)
            btManager?.adapter?.bluetoothLeScanner?.startScan(scanCallback)
        } catch (e: SecurityException) {
            _state.value = BluetoothState(error = "블루투스 권한이 필요합니다")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            btManager?.adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (_: Exception) {}
        _state.value = _state.value.copy(isScanning = false)
    }

    private fun parseDeviceType(majorClass: Int): String {
        return when (majorClass) {
            0x0100 -> "Computer"
            0x0200 -> "Phone"
            0x0400 -> "Audio/Video"
            0x0500 -> "Peripheral"
            0x0600 -> "Imaging"
            0x0700 -> "Wearable"
            0x0800 -> "Toy"
            0x0900 -> "Health"
            else -> "Generic"
        }
    }

    override fun onCleared() {
        stopScan()
        super.onCleared()
    }
}
