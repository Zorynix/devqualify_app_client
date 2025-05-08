package com.diploma.work.ui.feature.achievements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.diploma.work.data.models.Achievement
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing = uiState.isLoading
    val state = rememberPullToRefreshState()
    
    LaunchedEffect(key1 = uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            Logger.e("Error displayed: ${uiState.errorMessage}")
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadAchievements() },
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
                    .padding(16.dp)
            ) {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                when {
                    isRefreshing && uiState.achievements.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.errorMessage != null -> {
                        ErrorView(
                            errorMessage = uiState.errorMessage ?: "Unknown error",
                            onRetry = { viewModel.loadAchievements() }
                        )
                    }
                    uiState.achievements.isEmpty() -> {
                        EmptyAchievementsView(onRefresh = { viewModel.loadAchievements() })
                    }
                    else -> {
                        AchievementsGrid(
                            achievements = uiState.achievements,
                            onAchievementClick = { viewModel.showAchievementDetails(it) }
                        )
                    }
                }
            }
        }
        
        if (uiState.showAchievementDetails && uiState.selectedAchievement != null) {
            AchievementDetailsDialog(
                achievement = uiState.selectedAchievement!!,
                onDismiss = { viewModel.dismissAchievementDetails() }
            )
        }
    }
}

@Composable
fun AchievementsGrid(
    achievements: List<Achievement>,
    onAchievementClick: (Achievement) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(achievements) { achievement ->
            AchievementCard(
                achievement = achievement,
                onClick = { onAchievementClick(achievement) },
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasBeenEarned by remember { mutableStateOf(achievement.dateEarned.isNotEmpty()) }
    
    Card(
        modifier = modifier
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (hasBeenEarned) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (hasBeenEarned) 
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasBeenEarned) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                if (achievement.iconUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(achievement.iconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = achievement.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center),
                        error = painterResource(android.R.drawable.star_big_on)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = achievement.name,
                        tint = if (hasBeenEarned) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (hasBeenEarned) 
                    MaterialTheme.colorScheme.onSecondaryContainer
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (hasBeenEarned) {
                Spacer(modifier = Modifier.height(4.dp))
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                
                val formattedDate = try {
                    val date = dateFormat.parse(achievement.dateEarned)
                    displayFormat.format(date ?: Date())
                } catch (e: Exception) {
                    Logger.e("Error parsing date: ${e.message}")
                    achievement.dateEarned
                }
                
                Text(
                    text = "Earned $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AchievementDetailsDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (achievement.iconUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(achievement.iconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = achievement.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                        error = painterResource(android.R.drawable.star_big_on)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = achievement.name,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                if (achievement.dateEarned.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    
                    val formattedDate = try {
                        val date = dateFormat.parse(achievement.dateEarned)
                        displayFormat.format(date ?: Date())
                    } catch (e: Exception) {
                        Logger.e("Error parsing date: ${e.message}")
                        achievement.dateEarned
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Earned on $formattedDate",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Close")
                }
            }
        }
    }
}

@Composable
fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "Retry")
        }
    }
}

@Composable
fun EmptyAchievementsView(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "No achievements",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No achievements yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Complete tests and challenges to earn achievements",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "Refresh")
        }
    }
}

@Composable
fun painterResource(id: Int) = androidx.compose.ui.res.painterResource(id)
