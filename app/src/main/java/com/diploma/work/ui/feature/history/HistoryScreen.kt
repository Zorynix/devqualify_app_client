package com.diploma.work.ui.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.work.R
import com.diploma.work.data.models.Article
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.components.LoadingCard
import com.orhanobut.logger.Logger
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("История") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                HistoryTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.setSelectedTab(tab) },
                        text = { Text(tab.displayName) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    LoadingCard(
                        message = "Загрузка истории...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.error != null -> {
                    ErrorCard(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadHistoryData() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    val articles = viewModel.getCurrentArticles()
                    if (articles.isEmpty()) {
                        EmptyHistoryCard(uiState.selectedTab)
                    } else {
                        HistoryArticlesList(
                            articles = articles,
                            selectedTab = uiState.selectedTab,
                            onRestoreDisliked = { articleId ->
                                viewModel.restoreDislikedArticle(articleId)
                            },
                            onRemoveFromLiked = { articleId ->
                                viewModel.removeFromLiked(articleId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryArticlesList(
    articles: List<Article>,
    selectedTab: HistoryTab,
    onRestoreDisliked: (Long) -> Unit,
    onRemoveFromLiked: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles) { article ->
            HistoryArticleCard(
                article = article,
                selectedTab = selectedTab,
                onRestoreDisliked = { onRestoreDisliked(article.id) },
                onRemoveFromLiked = { onRemoveFromLiked(article.id) }
            )
        }
    }
}

@Composable
private fun HistoryArticleCard(
    article: Article,
    selectedTab: HistoryTab,
    onRestoreDisliked: () -> Unit,
    onRemoveFromLiked: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (article.url.isNotEmpty()) {
                    uriHandler.openUri(article.url)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (article.isViewed && selectedTab != HistoryTab.VIEWED) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Просмотрено",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        when (article.isLiked) {
                            true -> {
                                if (selectedTab != HistoryTab.LIKED) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ThumbUp,
                                        contentDescription = "Понравилось",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            false -> {
                                if (selectedTab != HistoryTab.DISLIKED) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ThumbDown,
                                        contentDescription = "Не понравилось",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            null -> {}
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (article.rssSourceName.isNotEmpty()) {
                        Text(
                            text = article.rssSourceName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (article.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (selectedTab) {
                        HistoryTab.DISLIKED -> {
                            OutlinedButton(
                                onClick = { showRestoreConfirm = true },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Вернуть",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Вернуть",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        HistoryTab.LIKED -> {
                            OutlinedButton(
                                onClick = { showRemoveConfirm = true },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbDown,
                                    contentDescription = "Убрать из понравившихся",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Убрать",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        HistoryTab.VIEWED -> {
                        }
                    }
                }
                
                Text(
                    text = article.publishedAt
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MMM dd")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("Вернуть статью?") },
            text = { Text("Статья будет возвращена в общий список и больше не будет скрыта.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirm = false
                        onRestoreDisliked()
                    }
                ) {
                    Text("Вернуть")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreConfirm = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
    
    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            title = { Text("Убрать из понравившихся?") },
            text = { Text("Статья будет удалена из списка понравившихся.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveConfirm = false
                        onRemoveFromLiked()
                    }
                ) {
                    Text("Убрать")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveConfirm = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun EmptyHistoryCard(selectedTab: HistoryTab) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (icon, title, message) = when (selectedTab) {
                HistoryTab.VIEWED -> Triple(
                    Icons.Default.Visibility,
                    "Нет просмотренных статей",
                    "Статьи, которые вы откроете, будут отображаться здесь"
                )
                HistoryTab.LIKED -> Triple(
                    Icons.Default.ThumbUp,
                    "Нет понравившихся статей",
                    "Статьи, которые вам понравятся, будут отображаться здесь"
                )
                HistoryTab.DISLIKED -> Triple(
                    Icons.Default.ThumbDown,
                    "Нет скрытых статей",
                    "Статьи, которые вы скроете, будут отображаться здесь"
                )
            }
            
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
