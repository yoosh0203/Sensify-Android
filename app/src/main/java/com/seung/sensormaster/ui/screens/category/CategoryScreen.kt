package com.seung.sensormaster.ui.screens.category

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.seung.sensormaster.data.model.SensorTool
import com.seung.sensormaster.data.model.SensorTools
import com.seung.sensormaster.data.model.ToolCategory
import com.seung.sensormaster.ui.components.MeshGradientBackground
import com.seung.sensormaster.ui.components.ToolListItem
import com.seung.sensormaster.ui.theme.LocalExtendedColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    category: ToolCategory,
    onToolClick: (SensorTool) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val tools = remember(category) { viewModel.getFilteredTools(category) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val ext = LocalExtendedColors.current
    val categoryIndex = ToolCategory.entries.indexOf(category)
    val gradient = if (categoryIndex < ext.categoryGradients.size)
        ext.categoryGradients[categoryIndex] else ext.categoryGradients.last()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshGradientBackground()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = { Text(category.label) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                )
            }
        ) { innerPadding ->
            BoxWithConstraints(modifier = Modifier.padding(innerPadding)) {
                val columns = if (maxWidth >= 600.dp) 2 else 1

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 카테고리 배너 (전체 너비 차지)
                    item(span = { GridItemSpan(columns) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            gradient.start.copy(alpha = 0.15f),
                                            gradient.end.copy(alpha = 0.08f)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    category.icon, null,
                                    tint = gradient.start,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        category.label,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        category.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 도구 아이템 (staggered 진입)
                    itemsIndexed(tools) { idx, tool ->
                        val itemVisible = remember { mutableStateOf(false) }
                        LaunchedEffect(visible) {
                            if (visible) {
                                delay(idx * 50L + 100)
                                itemVisible.value = true
                            }
                        }
                        val animAlpha by animateFloatAsState(
                            targetValue = if (itemVisible.value) 1f else 0f,
                            animationSpec = tween(350, easing = FastOutSlowInEasing), label = "a_$idx"
                        )
                        val animTranslate by animateFloatAsState(
                            targetValue = if (itemVisible.value) 0f else 30f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ), label = "t_$idx"
                        )

                        ToolListItem(
                            icon = tool.icon,
                            name = tool.name,
                            subtitle = tool.subtitle,
                            onClick = { onToolClick(tool) },
                            accentColor = gradient.start,
                            modifier = Modifier
                                .alpha(animAlpha)
                                .graphicsLayer { translationY = animTranslate }
                        )
                    }
                }
            }
        }
    }
}
