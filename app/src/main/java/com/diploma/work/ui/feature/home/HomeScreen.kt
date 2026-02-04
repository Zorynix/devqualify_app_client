package com.diploma.work.ui.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.R
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import com.diploma.work.data.models.Technology
import com.diploma.work.data.models.TestInfo
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.components.LoadingCard
import com.diploma.work.ui.navigation.TestDetails
import com.diploma.work.ui.navigation.safeNavigate
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle
import com.orhanobut.logger.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.available_tests), style = TextStyle.TitleLarge.value) },
                navigationIcon = {
                    IconButton(onClick = {
                        Logger.d("Navigation: Menu button clicked in Home screen")
                        onOpenDrawer()
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.open_menu))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = !showFilterDialog }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    title = { Text(stringResource(R.string.filter), style = TextStyle.TitleLarge.value) },
                    text = {
                        Column {
                            val allText = stringResource(R.string.all)

                            Text(
                                text = stringResource(R.string.filter_by_direction),
                                style = TextStyle.LabelMedium.value,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            ChipGroup(
                                items = listOf(null) + Direction.entries.filter { it != Direction.UNSPECIFIED },
                                selectedItem = state.selectedDirection?.toModelDirection(),
                                onSelectChange = { direction ->
                                    viewModel.selectDirection(direction?.toProtoDirection())
                                },
                                chipLabel = { it?.name?.lowercase()?.replaceFirstChar { c -> c.uppercase() } ?: allText }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.filter_by_level),
                                style = TextStyle.LabelMedium.value,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            ChipGroup(
                                items = listOf(null) + Level.entries.filter { it != Level.UNSPECIFIED },
                                selectedItem = state.selectedLevel?.toModelLevel(),
                                onSelectChange = { level ->
                                    viewModel.selectLevel(level?.toProtoLevel())
                                },
                                chipLabel = { it?.name?.lowercase()?.replaceFirstChar { c -> c.uppercase() } ?: allText }
                            )

                            if (state.technologies.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = stringResource(R.string.filter_by_technology),
                                    style = TextStyle.LabelMedium.value,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                ChipGroup(
                                    items = listOf(null) + state.technologies,
                                    selectedItem = state.selectedTechnology,
                                    onSelectChange = { technology ->
                                        viewModel.selectTechnology(technology)
                                    },
                                    chipLabel = { it?.name ?: allText }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showFilterDialog = false }
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                )
            }

            if (isLoading) {
                LoadingCard(
                    message = stringResource(R.string.loading_tests),
                    modifier = Modifier.fillMaxSize()
                )
            } else if (errorMessage != null) {
                ErrorCard(
                    error = errorMessage!!,
                    onRetry = { viewModel.loadTests() },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (state.tests.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_tests_available),
                        style = TextStyle.BodyLarge.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                TestsListWithCategories(
                    tests = state.tests,
                    selectedLevel = state.selectedLevel?.toModelLevel(),
                    onLevelSelect = { level ->
                        viewModel.selectLevel(level?.toProtoLevel())
                    },
                    onTestSelect = { test ->
                        navController.safeNavigate("TestDetails/${test.id}")
                    }
                )
            }
        }
    }
}

