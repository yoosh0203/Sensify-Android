package com.seung.sensormaster.ui.screens.tools.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SatelliteInfo(
    val svid: Int,
    val constellation: String,
    val elevation: Float,
    val azimuth: Float,
    val cn0: Float,
    val usedInFix: Boolean
)

data class GpsState(
    val satellites: List<SatelliteInfo> = emptyList(),
    val totalCount: Int = 0,
    val usedCount: Int = 0,
    val error: String? = null,
    val isListening: Boolean = false
)

@HiltViewModel
class GpsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(GpsState())
    val state: StateFlow<GpsState> = _state.asStateFlow()

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var gnssCallback: GnssStatus.Callback? = null

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (_state.value.isListening) return
        _state.value = _state.value.copy(isListening = true, error = null)

        gnssCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val sats = (0 until status.satelliteCount).map { i ->
                    val constellation = when (status.getConstellationType(i)) {
                        GnssStatus.CONSTELLATION_GPS -> "GPS"
                        GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                        GnssStatus.CONSTELLATION_BEIDOU -> "BeiDou"
                        GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
                        GnssStatus.CONSTELLATION_QZSS -> "QZSS"
                        else -> "기타"
                    }
                    SatelliteInfo(
                        svid = status.getSvid(i),
                        constellation = constellation,
                        elevation = status.getElevationDegrees(i),
                        azimuth = status.getAzimuthDegrees(i),
                        cn0 = status.getCn0DbHz(i),
                        usedInFix = status.usedInFix(i)
                    )
                }
                _state.value = GpsState(
                    satellites = sats,
                    totalCount = sats.size,
                    usedCount = sats.count { it.usedInFix },
                    isListening = true
                )
            }
        }
        try {
            locationManager.registerGnssStatusCallback(gnssCallback!!, null)
        } catch (e: SecurityException) {
            _state.value = GpsState(error = "위치 권한이 필요합니다")
        }
    }

    fun stopListening() {
        gnssCallback?.let { locationManager.unregisterGnssStatusCallback(it) }
        gnssCallback = null
        _state.value = _state.value.copy(isListening = false)
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
