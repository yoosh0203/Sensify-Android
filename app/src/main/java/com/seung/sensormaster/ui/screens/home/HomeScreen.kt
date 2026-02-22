package com.seung.sensormaster.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.seung.sensormaster.data.model.ToolCategory
import com.seung.sensormaster.ui.components.CategoryCard
import com.seung.sensormaster.ui.theme.LocalExtendedColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (ToolCategory) -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val ext = LocalExtendedColors.current
    val categories = ToolCategory.entries.toTypedArray()

    // Staggered 진입 애니메이션
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("Sensify")
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "설정",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.padding(innerPadding)) {
            val columns = when {
                maxWidth >= 840.dp -> 4
                maxWidth >= 600.dp -> 3
                else -> 2
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(categories) { index, category ->
                    val gradients = ext.categoryGradients
                    val gradient = if (index < gradients.size) gradients[index]
                                   else gradients.last()

                    // Staggered 진입: 각 아이템이 50ms 간격으로 등장
                    val itemVisible = remember { mutableStateOf(false) }
                    LaunchedEffect(visible) {
                        if (visible) {
                            delay(index * 60L)
                            itemVisible.value = true
                        }
                    }
                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (itemVisible.value) 1f else 0f,
                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                        label = "alpha_$index"
                    )
                    val animatedTranslation by animateFloatAsState(
                        targetValue = if (itemVisible.value) 0f else 40f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "translate_$index"
                    )

                    CategoryCard(
                        icon = category.icon,
                        label = category.label,
                        description = category.description,
                        onClick = { onCategoryClick(category) },
                        gradientStart = gradient.start,
                        gradientEnd = gradient.end,
                        modifier = Modifier
                            .alpha(animatedAlpha)
                            .graphicsLayer { translationY = animatedTranslation }
                    )
                }
            }
        }
    }
}
