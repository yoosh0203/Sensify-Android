package com.seung.sensormaster.ui.screens.tools.bluetooth

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.BluetoothSearching
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.components.NeonBarGauge
import com.seung.sensormaster.ui.theme.LocalExtendedColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    onBack: () -> Unit,
    viewModel: BluetoothViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ext = LocalExtendedColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BT 스캐너") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로") } },
                actions = {
                    if (state.isScanning) {
                        IconButton(onClick = { viewModel.stopScan() }) { Icon(Icons.Outlined.Stop, "정지") }
                    } else {
                        IconButton(onClick = { viewModel.startScan() }) { Icon(Icons.Outlined.BluetoothSearching, "스캔") }
                    }
                }
            )
        }
    ) { padding ->
        if (state.error != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
            }
        } else if (state.devices.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Bluetooth, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(if (state.isScanning) "스캔 중..." else "스캔 버튼을 눌러 BLE 기기를 검색하세요", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.devices) { idx, device ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { delay(idx * 40L); visible = true }
                    val a by animateFloatAsState(if (visible) 1f else 0f, tween(300), label = "a")
                    val ty by animateFloatAsState(if (visible) 0f else 20f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "t")

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth().alpha(a).graphicsLayer { translationY = ty }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Bluetooth, null, tint = ext.neonPurple, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(device.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text("${device.rssi} dBm", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(8.dp))
                            NeonBarGauge(
                                progress = ((device.rssi + 100) / 70f).coerceIn(0f, 1f),
                                glowColor = ext.neonPurple,
                                modifier = Modifier.fillMaxWidth().height(6.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    device.address,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        device.deviceType,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
