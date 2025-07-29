package com.diploma.work.ui.feature.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.available_tests), style = TextStyle.TitleLarge.value) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
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
            if (viewModel.isLoading.collectAsState().value) {
                LoadingCard(
                    message = stringResource(R.string.loading_tests),
                    modifier = Modifier.fillMaxSize()
                )
            }
            else if (viewModel.errorMessage.collectAsState().value != null) {
                ErrorCard(
                    error = viewModel.errorMessage.collectAsState().value!!,
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
                TestsList(
                    tests = state.tests,
                    onTestSelect = { test ->
                        navController.safeNavigate("TestDetails/${test.id}")
                    }
                )
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
fun TestsList(
    tests: List<TestInfo>,
    onTestSelect: (TestInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tests) { test ->
            TestCard(test = test, onClick = { onTestSelect(test) })
        }
    }
}

@Composable
fun TestCard(
    test: TestInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = test.title,
                style = TextStyle.HeadlineSmall.value,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = test.description,
                style = TextStyle.BodyMedium.value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = test.technologyName,
                        style = TextStyle.LabelMedium.value,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = test.level.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = TextStyle.LabelMedium.value,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
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
