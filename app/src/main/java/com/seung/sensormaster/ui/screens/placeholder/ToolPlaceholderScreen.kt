package com.seung.sensormaster.ui.screens.placeholder

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seung.sensormaster.ui.components.AdvancedModeToggle
import com.seung.sensormaster.ui.components.AdvancedPanel
import com.seung.sensormaster.ui.components.AdvancedDataRow

/**
 * 개별 도구의 플레이스홀더 화면
 * Phase 1+ 에서 실제 구현으로 교체됩니다.
 * 고급 모드 토글 시스템의 데모를 포함합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolPlaceholderScreen(
    toolName: String,
    onBack: () -> Unit
) {
    var isAdvancedMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(toolName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "뒤로"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // 고급 모드 토글
            AdvancedModeToggle(
                isAdvanced = isAdvancedMode,
                onToggle = { isAdvancedMode = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 메인 컨텐츠 영역 (플레이스홀더)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Construction,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "$toolName 구현 예정",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Phase 1+ 에서 활성화됩니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // 고급 모드 패널 (데모)
            AdvancedPanel(isVisible = isAdvancedMode) {
                Text(
                    text = "전문가 데이터",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AdvancedDataRow(label = "센서 타입", value = "데모 데이터")
                AdvancedDataRow(label = "샘플링 주기", value = "—")
                AdvancedDataRow(label = "정밀도", value = "—")
            }
        }
    }
}