@Composable
private fun TestsListWithCategories(
    tests: List<TestInfo>,
    selectedLevel: Level?,
    onLevelSelect: (Level?) -> Unit,
    onTestSelect: (TestInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Выберите уровень",
                style = TextStyle.TitleMedium.value,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LevelCategoryCard(
                    level = Level.JUNIOR,
                    icon = Icons.Default.School,
                    gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)),
                    testsCount = tests.count { it.level == Level.JUNIOR },
                    isSelected = selectedLevel == Level.JUNIOR,
                    onClick = {
                        onLevelSelect(if (selectedLevel == Level.JUNIOR) null else Level.JUNIOR)
                    },
                    modifier = Modifier.weight(1f)
                )

                LevelCategoryCard(
                    level = Level.MIDDLE,
                    icon = Icons.Default.TrendingUp,
                    gradientColors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4)),
                    testsCount = tests.count { it.level == Level.MIDDLE },
                    isSelected = selectedLevel == Level.MIDDLE,
                    onClick = {
                        onLevelSelect(if (selectedLevel == Level.MIDDLE) null else Level.MIDDLE)
                    },
                    modifier = Modifier.weight(1f)
                )

                LevelCategoryCard(
                    level = Level.SENIOR,
                    icon = Icons.Default.WorkspacePremium,
                    gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
                    testsCount = tests.count { it.level == Level.SENIOR },
                    isSelected = selectedLevel == Level.SENIOR,
                    onClick = {
                        onLevelSelect(if (selectedLevel == Level.SENIOR) null else Level.SENIOR)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }


        item {
            val filteredTests = if (selectedLevel != null) {
                tests.filter { it.level == selectedLevel }
            } else {
                tests
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedLevel != null) {
                        "Тесты ${selectedLevel.name.lowercase().replaceFirstChar { it.uppercase() }}"
                    } else {
                        "Все тесты"
                    },
                    style = TextStyle.TitleMedium.value,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (selectedLevel != null) {
                    TextButton(onClick = { onLevelSelect(null) }) {
                        Text("Сбросить")
                    }
                }
            }
        }


        val filteredTests = if (selectedLevel != null) {
            tests.filter { it.level == selectedLevel }
        } else {
            tests
        }

        items(filteredTests) { test ->
            TestCard(test = test, onClick = { onTestSelect(test) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelCategoryCard(
    level: Level,
    icon: ImageVector,
    gradientColors: List<Color>,
    testsCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val levelName = when (level) {
        Level.JUNIOR -> "Junior"
        Level.MIDDLE -> "Middle"
        Level.SENIOR -> "Senior"
        else -> ""
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(colors = gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = levelName,
                        style = TextStyle.TitleSmall.value,
                        color = Color.White
                    )
                    Text(
                        text = "$testsCount тестов",
                        style = TextStyle.BodySmall.value,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChipGroup(
    items: List<T>,
    selectedItem: T?,
    onSelectChange: (T) -> Unit,
    chipLabel: (T) -> String
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            FilterChip(
                selected = item == selectedItem,
                onClick = { onSelectChange(item) },
                label = { Text(chipLabel(item)) },
                leadingIcon = if (item == selectedItem) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.selected),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun TestCard(
    test: TestInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val levelColor = when (test.level) {
        Level.JUNIOR -> Color(0xFF4CAF50)
        Level.MIDDLE -> Color(0xFF2196F3)
        Level.SENIOR -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = levelColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = levelColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = test.title,
                    style = TextStyle.TitleMedium.value,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = test.description,
                    style = TextStyle.BodySmall.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = test.technologyName,
                            style = TextStyle.LabelSmall.value,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = levelColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = test.level.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = TextStyle.LabelSmall.value,
                            color = levelColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun Direction.toProtoDirection(): Direction {
    return when (this) {
        Direction.UNSPECIFIED -> Direction.UNSPECIFIED
        Direction.BACKEND -> Direction.BACKEND
        Direction.FRONTEND -> Direction.FRONTEND
        Direction.DEVOPS -> Direction.DEVOPS
        Direction.DATA_SCIENCE -> Direction.DATA_SCIENCE
    }
}

private fun Direction.toModelDirection(): Direction {
    return when (this) {
        Direction.UNSPECIFIED -> Direction.UNSPECIFIED
        Direction.BACKEND -> Direction.BACKEND
        Direction.FRONTEND -> Direction.FRONTEND
        Direction.DEVOPS -> Direction.DEVOPS
        Direction.DATA_SCIENCE -> Direction.DATA_SCIENCE
    }
}

private fun Level.toProtoLevel(): Level {
    return when (this) {
        Level.UNSPECIFIED -> Level.UNSPECIFIED
        Level.JUNIOR -> Level.JUNIOR
        Level.MIDDLE -> Level.MIDDLE
        Level.SENIOR -> Level.SENIOR
    }
}

private fun Level.toModelLevel(): Level {
    return when (this) {
        Level.UNSPECIFIED -> Level.UNSPECIFIED
        Level.JUNIOR -> Level.JUNIOR
        Level.MIDDLE -> Level.MIDDLE
        Level.SENIOR -> Level.SENIOR
    }
}
