package com.diploma.work.ui.feature.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.diploma.work.R
import com.diploma.work.data.models.User
import com.diploma.work.grpc.Direction
import com.diploma.work.grpc.Level
import com.diploma.work.ui.DiplomTextField
import com.diploma.work.ui.components.AvatarImage
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle
import kotlinx.coroutines.launch

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
                title = { Text("Leaderboard") },
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
                    FilterSection(
                        selectedDirection = uiState.direction,
                        selectedLevel = uiState.level,
                        selectedSortType = uiState.sortType,
                        onDirectionSelected = { viewModel.setDirection(it) },
                        onLevelSelected = { viewModel.setLevel(it) },
                        onSortTypeSelected = { viewModel.setSortType(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (uiState.users.isEmpty() && !uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found for these filters.",
                                style = TextStyle.bodyMedium,
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
fun FilterSection(
    selectedDirection: Direction,
    selectedLevel: Level,
    selectedSortType: LeaderboardSortType,
    onDirectionSelected: (Direction) -> Unit,
    onLevelSelected: (Level) -> Unit,
    onSortTypeSelected: (LeaderboardSortType) -> Unit
) {
    var directionMenuExpanded by remember { mutableStateOf(false) }
    var levelMenuExpanded by remember { mutableStateOf(false) }
    var sortTypeMenuExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Filters", style = TextStyle.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Direction",
                        style = TextStyle.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = directionMenuExpanded,
                        onExpandedChange = { directionMenuExpanded = it }
                    ) {
                        DiplomTextField(
                            value = directionToString(selectedDirection),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = directionMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = directionMenuExpanded,
                            onDismissRequest = { directionMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Directions") },
                                onClick = {
                                    onDirectionSelected(Direction.DIRECTION_UNSPECIFIED)
                                    directionMenuExpanded = false
                                }
                            )
                            
                            Direction.entries.filter { 
                                it != Direction.UNRECOGNIZED && it != Direction.DIRECTION_UNSPECIFIED 
                            }.forEach { direction ->
                                DropdownMenuItem(
                                    text = { Text(directionToString(direction)) },
                                    onClick = {
                                        onDirectionSelected(direction)
                                        directionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Level",
                        style = TextStyle.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = levelMenuExpanded,
                        onExpandedChange = { levelMenuExpanded = it }
                    ) {
                        DiplomTextField(
                            value = levelToString(selectedLevel),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = levelMenuExpanded,
                            onDismissRequest = { levelMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Levels") },
                                onClick = {
                                    onLevelSelected(Level.LEVEL_UNSPECIFIED)
                                    levelMenuExpanded = false
                                }
                            )
                            
                            Level.entries.filter { 
                                it != Level.UNRECOGNIZED && it != Level.LEVEL_UNSPECIFIED 
                            }.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(levelToString(level)) },
                                    onClick = {
                                        onLevelSelected(level)
                                        levelMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Sort By",
                    style = TextStyle.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = sortTypeMenuExpanded,
                    onExpandedChange = { sortTypeMenuExpanded = it }
                ) {
                    DiplomTextField(
                        value = selectedSortType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortTypeMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = sortTypeMenuExpanded,
                        onDismissRequest = { sortTypeMenuExpanded = false }
                    ) {
                        LeaderboardSortType.entries.forEach { sortType ->
                            DropdownMenuItem(
                                text = { Text(sortType.displayName) },
                                onClick = {
                                    onSortTypeSelected(sortType)
                                    sortTypeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
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
                avatarUrl = "https://ui-avatars.com/api/?name=${user.username}&background=random&size=200",
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
                    style = TextStyle.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${directionToString(user.direction)}, ${levelToString(user.level)}",
                    style = TextStyle.bodySmall,
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
                            style = TextStyle.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Achievements",
                            style = TextStyle.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LeaderboardSortType.CORRECT_ANSWERS -> {
                        Text(
                            text = "${user.totalCorrectAnswers}",
                            style = TextStyle.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Correct Answers",
                            style = TextStyle.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LeaderboardSortType.COMPLETED_TESTS -> {
                        Text(
                            text = "${user.completedTestsCount}",
                            style = TextStyle.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Completed Tests",
                            style = TextStyle.bodySmall,
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
                    avatarUrl = "https://ui-avatars.com/api/?name=${user.username}&background=random&size=200",
                    size = 100.dp,
                    borderWidth = 2.dp,
                    session = viewModel.session
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = user.username,
                    style = TextStyle.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${directionToString(user.direction)}, ${levelToString(user.level)}",
                    style = TextStyle.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Statistics",
                    style = TextStyle.titleMedium,
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
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Close")
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
        Text(label, style = TextStyle.bodyMedium)
        Text(value, style = TextStyle.bodyMedium, color = MaterialTheme.colorScheme.primary)
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
