package com.seung.sensormaster.ui.screens.tools.doppler

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
import com.seung.sensormaster.ui.components.PulseRingBackground
import com.seung.sensormaster.ui.theme.LocalExtendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DopplerScreen(
    onBack: () -> Unit,
    viewModel: DopplerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ext = LocalExtendedColors.current
    var hasPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }

    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.RECORD_AUDIO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("도플러 속도계") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isActive) PulseRingBackground(color = ext.neonCyan)

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                Box(contentAlignment = Alignment.Center) {
                    NeonRingGauge(
                        progress = (state.velocity / 5f).coerceIn(0f, 1f),
                        glowColor = when (state.direction) {
                            "접근 중" -> ext.neonGreen
                            "멀어짐" -> ext.neonPink
                            else -> ext.neonCyan
                        },
                        modifier = Modifier.size(220.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("%.2f".format(state.velocity), style = MaterialTheme.typography.displayMedium)
                        Text("m/s", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    state.direction,
                    style = MaterialTheme.typography.titleLarge,
                    color = when (state.direction) {
                        "접근 중" -> ext.neonGreen
                        "멀어짐" -> ext.neonPink
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text("주파수 이동: %.1f Hz".format(state.shiftHz), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(32.dp))

                @Suppress("MissingPermission")
                Button(
                    onClick = {
                        if (state.isActive) viewModel.stop()
                        else if (hasPermission) viewModel.start()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isActive) MaterialTheme.colorScheme.error else ext.neonCyan
                    )
                ) {
                    Icon(if (state.isActive) Icons.Outlined.Stop else Icons.Outlined.PlayArrow, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isActive) "측정 중지" else "측정 시작")
                }

                Spacer(Modifier.height(16.dp))
                Text("물체를 기기 앞에서 앞뒤로 움직이세요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
