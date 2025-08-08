package com.diploma.work.ui.feature.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
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
import com.diploma.work.data.models.*
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.components.LoadingCard
import com.orhanobut.logger.Logger
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {},
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.refreshUserPreferences()
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.articles)) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.open_menu))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter)
                        )
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
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onSearch = { viewModel.loadArticles() },
                placeholder = stringResource(R.string.search_articles)
            )
            
            if (showFilters) {
                FiltersPanel(
                    uiState = uiState,
                    onTimePeriodChange = viewModel::setTimePeriod,
                    onSortByChange = viewModel::setSortBy,
                    onSourceChange = viewModel::toggleSource,
                    onClearFilters = viewModel::clearFilters,
                    onApplyUserPreferences = viewModel::applyUserPreferencesToFilters
                )
            }
            
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshArticles() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.articles.isEmpty() -> {
                        LoadingCard(
                            message = stringResource(R.string.loading_articles),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.error != null -> {
                        ErrorCard(
                            error = uiState.error!!,
                            onRetry = { viewModel.loadArticles() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.articles.isEmpty() -> {
                        EmptyStateCard()
                    }
                    else -> {
                        ArticlesList(
                            articles = if (uiState.searchQuery.isNotBlank()) uiState.filteredArticles else uiState.articles,
                            onArticleClick = { article -> 
                                viewModel.markArticleAsViewed(article.id)
                            },
                            onArticleLike = { article -> viewModel.likeArticle(article.id) },
                            onArticleDislike = { article -> viewModel.dislikeArticle(article.id) },
                            onRemoveLikeDislike = { article -> viewModel.removeLikeDislike(article.id) },
                            onLoadMore = { viewModel.loadMoreArticles() },
                            isLoading = uiState.isLoading,
                            hasMore = uiState.hasMore
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_desc))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_desc))
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true
    )
}

@Composable
private fun FiltersPanel(
    uiState: ArticlesUiState,
    onTimePeriodChange: (TimePeriod) -> Unit,
    onSortByChange: (SortType) -> Unit,
    onSourceChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onApplyUserPreferences: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.filters),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {                    TextButton(onClick = onApplyUserPreferences) {
                        Text(stringResource(R.string.my_interests))
                    }
                    TextButton(onClick = onClearFilters) {
                        Text(stringResource(R.string.clear_all))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            FilterSection(title = stringResource(R.string.time_period)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TimePeriod.entries) { period ->
                        FilterChip(
                            selected = uiState.selectedTimePeriod == period,
                            onClick = { onTimePeriodChange(period) },
                            label = { Text(period.getDisplayName()) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            FilterSection(title = stringResource(R.string.sort_by)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SortType.entries) { sortType ->
                        FilterChip(
                            selected = uiState.selectedSortType == sortType,
                            onClick = { onSortByChange(sortType) },
                            label = { Text(sortType.getDisplayName()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun ArticlesList(
    articles: List<Article>,
    onArticleClick: (Article) -> Unit,
    onArticleLike: (Article) -> Unit,
    onArticleDislike: (Article) -> Unit,
    onRemoveLikeDislike: (Article) -> Unit,
    onLoadMore: () -> Unit = {},
    isLoading: Boolean = false,
    hasMore: Boolean = true
) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull()
                if (lastVisibleItem != null && 
                    lastVisibleItem.index >= articles.size - 5 &&
                    hasMore && 
                    !isLoading) {
                    onLoadMore()
                }
            }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles) { article ->
            ArticleCard(
                article = article,
                onClick = { onArticleClick(article) },
                onLike = { onArticleLike(article) },
                onDislike = { onArticleDislike(article) },
                onRemoveLikeDislike = { onRemoveLikeDislike(article) }
            )
        }
        
        if (isLoading && articles.isNotEmpty()) {
            item {
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
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onRemoveLikeDislike: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var showDislikeConfirm by remember { mutableStateOf(false) }
    
    Logger.d("ArticleCard: title='${article.title}', rssSourceName='${article.rssSourceName}'")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                onClick()
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
                        
                        if (article.isViewed) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Просмотрено",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                    IconButton(
                        onClick = {
                            if (article.isLiked == true) {
                                onRemoveLikeDislike()
                            } else {
                                onLike()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Лайк",
                            modifier = Modifier.size(18.dp),
                            tint = if (article.isLiked == true) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            if (article.isLiked == false) {
                                onRemoveLikeDislike()
                            } else {
                                showDislikeConfirm = true
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Дизлайк",
                            modifier = Modifier.size(18.dp),
                            tint = if (article.isLiked == false) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
    
    if (showDislikeConfirm) {
        AlertDialog(
            onDismissRequest = { showDislikeConfirm = false },
            title = { Text("Скрыть статью?") },
            text = { Text("Эта статья будет скрыта из списка и больше не будет показываться. Вы уверены?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDislikeConfirm = false
                        onDislike()
                    }
                ) {
                    Text("Да, скрыть")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDislikeConfirm = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Article,
                contentDescription = stringResource(R.string.no_articles_desc),
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_articles_found),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_articles_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimePeriod.getDisplayName(): String = when (this) {
    TimePeriod.DAY -> stringResource(R.string.time_24_hours)
    TimePeriod.WEEK -> stringResource(R.string.time_week)
    TimePeriod.MONTH -> stringResource(R.string.time_month)
    TimePeriod.QUARTER -> stringResource(R.string.time_3_months)
    TimePeriod.YEAR -> stringResource(R.string.time_year)
    TimePeriod.ALL -> stringResource(R.string.time_all)
}

@Composable
private fun SortType.getDisplayName(): String = when (this) {
    SortType.RELEVANCE -> stringResource(R.string.sort_relevance)
    SortType.DATE_DESC -> stringResource(R.string.sort_newest)
    SortType.DATE_ASC -> stringResource(R.string.sort_oldest)
    SortType.RATING_DESC -> stringResource(R.string.sort_highest_rated)
    SortType.RATING_ASC -> stringResource(R.string.sort_lowest_rated)
}

private val ArticleDirection.displayName: String
    get() = when (this) {
        ArticleDirection.BACKEND -> "Backend"
        ArticleDirection.FRONTEND -> "Frontend"
        ArticleDirection.DEVOPS -> "DevOps"
        ArticleDirection.DATA_SCIENCE -> "Data Science"
        ArticleDirection.UNSPECIFIED -> "All"
    }
