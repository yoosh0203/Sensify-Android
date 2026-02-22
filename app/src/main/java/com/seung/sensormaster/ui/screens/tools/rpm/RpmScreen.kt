package com.seung.sensormaster.ui.screens.tools.rpm

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.components.NeonRingGauge
import com.seung.sensormaster.ui.theme.LocalExtendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RpmScreen(
    onBack: () -> Unit,
    viewModel: RpmViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ext = LocalExtendedColors.current
    var hasPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }

    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.RECORD_AUDIO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RPM 측정기") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                NeonRingGauge(
                    progress = (state.rpm / 6000f).coerceIn(0f, 1f),
                    glowColor = ext.neonAmber,
                    modifier = Modifier.size(220.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%.0f".format(state.rpm), style = MaterialTheme.typography.displayMedium)
                    Text("RPM", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("기본 주파수: %.1f Hz".format(state.frequency), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(32.dp))

            @Suppress("MissingPermission")
            Button(
                onClick = {
                    if (state.isRecording) viewModel.stopRecording()
                    else if (hasPermission) viewModel.startRecording()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isRecording) MaterialTheme.colorScheme.error else ext.neonAmber
                )
            ) {
                Icon(
                    if (state.isRecording) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                    null, modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (state.isRecording) "측정 중지" else "측정 시작")
            }

            Spacer(Modifier.height(16.dp))
            Text("회전 소리를 마이크에 가까이 대세요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
