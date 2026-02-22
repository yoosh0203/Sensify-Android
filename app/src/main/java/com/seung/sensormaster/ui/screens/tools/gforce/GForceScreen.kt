package com.seung.sensormaster.ui.screens.tools.gforce

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seung.sensormaster.ui.components.AdvancedDataRow
import com.seung.sensormaster.ui.components.AdvancedPanel
import com.seung.sensormaster.ui.screens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GForceScreen(
    onBack: () -> Unit,
    viewModel: GForceViewModel = hiltViewModel(),
    settingsVM: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAdvancedMode by settingsVM.advancedMode.collectAsStateWithLifecycle()

    val gColor = when {
        state.totalG < 0.5f -> Color(0xFF2196F3)
        state.totalG in 0.8f..1.2f -> Color(0xFF4CAF50)
        state.totalG < 3f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("G-Force") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetMax() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "최대값 리셋")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "%.2f".format(state.totalG),
                style = MaterialTheme.typography.displayLarge,
                color = gColor
            )
            Text("G", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = if (state.totalG > 4f) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(state.context, style = MaterialTheme.typography.titleMedium,
                    color = if (state.totalG > 4f) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("최소", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (state.minG < Float.MAX_VALUE) "%.2f G".format(state.minG) else "—",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("평균", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("%.2f G".format(state.avgG), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("최대", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    Text("%.2f G".format(state.maxG), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AdvancedPanel(isVisible = isAdvancedMode) {
                Text("G-Force 상세", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                AdvancedDataRow(label = "Total G", value = "%.4f".format(state.totalG))
                AdvancedDataRow(label = "X축 G", value = "%.4f".format(state.gX))
                AdvancedDataRow(label = "Y축 G", value = "%.4f".format(state.gY))
                AdvancedDataRow(label = "Z축 G", value = "%.4f".format(state.gZ))
                AdvancedDataRow(label = "최대 G", value = "%.4f".format(state.maxG))
                AdvancedDataRow(label = "최소 G", value = if (state.minG < Float.MAX_VALUE) "%.4f".format(state.minG) else "—")
                AdvancedDataRow(label = "평균 G", value = "%.4f".format(state.avgG))
            }
        }
    }
}
