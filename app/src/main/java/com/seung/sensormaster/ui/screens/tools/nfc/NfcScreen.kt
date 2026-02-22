package com.seung.sensormaster.ui.screens.tools.nfc

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.components.PulseRingBackground
import com.seung.sensormaster.ui.theme.LocalExtendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScreen(
    onBack: () -> Unit,
    viewModel: NfcViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ext = LocalExtendedColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC 리더") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로") } }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            PulseRingBackground(color = ext.neonCyan)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Nfc, null, modifier = Modifier.size(64.dp), tint = ext.neonCyan)
                Spacer(Modifier.height(16.dp))

                if (!state.isAvailable) {
                    Text("NFC를 지원하지 않는 기기입니다", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                } else if (!state.isEnabled) {
                    Text("NFC가 비활성화되어 있습니다", style = MaterialTheme.typography.titleMedium, color = ext.neonAmber)
                    Text("설정에서 NFC를 켜주세요", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (state.lastTag != null) {
                    val tag = state.lastTag!!
                    Text("태그 감지됨!", style = MaterialTheme.typography.titleMedium, color = ext.neonGreen)
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(tag.id, style = MaterialTheme.typography.bodyLarge)
                            
                            Spacer(Modifier.height(12.dp))
                            Text("표준 기술", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(tag.friendlyTech, style = MaterialTheme.typography.bodyMedium)
                            
                            if (!tag.message.isNullOrEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text("데이터 (NDEF)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(tag.message, style = MaterialTheme.typography.bodyMedium)
                            }
                            
                            Spacer(Modifier.height(12.dp))
                            Text("상세 프로토콜", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(tag.techList, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Text("NFC 태그를 기기에 가까이 대세요", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("태그가 감지되면 정보가 표시됩니다", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
