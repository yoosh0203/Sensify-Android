package com.seung.sensormaster

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.data.settings.ThemeMode
import com.seung.sensormaster.data.sensor.SensorAvailabilityManager
import com.seung.sensormaster.ui.SensorMasterNavHost
import com.seung.sensormaster.ui.screens.settings.SettingsViewModel
import com.seung.sensormaster.ui.theme.SensorMasterTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sensorAvailabilityManager: SensorAvailabilityManager

    private var nfcAdapter: NfcAdapter? = null
    private var pendingNfcIntent: PendingIntent? = null

    companion object {
        private val _nfcTagFlow = MutableSharedFlow<NfcTagEvent>(extraBufferCapacity = 1)
        val nfcTagFlow = _nfcTagFlow.asSharedFlow()
    }

    data class NfcTagEvent(
        val id: String,
        val techList: String,
        val message: String?
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 센서 가용성 초기화 (최초 1회)
        sensorAvailabilityManager.initializeIfNeeded()

        // NFC 설정
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingNfcIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        setContent {
            val settingsVM: SettingsViewModel = hiltViewModel()
            val themeMode by settingsVM.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by settingsVM.dynamicColor.collectAsStateWithLifecycle()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            SensorMasterTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SensorMasterNavHost()
                }
            }
        }

        // 앱이 NFC intent로 실행된 경우 처리
        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingNfcIntent,
            arrayOf(
                IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                    try { addDataType("*/*") } catch (_: Exception) {}
                },
                IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
            ),
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED -> {
                val tag = if (android.os.Build.VERSION.SDK_INT >= 33) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                }
                tag?.let { t ->
                    val id = t.id?.joinToString(":") { "%02X".format(it) } ?: "Unknown"
                    val techList = t.techList?.joinToString(", ") { it.substringAfterLast('.') } ?: ""
                    val ndefMessages = if (android.os.Build.VERSION.SDK_INT >= 33) {
                        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                    }
                    val message = ndefMessages?.let { msgs ->
                        msgs.filterIsInstance<NdefMessage>()
                            .flatMap { it.records.toList() }
                            .joinToString("\n") { record ->
                                String(record.payload).drop(if (record.payload.size > 1) 3 else 0)
                            }
                            .ifEmpty { null }
                    }
                    _nfcTagFlow.tryEmit(NfcTagEvent(id, techList, message))
                }
            }
        }
    }
}
