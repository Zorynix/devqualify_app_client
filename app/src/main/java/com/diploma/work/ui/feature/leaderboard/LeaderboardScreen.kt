package com.diploma.work.ui.feature.leaderboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.R
import com.diploma.work.data.models.User
import com.diploma.work.grpc.userinfo.Direction
import com.diploma.work.grpc.userinfo.Level
import com.diploma.work.ui.components.AvatarImage
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isRefreshing = uiState.isLoading
    val state = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()
    
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage ?: "An error occurred")
        }
    }

    LaunchedEffect(lazyListState) {
        if (uiState.hasMoreData && !uiState.isLoading) {
            val layoutInfo = lazyListState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            if (lastVisibleItemIndex >= totalItemsCount - 3) {
                viewModel.loadMoreUsers()
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard", style = TextStyle.HeadlineMedium.value) },
                actions = {
                    IconButton(onClick = { viewModel.loadLeaderboard(true) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.loadLeaderboard(true) },
                modifier = Modifier.fillMaxSize(),
                state = state,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isRefreshing,
                        state = state
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CompactFilterSortSection(
                        selectedDirection = uiState.direction,
                        selectedLevel = uiState.level,
                        selectedSortType = uiState.sortType,
                        onDirectionSelected = { viewModel.setDirection(it) },
                        onLevelSelected = { viewModel.setLevel(it) },
                        onSortTypeSelected = { viewModel.setSortType(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (uiState.users.isEmpty() && !uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found for these filters.",
                                style = TextStyle.BodyMedium.value,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.users) { user ->
                                UserListItem(
                                    user = user,
                                    sortType = uiState.sortType,
                                    onClick = { viewModel.selectUser(user) }
                                )
                            }
                            
                            item {
                                if (uiState.isLoading && uiState.users.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
            
            if (isRefreshing && uiState.users.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
    
    if (uiState.isUserDetailDialogVisible && uiState.selectedUser != null) {
        UserDetailDialog(
            user = uiState.selectedUser!!,
            onDismiss = { viewModel.dismissUserDetailDialog() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactFilterSortSection(
    selectedDirection: Direction,
    selectedLevel: Level,
    selectedSortType: LeaderboardSortType,
    onDirectionSelected: (Direction) -> Unit,
    onLevelSelected: (Level) -> Unit,
    onSortTypeSelected: (LeaderboardSortType) -> Unit
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    val filterLabel = when {
        selectedDirection != Direction.DIRECTION_UNSPECIFIED && selectedLevel != Level.LEVEL_UNSPECIFIED -> 
            "${directionToString(selectedDirection)}, ${levelToString(selectedLevel)}"
        selectedDirection != Direction.DIRECTION_UNSPECIFIED -> 
            directionToString(selectedDirection)
        selectedLevel != Level.LEVEL_UNSPECIFIED -> 
            levelToString(selectedLevel)
        else -> "Filter"
    }
    
    val hasActiveFilters = selectedDirection != Direction.DIRECTION_UNSPECIFIED || 
                          selectedLevel != Level.LEVEL_UNSPECIFIED
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        FilterChip(
            selected = hasActiveFilters,
            onClick = { showFilterDialog = !showFilterDialog },
            label = { Text(filterLabel) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    tint = LocalContentColor.current
                )
            },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = false,
            onClick = { showSortDialog = !showSortDialog },
            label = { Text("Sort: ${selectedSortType.displayName}") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort",
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    tint = LocalContentColor.current
                )
            },
            modifier = Modifier.weight(1f)
        )
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter", style = TextStyle.TitleLarge.value) },
            text = {
                Column {
                    Text(
                        text = "Direction",
                        style = TextStyle.LabelMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ChipGroup(
                        items = listOf(Direction.DIRECTION_UNSPECIFIED) + 
                                Direction.entries.filter { it != Direction.UNRECOGNIZED && it != Direction.DIRECTION_UNSPECIFIED },
                        selectedItem = selectedDirection,
                        onSelectedChanged = { onDirectionSelected(it) },
                        chipLabel = { directionToString(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Level",
                        style = TextStyle.LabelMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ChipGroup(
                        items = listOf(Level.LEVEL_UNSPECIFIED) + 
                                Level.entries.filter { it != Level.UNRECOGNIZED && it != Level.LEVEL_UNSPECIFIED },
                        selectedItem = selectedLevel,
                        onSelectedChanged = { onLevelSelected(it) },
                        chipLabel = { levelToString(it) }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showFilterDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort by", style = TextStyle.TitleLarge.value) },
            text = {
                Column {
                    ChipGroup(
                        items = LeaderboardSortType.entries.toList(),
                        selectedItem = selectedSortType,
                        onSelectedChanged = { 
                            onSortTypeSelected(it)
                            showSortDialog = false
                        },
                        chipLabel = { it.displayName }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSortDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChipGroup(
    items: List<T>,
    selectedItem: T,
    onSelectedChanged: (T) -> Unit,
    chipLabel: (T) -> String
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            FilterChip(
                selected = item == selectedItem,
                onClick = { onSelectedChanged(item) },
                label = { Text(chipLabel(item)) },
                leadingIcon = if (item == selectedItem) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                            tint = LocalContentColor.current
                        )
                    }
                } else null
            )
        }
    }
}


@Composable
fun UserListItem(user: User, sortType: LeaderboardSortType, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val viewModel = hiltViewModel<LeaderboardViewModel>()
            
            AvatarImage(
                avatarUrl = if (user.avatarUrl.isNotEmpty()) user.avatarUrl 
                           else "https://ui-avatars.com/api/?name=${user.username}&background=random&size=200",
                size = 50.dp,
                borderWidth = 2.dp,
                session = viewModel.session
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = user.username,
                    style = TextStyle.TitleMedium.value,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${directionToString(user.direction)}, ${levelToString(user.level)}",
                    style = TextStyle.BodySmall.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                when (sortType) {
                    LeaderboardSortType.ACHIEVEMENTS -> {
                        Text(
                            text = "${user.achievementsCount}",
                            style = TextStyle.TitleMedium.value,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Achievements",
                            style = TextStyle.BodySmall.value,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LeaderboardSortType.CORRECT_ANSWERS -> {
                        Text(
                            text = "${user.totalCorrectAnswers}",
                            style = TextStyle.TitleMedium.value,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Correct Answers",
                            style = TextStyle.BodySmall.value,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LeaderboardSortType.COMPLETED_TESTS -> {
                        Text(
                            text = "${user.completedTestsCount}",
                            style = TextStyle.TitleMedium.value,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Completed Tests",
                            style = TextStyle.BodySmall.value,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailDialog(user: User, onDismiss: () -> Unit) {
    val viewModel = hiltViewModel<LeaderboardViewModel>()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarImage(
                    avatarUrl = if (user.avatarUrl.isNotEmpty()) user.avatarUrl 
                               else "https://ui-avatars.com/api/?name=${user.username}&background=random&size=200",
                    size = 100.dp,
                    borderWidth = 2.dp,
                    session = viewModel.session
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = user.username,
                    style = TextStyle.TitleLarge.value,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${directionToString(user.direction)}, ${levelToString(user.level)}",
                    style = TextStyle.BodyMedium.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Statistics",
                    style = TextStyle.TitleMedium.value,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                StatisticRow("Achievements", "${user.achievementsCount}")
                StatisticRow("Correct Answers", "${user.totalCorrectAnswers}")
                StatisticRow("Incorrect Answers", "${user.totalIncorrectAnswers}")
                StatisticRow("Completed Tests", "${user.completedTestsCount}")
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = TextStyle.BodyMedium.value)
        Text(value, style = TextStyle.BodyMedium.value, color = MaterialTheme.colorScheme.primary)
    }
}

private fun directionToString(direction: Direction): String {
    return when (direction) {
        Direction.BACKEND -> "Backend"
        Direction.FRONTEND -> "Frontend"
        Direction.DEVOPS -> "DevOps"
        Direction.DATA_SCIENCE -> "Data Science"
        Direction.DIRECTION_UNSPECIFIED -> "All Directions"
        else -> "Unknown"
    }
}

private fun levelToString(level: Level): String {
    return when (level) {
        Level.JUNIOR -> "Junior"
        Level.MIDDLE -> "Middle"
        Level.SENIOR -> "Senior"
        Level.LEVEL_UNSPECIFIED -> "All Levels"
        else -> "Unknown"
    }
}
