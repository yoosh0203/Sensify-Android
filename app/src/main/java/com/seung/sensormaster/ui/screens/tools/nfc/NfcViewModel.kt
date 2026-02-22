package com.seung.sensormaster.ui.screens.tools.nfc

import android.content.Context
import android.nfc.NfcAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seung.sensormaster.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NfcTagDisplayInfo(
    val id: String,
    val techList: String,
    val message: String?,
    val friendlyTech: String = "" // 읽기 쉬운 기술 명칭
)

data class NfcState(
    val isAvailable: Boolean = false,
    val isEnabled: Boolean = false,
    val lastTag: NfcTagDisplayInfo? = null,
    val isScanning: Boolean = false,
    val tagCount: Int = 0
)

@HiltViewModel
class NfcViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(NfcState())
    val state: StateFlow<NfcState> = _state.asStateFlow()

    private val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

    init {
        _state.value = NfcState(
            isAvailable = nfcAdapter != null,
            isEnabled = nfcAdapter?.isEnabled ?: false
        )

        // MainActivity에서 오는 NFC 태그 이벤트 수신
        viewModelScope.launch {
            MainActivity.nfcTagFlow.collect { event ->
                val friendly = translateTechList(event.techList)
                _state.value = _state.value.copy(
                    lastTag = NfcTagDisplayInfo(
                        id = event.id,
                        techList = event.techList,
                        message = event.message,
                        friendlyTech = friendly
                    ),
                    tagCount = _state.value.tagCount + 1
                )
            }
        }
    }

    private fun translateTechList(techs: String): String {
        return techs.split(", ")
            .map { it.trim() }
            .map { tech ->
                when (tech) {
                    "NfcA" -> "ISO 14443-3A"
                    "NfcB" -> "ISO 14443-3B"
                    "NfcF" -> "JIS 6319-4 (FeliCa)"
                    "NfcV" -> "ISO 15693 (Vicinity)"
                    "IsoDep" -> "ISO 14443-4"
                    "Ndef" -> "NDEF 지원"
                    "NdefFormatable" -> "NDEF 포맷 가능"
                    "MifareClassic" -> "MIFARE Classic"
                    "MifareUltralight" -> "MIFARE Ultralight"
                    else -> tech
                }
            }.joinToString(", ")
    }
}
